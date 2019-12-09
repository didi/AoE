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
import android.text.TextUtils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.didi.aoe.library.service.pojos.ModelOption;
import com.didi.aoe.library.service.pojos.UpgradeModelResult;
import com.didi.aoe.library.api.AoeModelOption;
import com.didi.aoe.library.api.AoeProcessor;
import com.didi.aoe.library.common.util.FileUtils;
import com.didi.aoe.library.lang.AoeIOException;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


/**
 * 模型动态升级服务配套模型加载组件
 *
 * @author noctis
 * @since 1.1.0
 */
public class AoeModelOptionLoader implements AoeProcessor.ModelOptionLoaderComponent {

    private static final String CONFIG_FILE_NAME = "model.config";
    private final Logger mLogger = LoggerFactory.getLogger("ModelLoader");
    private final HandlerThread mHandlerThread;
    private final Handler mModelRequestHandler;

    public AoeModelOptionLoader() {
        this.mHandlerThread = new HandlerThread("model-loader-worker");
        mHandlerThread.start();
        this.mModelRequestHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public AoeModelOption load(@NonNull Context context, @NonNull String modelDir) throws AoeIOException {
        ModelOption internalOption = readInternalConfig(context, modelDir);
        ModelOption downloadOption = readDownloadConfig(context, getTopVersionModelDir(context, modelDir));

        ModelOption suggestedModelOption = calculateSuggestedModelOption(internalOption, downloadOption);
        upgradeIfNeeded(context, suggestedModelOption);
        return suggestedModelOption;
    }

    /**
     * 升级到最新状态
     *
     * @param context
     * @param option
     */
    private void upgradeIfNeeded(@NonNull final Context context, @Nullable ModelOption option) {
        if (option == null) {
            mLogger.warn("upgradeIfNeeded: ModelOption required.");
            return;
        }

        // 请求新的版本模型
        ModelRequest modelRequest = new ModelRequest(Aoe.getInstance().getDataProvider().appId(), option);
        ModelContract.requestModel(context, modelRequest, mModelRequestHandler, new ModelContract.ModelRequestCallback() {
            @Override
            public void onModelRetrieved(UpgradeModelResult model) {
                mLogger.debug("requestModel success: " + model);
            }

            @Override
            public void onModelRequestFailed(int code, String msg) {
                mLogger.info("requestModel failed: " + code + ", msg: " + msg);
            }
        });
    }

    /**
     * 比较选出合适的模型配置
     *
     * @param internalOption
     * @param downloadOption
     * @return
     */
    @Nullable
    private ModelOption calculateSuggestedModelOption(@Nullable ModelOption internalOption, @Nullable ModelOption downloadOption) {
        if (downloadOption == null || !downloadOption.isValid()) {
            // 没有外置策略时使用内部策略
            return internalOption;
        }

        if (internalOption == null || !internalOption.isValid()) {
            // 只有外部策略时，使用外部策略，正常不应该执行到这个逻辑，内置配置默认需要配置。
            mLogger.warn("Internal option not found");
            return downloadOption;
        }

        // 比较内外模型版本，选择高版本配置
        if (!TextUtils.isEmpty(internalOption.getVersion())
                && !TextUtils.isEmpty(downloadOption.getVersion())
                && compareVersion(internalOption.getVersion(), downloadOption.getVersion()) < 0) {
            return downloadOption;
        } else {
            return internalOption;
        }
    }

    /**
     * 读取内置模型配置
     *
     * @param context
     * @param modelDir
     * @return
     */
    @Nullable
    private ModelOption readInternalConfig(@NonNull Context context, @NonNull String modelDir) {
        mLogger.debug("readInternalConfig: " + modelDir);
        String config = null;
        try {
            config = FileUtils.readString(context.getAssets().open(modelDir + File.separator + CONFIG_FILE_NAME));
        } catch (Exception e) {
            mLogger.error("readInternalConfig failed:", e);
        }
        return parseModelOption(config);
    }

    /**
     * 读取下载模型配置
     *
     * @param context
     * @param modelDir
     * @return
     */
    @Nullable
    private ModelOption readDownloadConfig(@NonNull Context context, @Nullable String modelDir) {
        mLogger.debug("readDownloadConfig: " + modelDir);
        if (TextUtils.isEmpty(modelDir)) {
            return null;
        }
        String configPath = FileUtils.getFilesDir(context) + File.separator + modelDir + File.separator + CONFIG_FILE_NAME;
        if (FileUtils.isExist(configPath)) {
            String config = FileUtils.readString(configPath);
            return parseModelOption(config);
        }
        return null;
    }

    @Nullable
    private ModelOption parseModelOption(@Nullable String config) {
        ModelOption option = null;
        if (config != null) {
            option = objectFromJson(config, ModelOption.class);
        }

        //make sure each filed is not empty
        if (option != null && !option.isValid()) {
            mLogger.warn("Some field of this config is empty: " + option.toString());
        }
        return option;
    }

    public <T> T objectFromJson(String json, Class<T> klass) {
        if (json == null) {
            return null;
        }
        try {
            return new Gson().fromJson(json, klass);
        } catch (Exception e) {
            mLogger.error("JsonUtil", e);
        }
        return null;
    }

    /**
     * 下载模型文件按版本号解压到对应目录下，按最新版本号读取模型文件
     *
     * @param context
     * @param modelDir
     * @return
     */
    private String getTopVersionModelDir(@NonNull Context context, @NonNull final String modelDir) {
        String rootPath = com.didi.aoe.library.common.util.FileUtils.getFilesDir(context);
        File dir = new File(rootPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && pathname.getName().startsWith(modelDir);
                }
            });
            if (files != null && files.length > 0) {
                File topVersionFile = Collections.max(Arrays.asList(files), new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return compareVersion(o1.getName(), o2.getName());
                    }
                });
                return topVersionFile.getName();

            }
        }
        return null;
    }

    /**
     * 版本名比较，版本名应遵循 [{x}.].{x}
     *
     * @param v1
     * @param v2
     * @return
     */
    private int compareVersion(@NonNull String v1, @NonNull String v2) {
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
            //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
            diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
            return diff;
        } catch (Exception e) {
            mLogger.error("compareVersion failed:", e);
        }
        return 0;
    }
}
