/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 * Copyright 2020 The AoE Authors. All Rights Reserved.
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

package com.didi.aoe.library.common.stat;

import android.app.ActivityManager;
import android.content.Context;
import android.os.*;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Choreographer;
import com.didi.aoe.library.common.util.FileUtils;
import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.lang.Process;

/**
 * Created by wanglikun on 2018/9/13.
 * <p>
 * https://github.com/didi/DoraemonKit/blob/master/Android/doraemonkit/src/main/java/com/didichuxing/doraemonkit/kit/common/PerformanceDataManager.java
 *
 * @modified Noctis
 */

public class PerformanceDataManager {
    private final Logger mLogger = LoggerFactory.getLogger("Performance");

    private static final float SECOND_IN_NANOS = 1000000000f;
    private static final int NORMAL_FRAME_RATE = 1;
    private String filePath;
    private String memoryFileName = "memory.txt";
    private String cpuFileName = "cpu.txt";
    private String fpsFileName = "fps.txt";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private long mLastFrameTimeNanos;
    private int mLastFrameRate;
    private int mLastSkippedFrames;
    private float mLastCpuRate;
    private float mLastMemoryInfo;
    private String mPackageName;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private float mMaxMemory;
    private ActivityManager mActivityManager;
    private RandomAccessFile mProcStatFile;
    private RandomAccessFile mAppStatFile;
    private Long mLastCpuTime;
    private Long mLastAppCpuTime;
    private boolean mAboveAndroidO; // 是否是8.0及其以上
    private static final int MSG_CPU = 1;
    private static final int MSG_MEMORY = 2;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (mLastFrameTimeNanos != 0L) {
                long temp = frameTimeNanos - mLastFrameTimeNanos;
                if (temp != 0) {
                    mLastFrameRate = Math.round(SECOND_IN_NANOS / (frameTimeNanos - mLastFrameTimeNanos));
                    mLastSkippedFrames = 60 - mLastFrameRate;
                }
            }
            mLastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
            writeFpsDataIntoFile();
        }
    };

    private void executeCpuData() {
//        mLogger.debug("current thread name is ==" + Thread.currentThread().getName());
        if (mAboveAndroidO) {
            mLastCpuRate = getCpuDataForO();
//            mLogger.debug("cpu info is =" + mLastCpuRate);
            writeCpuDataIntoFile();
        } else {
            mLastCpuRate = getCPUData();
//            mLogger.debug("cpu info is =" + mLastCpuRate);
            writeCpuDataIntoFile();
        }
    }

    private float getCpuDataForO() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("top -n 1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int cpuIndex = -1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                int tempIndex = getCPUIndex(line);
                if (tempIndex != -1) {
                    cpuIndex = tempIndex;
                    continue;
                }
                if (line.startsWith(String.valueOf(android.os.Process.myPid()))) {
                    if (cpuIndex == -1) {
                        continue;
                    }
                    String[] param = line.split("\\s+");
                    if (param.length <= cpuIndex) {
                        continue;
                    }
                    String cpu = param[cpuIndex];
                    if (cpu.endsWith("%")) {
                        cpu = cpu.substring(0, cpu.lastIndexOf("%"));
                    }
                    float rate = Float.parseFloat(cpu) / Runtime.getRuntime().availableProcessors();
                    return rate;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return 0;
    }

    private int getCPUIndex(String line) {
        if (line.contains("CPU")) {
            String[] titles = line.split("\\s+");
            for (int i = 0; i < titles.length; i++) {
                if (titles[i].contains("CPU")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void executeMemoryData() {
        mLastMemoryInfo = getMemoryData();
//        mLogger.debug("memory info is =" + mLastMemoryInfo);
        writeMemoryDataIntoFile();
    }

    private static class Holder {
        private static PerformanceDataManager INSTANCE = new PerformanceDataManager();
    }

    private PerformanceDataManager() {
    }

    public static PerformanceDataManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(Context context) {
        filePath = getFilePath(context);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAboveAndroidO = true;
            mPackageName = context.getPackageName();
        }
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("handler-thread");
            mHandlerThread.start();
        }
        if (mHandler == null) {
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == MSG_CPU) {
                        executeCpuData();
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(MSG_CPU, NORMAL_FRAME_RATE * 1000);
                        }
                    } else if (msg.what == MSG_MEMORY) {
                        executeMemoryData();
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(MSG_MEMORY, NORMAL_FRAME_RATE * 1000);
                        }
                    }
                }
            };
        }
    }

    private String getFilePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/doraemon/";
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void startMonitorFrameInfo() {
        Choreographer.getInstance().postFrameCallback(mFrameCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void stopMonitorFrameInfo() {
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    public void startMonitorCPUInfo() {
        mHandler.sendEmptyMessageDelayed(MSG_CPU, NORMAL_FRAME_RATE * 1000);
    }

    public void stopMonitorCPUInfo() {
        mHandler.removeMessages(MSG_CPU);
    }

    public void destroy() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }

        if (mHandler == null) {
            return;
        }
        stopMonitorMemoryInfo();
        stopMonitorCPUInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            stopMonitorFrameInfo();
        }

        mHandlerThread = null;
        mHandler = null;
    }

    public void startMonitorMemoryInfo() {
        if (mMaxMemory == 0) {
            mMaxMemory = mActivityManager.getMemoryClass();
        }
        mHandler.sendEmptyMessageDelayed(MSG_MEMORY, NORMAL_FRAME_RATE * 1000);
    }

    public void stopMonitorMemoryInfo() {
        mHandler.removeMessages(MSG_MEMORY);
    }

    private void writeCpuDataIntoFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mLastCpuRate);
        stringBuilder.append(" ");
        stringBuilder.append(simpleDateFormat.format(new Date(System.currentTimeMillis())));
        FileUtils.writeTxtToFile(stringBuilder.toString(), filePath, cpuFileName);
    }

    private void writeMemoryDataIntoFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mLastMemoryInfo);
        stringBuilder.append(" ");
        stringBuilder.append(simpleDateFormat.format(new Date(System.currentTimeMillis())));
        FileUtils.writeTxtToFile(stringBuilder.toString(), filePath, memoryFileName);
    }

    private void writeFpsDataIntoFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mLastFrameRate);
        stringBuilder.append(" ");
        stringBuilder.append(simpleDateFormat.format(new Date(System.currentTimeMillis())));
        FileUtils.writeTxtToFile(stringBuilder.toString(), filePath, fpsFileName);
    }

    private float getCPUData() {
        long cpuTime;
        long appTime;
        float value = 0.0f;
        try {
            if (mProcStatFile == null || mAppStatFile == null) {
                mProcStatFile = new RandomAccessFile("/proc/stat", "r");
                mAppStatFile = new RandomAccessFile("/proc/" + android.os.Process.myPid() + "/stat", "r");
            } else {
                mProcStatFile.seek(0L);
                mAppStatFile.seek(0L);
            }
            String procStatString = mProcStatFile.readLine();
            String appStatString = mAppStatFile.readLine();
            String[] procStats = procStatString.split(" ");
            String[] appStats = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (mLastCpuTime == null && mLastAppCpuTime == null) {
                mLastCpuTime = cpuTime;
                mLastAppCpuTime = appTime;
                return value;
            }
            value = ((float) (appTime - mLastAppCpuTime) / (float) (cpuTime - mLastCpuTime)) * 100f;
            mLastCpuTime = cpuTime;
            mLastAppCpuTime = appTime;
        } catch (Exception e) {
            mLogger.warn("getCPUData fail: " + e.toString());
        }
        return value;
    }

    private float getMemoryData() {
        float mem = 0.0F;
        try {
            // 统计进程的内存信息 totalPss
            final Debug.MemoryInfo[] memInfo = mActivityManager.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
            if (memInfo.length > 0) {
                // TotalPss = dalvikPss + nativePss + otherPss, in KB
                final int totalPss = memInfo[0].getTotalPss();
                if (totalPss >= 0) {
                    // Mem in MB
                    mem = totalPss / 1024.0F;
                }
            }
        } catch (Exception e) {
            mLogger.warn("getMemoryData fail: " + e.toString());
        }
        return mem;
    }

    private float parseMemoryData(String data) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.getBytes())));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.contains("Permission Denial")) {
                break;
            } else {
                String[] lineItems = line.split("\\s+");
                if (lineItems != null && lineItems.length > 1) {
                    String result = lineItems[0];
                    mLogger.debug("result is ==" + result);
                    bufferedReader.close();
                    if (!TextUtils.isEmpty(result) && result.contains("K:")) {
                        result = result.replace("K:", "");
                        if (result.contains(",")) {
                            result = result.replace(",", ".");
                        }
                    }
                    // Mem in MB
                    return Float.parseFloat(result) / 1024;
                }
            }
        }
        return 0;
    }

    private float parseCPUData(String data) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.getBytes())));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.contains("Permission Denial")) {
                break;
            } else {
                String[] lineItems = line.split("\\s+");
                if (lineItems != null && lineItems.length > 1) {
                    mLogger.debug("result is ==" + lineItems[0]);
                    bufferedReader.close();
                    return Float.parseFloat(lineItems[0].replace("%", ""));
                }
            }
        }
        return 0;
    }

    public String getCpuFilePath() {
        return filePath + cpuFileName;
    }

    public String getMemoryFilePath() {
        return filePath + memoryFileName;
    }

    public String getFpsFilePath() {
        return filePath + fpsFileName;
    }

    public long getLastFrameRate() {
        return mLastFrameRate;
    }

    public float getLastCpuRate() {
        return mLastCpuRate;
    }

    public float getLastMemoryInfo() {
        return mLastMemoryInfo;
    }

    public int getLastSkippedFrames() {
        return mLastSkippedFrames;
    }

    public float getMaxMemory() {
        return mMaxMemory;
    }
}
