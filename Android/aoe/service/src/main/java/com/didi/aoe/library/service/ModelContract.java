/*
 * Copyright 2019 The AoE Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.aoe.library.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.LruCache;

import com.didi.aoe.extensions.downloadmanager.DownloadRequest;
import com.didi.aoe.library.common.util.FileUtils;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.didi.aoe.library.service.pojos.ModelOption;
import com.didi.aoe.library.service.pojos.UpgradeModelResult;
import com.google.gson.Gson;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

import static com.didi.aoe.library.service.ModelContract.ModelRequestCallback.FAIL_REASON_MODEL_LOADER_ERROR;
import static com.didi.aoe.library.service.ModelContract.ModelRequestCallback.FAIL_REASON_MODEL_QUERY_ERROR;

/**
 * 模型门面交互类
 *
 * @author noctis
 * @since 1.1.0
 */
public final class ModelContract {
    private static final Logger mLogger = LoggerFactory.getLogger("ModelContract");
    private static final Object sLock = new Object();
    private static final LruCache<String, UpgradeModelResult> sModelCache = new LruCache<>(10);
    private static Handler sHandler;
    private static HandlerThread sThread;

    private ModelContract() {
    }

    /**
     * 请求应用新模型
     *
     * @param context
     * @param request
     * @param handler
     * @param callback
     */
    public static void requestModel(@NonNull final Context context, @NonNull final ModelRequest request,
            @NonNull Handler handler, @NonNull final ModelRequestCallback callback) {
        final Handler callerThreadHandler = new Handler();
        final UpgradeModelResult cachedModel = sModelCache.get(request.getIdentifier());
        if (cachedModel != null) {
            mLogger.debug("requestModel cached: " + cachedModel);
            callerThreadHandler.post(() -> callback.onModelRetrieved(cachedModel));
            return;
        }

        handler.post(() -> {
            final ModelResult result = fetchModel(context, request);

            // 当拉取新模型过程中可能已经应用了新模型
            final UpgradeModelResult anotherCachedModel = sModelCache.get(request.getIdentifier());
            if (anotherCachedModel != null) {
                mLogger.debug("requestModel cached after fetch: " + anotherCachedModel);
                callerThreadHandler.post(() -> callback.onModelRetrieved(anotherCachedModel));
                return;
            }

            if (ModelResult.STATUS_OK != result.getStatusCode()) {
                callerThreadHandler
                        .post(() -> callback.onModelRequestFailed(FAIL_REASON_MODEL_QUERY_ERROR, "result: " + result));
                return;
            } else {
                final UpgradeModelResult model = result.getModel();
                if (model != null) {
                    // 下载模型校验成功
                    sModelCache.put(request.getIdentifier(), model);
                    callerThreadHandler.post(() -> callback.onModelRetrieved(model));
                    return;
                }
            }

            callerThreadHandler
                    .post(() -> callback.onModelRequestFailed(FAIL_REASON_MODEL_LOADER_ERROR, "result: " + result));

        });


    }

    /**
     * 同步拉取模型
     *
     * @param context
     * @param request
     * @return
     */
    @WorkerThread
    @NonNull
    public static ModelResult fetchModel(@NonNull Context context, @NonNull ModelRequest request) {
        Map<String, Object> bodyMap = buildBodyMap(request);
        mLogger.debug("===fetchModel url:"+AoeAPI.ModelUpdate.API_REQUEST_MODEL_UPDATE);
        try (Response response = HttpManager.Companion.getInstance()
                .performRequest(AoeAPI.ModelUpdate.API_REQUEST_MODEL_UPDATE, bodyMap)) {
            mLogger.debug("===fetchModel response code:"+response.code());

            if (!response.isSuccessful() || response.body() == null) {
                mLogger.warn("performRequest failed: " + response.message());
                return new ModelResult(ModelResult.STATUS_NETWORK_FAILED,
                        "performRequest failed: " + response.message(), null);
            }


            String body = response.body().string();
            mLogger.debug("===fetchModel response: " + body);


            AoeResponse modelResponse = new Gson().fromJson(body, AoeResponse.class);

//            mLogger.debug("===fetchModel AoeResponse: " + modelResponse);

            if (modelResponse.getCode() != 0) {
                // 业务请求失败，直接返回
                return new ModelResult(ModelResult.STATUS_UNEXPECTED_DATA, "Business response error: " + modelResponse,
                        null);
            }

            UpgradeModelResult model = modelResponse.getData();
            if (model != null && model.isValid()) {
                startDownloadAsync(context, request, model);
                return new ModelResult(ModelResult.STATUS_OK, "New model available, downloading...", model);
            } else {
                // 当前模型已为最新
                return new ModelResult(ModelResult.STATUS_UNEXPECTED_DATA, "ModelResponse: " + modelResponse, model);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLogger.error("performRequest Exception: " + e);
            return new ModelResult(ModelResult.STATUS_UNEXPECTED_DATA, "performRequest IOException: " + e, null);
        }

    }

    /**
     * @param context
     * @param request
     * @param model
     */
    private static void startDownloadAsync(@NonNull Context context,
            @NonNull ModelRequest request,
            @NonNull UpgradeModelResult model) {
        String modelDir = request.getCurrentModelOption().getModelDir();

        // 准备下载，设置zip下载路径。
        String zipPath = prepareDestination(context, modelDir, model.getVersionName());

        // 同步下载模型文件
        downloadModel(context, model, zipPath, new DownloadRequest.OnProgressListener() {
            @Override
            public void onProgressUpdated(long totalBytes, long currentBytes) {
                mLogger.debug("updateResult totalBytes : " + totalBytes + ", currentBytes: " + currentBytes);
            }

            @Override
            public void onResult(boolean success) {
                mLogger.debug("Download result : " + success);
                if (success) {
                    syncModelFiles(modelDir, model, context, zipPath);
                }

                // 清理下载文件
                File file = new File(zipPath);
                file.delete();
            }
        });
    }

    private static void syncModelFiles(String modelDir, @NonNull UpgradeModelResult model, @NonNull Context context,
            String zipPath) {
        // 模型归档目录名
        String targetDirName = model.getVersionName();
        String targetDirPath = modelDir + File.separator + targetDirName;

        // 解压zip包
        boolean unpackOk = unpackDownloadZip(context, targetDirPath, model.getMd5(), zipPath);

        if (unpackOk) {
            // 解压成功，清理目录下文件缓存旧版本文件 TODO
            doModelDirCleanExclude(FileUtils.getFilesDir(context), modelDir, targetDirName);

            // 检验文件完整性
            boolean validOk = validDownload(context, targetDirPath);
            mLogger.debug("valid result: " + validOk);
            if (!validOk) {
                doModelDirClean(FileUtils.getFilesDir(context), modelDir, targetDirName);
            }
        }
    }

    /**
     * 解压下载的zip包
     *
     * @param context
     * @param targetDir
     * @param md5
     * @param zipPath
     * @return
     */
    private static boolean unpackDownloadZip(@NonNull Context context, @NonNull String targetDir, String md5,
            String zipPath) {
        String zipSign = MD5Util.md5sum(zipPath);
        mLogger.debug("fetchModel zipSign : " + zipSign + " sign: " + md5);

        if (zipSign != null && zipSign.equals(md5)) {

            return ZipUtil.unpackZipAndRename(zipPath, FileUtils.getFilesDir(context), targetDir);
        }

        return false;
    }

    private static void downloadModel(@NonNull Context context, UpgradeModelResult model, String zipPath,
            DownloadRequest.OnProgressListener onProgressListener) {
        final DownloadRequest req = new DownloadRequest(context, model.getUrl());

        mLogger.debug("prepareDestination: " + zipPath);
        req.setDestinationUri(zipPath);
        req.setOnProgressListener(onProgressListener);

        req.send();

    }

    @NonNull
    private static Map<String, Object> buildBodyMap(@NonNull ModelRequest request) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(HttpParams.PARAM_APP_ID, request.getAppId());
        bodyMap.put(HttpParams.PARAM_MODEL_ID, request.getModelId());
        bodyMap.put(HttpParams.PARAM_MODEL_VERSION_CODE, request.getCurrentModelVersion());

        final AoeDataProvider dataProvider = AoeService.getInstance().getDataProvider();
        if (dataProvider != null) {
            bodyMap.put(HttpParams.PARAM_COMMON_LAT, dataProvider.latitude());
            bodyMap.put(HttpParams.PARAM_COMMON_LNG, dataProvider.longitude());
        }
        return bodyMap;
    }

    /**
     * 清理模型目录下旧文件
     *
     * @param modelDir
     * @param exceptDir
     */
    private static void doModelDirCleanExclude(String rootPath, String modelDir, String exceptDir) {
        try {
            File rootDir = new File(rootPath + File.separator + modelDir);
            if (rootDir.exists() && rootDir.isDirectory()) {
                File[] subDirs = rootDir.listFiles();
                if (subDirs == null) {
                    return;
                }
                for (File file : subDirs) {
                    if (!file.isDirectory()) {
                        // 只处理modelDir的子目录
                        continue;
                    }

                    String fileName = file.getName();
                    if (compareVersion(fileName, exceptDir) < 0) {
                        // 对老版本的目录进行清理
                        File[] subFiles = file.listFiles();
                        if (subFiles == null) {
                            return;
                        }
                        for (File f : subFiles) {
                            boolean result = f.delete();
                            mLogger.debug(
                                    "deleteDirsExcept delete model file and config file: " + f.getName() + " result: "
                                            + result);
                        }
                        boolean result = file.delete();
                        mLogger.debug("deleteDirsExcept delete model dir: " + fileName + " result: " + result);
                    }

                }
            }
        } catch (Exception e) {
            mLogger.error("deleteDirsExcept deleteDirs failed:", e);
        }
    }

    private static void doModelDirClean(String rootPath, String modelDir, String targetDir) {
        try {
            File rootDir = new File(rootPath + File.separator + modelDir);
            if (rootDir.exists() && rootDir.isDirectory()) {
                File[] subDirs = rootDir.listFiles();
                if (subDirs == null) {
                    return;
                }
                for (File file : subDirs) {
                    if (!file.isDirectory()) {
                        // 只处理modelDir的子目录
                        continue;
                    }

                    String fileName = file.getName();
                    if (compareVersion(fileName, targetDir) == 0) {
                        // 对老版本的目录进行清理
                        File[] subFiles = file.listFiles();
                        if (subFiles == null) {
                            return;
                        }
                        for (File f : subFiles) {
                            boolean result = f.delete();
                            mLogger.debug(
                                    "doModelDirClean delete model file and config file: " + f.getName() + " result: "
                                            + result);
                        }
                        boolean result = file.delete();
                        mLogger.debug("doModelDirClean delete model dir: " + fileName + " result: " + result);
                    }

                }
            }
        } catch (Exception e) {
            mLogger.error("doModelDirClean deleteDirs failed:", e);
        }
    }

    private static int compareVersion(@NonNull String v1, @NonNull String v2) {
        try {
            String[] versionArray1 = v1.split("\\.");//注意此处为正则匹配，不能用"."；
            String[] versionArray2 = v2.split("\\.");
            int idx = 0;
            int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
            int diff = 0;
            while (idx < minLength
                    && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
                    && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符
                ++idx;
            }
            // 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
            diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
            return diff;
        } catch (Exception e) {
            mLogger.error("compareVersion failed:", e);
        }
        return 0;
    }

    private static String prepareDestination(Context context, String modelDir, String version) {
        File modelRootDir = new File(FileUtils.getFilesDir(context) + File.separator + modelDir);
        if (!modelRootDir.exists()) {
            modelRootDir.mkdirs();
        }
        return modelRootDir.getAbsolutePath() + File.separator + modelDir + "_" + version
                + ".zip";
    }

    /**
     * @param modelDir
     * @param version
     * @return
     */
    private static String versionNamedDir(String modelDir, String version) {
        return modelDir + "_" + version;
    }

    /**
     * 校验下载模型文件是否合法
     *
     * @param context
     * @param modelDir
     * @return
     */
    private static boolean validDownload(Context context, String modelDir) {
        if (hasDownloadConfig(context, modelDir)) {
            ModelOption downloadOption = readDownloadConfig(context, modelDir);
            if (downloadOption != null && hasValidDownloadModel(context, downloadOption, modelDir)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasValidDownloadModel(@NonNull Context context, @NonNull ModelOption downloadModelOption,
            @NonNull String loadModelDir) {
        String modelPath = FileUtils.getFilesDir(context) + File.separator + loadModelDir;
        File modelDir = new File(modelPath);
        if (modelDir.exists() && modelDir.isDirectory()) {
            File[] files = modelDir.listFiles();
            if (files != null) {//fix online issues
                for (File f : files) {
                    if (f.isFile() && f.getName().startsWith(downloadModelOption.getModelName())) {
                        String modelMd5 = MD5Util.md5sum(f.getAbsolutePath());
                        if (modelMd5 != null && modelMd5.equals(downloadModelOption.getSign())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static ModelOption readDownloadConfig(@NonNull Context context, @NonNull String modelDir) {
        String configPath =
                FileUtils.getFilesDir(context) + File.separator + modelDir + File.separator + "model.config";
        if (FileUtils.isExist(configPath)) {
            String config = FileUtils.readString(configPath);
            if (config != null) {
                return new Gson().fromJson(config, ModelOption.class);
            }
        }
        return null;
    }

    private static boolean hasDownloadConfig(@NonNull Context context, @NonNull String loadModelDir) {
        String configPath =
                FileUtils.getFilesDir(context) + File.separator + loadModelDir + File.separator + "model.config";
        if (FileUtils.isExist(configPath)) {
            return true;
        }
        return false;
    }

    public static class ModelResult {
        public static final int STATUS_OK = 0;

        /**
         * 服务请求失败
         */
        public static final int STATUS_NETWORK_FAILED = 1;

        /**
         * 请求Model信息解析失败
         */
        public static final int STATUS_UNEXPECTED_DATA = 2;

        /**
         * 文件下载中
         */
        public static final int STATUS_DOWNLOADING = 3;
        @ModelResultStatus
        private final int mStatusCode;
        private final UpgradeModelResult mModel;
        private String mMessage;
        public ModelResult(@ModelResultStatus int statusCode, UpgradeModelResult model) {
            this(statusCode, null, model);
        }

        public ModelResult(@ModelResultStatus int statusCode, String msg, UpgradeModelResult model) {
            this.mStatusCode = statusCode;
            this.mModel = model;
            this.mMessage = msg;
        }

        public int getStatusCode() {
            return mStatusCode;
        }

        public UpgradeModelResult getModel() {
            return mModel;
        }

        public String getMessage() {
            return mMessage;
        }

        @Override
        public String toString() {
            return "ModelResult{" +
                    "mStatusCode=" + mStatusCode +
                    ", mModel=" + mModel +
                    ", mMessage='" + mMessage + '\'' +
                    '}';
        }

        /**
         * @hide
         */
        @IntDef({STATUS_OK, STATUS_NETWORK_FAILED,
                 STATUS_UNEXPECTED_DATA, STATUS_DOWNLOADING})
        @Retention(RetentionPolicy.SOURCE)
        @interface ModelResultStatus {
        }
    }

    public static class ModelRequestCallback {
        public static final int FAIL_REASON_PROVIDER_NOT_FOUND = -1;
        public static final int FAIL_REASON_WRONG_CERTIFICATES = -2;
        public static final int FAIL_REASON_MODEL_QUERY_ERROR = -3;
        public static final int FAIL_REASON_MODEL_LOADER_ERROR = -4;

        public ModelRequestCallback() {
        }

        public void onModelRetrieved(UpgradeModelResult model) {
        }

        public void onModelRequestFailed(@ModelRequestFailReason int reason, String msg) {
        }

        /**
         * @hide
         */
        @IntDef({FAIL_REASON_PROVIDER_NOT_FOUND, FAIL_REASON_WRONG_CERTIFICATES,
                 FAIL_REASON_MODEL_QUERY_ERROR, FAIL_REASON_MODEL_LOADER_ERROR})
        @Retention(RetentionPolicy.SOURCE)
        @interface ModelRequestFailReason {
        }
    }
}
