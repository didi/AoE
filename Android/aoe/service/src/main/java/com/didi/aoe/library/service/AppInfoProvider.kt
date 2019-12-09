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

package com.didi.aoe.library.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils

/**
 * 应用信息提供者
 * @author noctis
 * @since 1.1.0
 */
class AppInfoProvider(context: Context) {
    /**
     * 分配给应用的AppId
     */
    val appKey: String by lazy {
        getAppKey(context)
    }
    /**
     * 获取设备唯一标识码
     */
    val deviceSN: String by lazy {
        getDeviceId(context)
    }
    /**
     * 设备型号
     */
    val deviceType: String by lazy {
        Build.MODEL
    }

    private fun getAppKey(context: Context): String {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData.getString("com.didi.aoe.service.API_KEY", "API_KEY not found")
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    @Synchronized
    private fun getDeviceId(context: Context): String {
        var deviceId: String? = null
        val permissionGranted =
                context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PERMISSION_GRANTED
        if (permissionGranted) {
            var imei: String? = null

            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
                @Suppress("DEPRECATION")
                imei = telephonyManager?.deviceId
            } catch (e: Exception) {
                // ignore
            }

            if (isValidDeviceId(imei)) {
                deviceId = imei!!
            }
        }

        if (TextUtils.isEmpty(deviceId)) {
            try {
                deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (var8: Exception) {
            }

        }

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = getVirtualDeviceId()
        }

        return deviceId!!
    }

    private fun isValidDeviceId(imei: String?): Boolean {
        if (TextUtils.isEmpty(imei)) {
            return false
        } else if (imei!!.length < 15) {
            return false
        } else {
            var sameChar = true

            for (i in 0 until imei.length - 1) {
                if (imei[i] != imei[i + 1]) {
                    sameChar = false
                    break
                }
            }

            return !sameChar
        }
    }

    private fun getVirtualDeviceId(): String {
        val info = StringBuffer()
                .append(Build.BOARD)
                .append(Build.BRAND)
                .append(Build.DEVICE)
                .append(Build.DISPLAY)
                .append(Build.HOST)
                .append(Build.ID)
                .append(Build.MANUFACTURER)
                .append(Build.USER)
                .toString()
        return MD5Util.encode(info).substring(0, 15)

    }
}
