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

import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author noctis
 * @since 1.0.0
 */
class MD5Util {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] LITTLEHEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    static String encode(String input) {
        try {
            MessageDigest digest;
            (digest = MessageDigest.getInstance("MD5")).update(input.getBytes());
            return format(digest.digest());
        } catch (NoSuchAlgorithmException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    static String encode(byte[] input) {
        try {
            MessageDigest digest;
            (digest = MessageDigest.getInstance("MD5")).update(input);
            return format(digest.digest());
        } catch (NoSuchAlgorithmException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    /**
     * 对文件进行MD5计算
     *
     * @param filename 文件名
     * @return 32位MD5Sum
     */
    static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filename);
            md5 = (MessageDigest) MessageDigest.getInstance("MD5").clone();
            if (null == md5)
                return "";
            byte[] md5dist = null;
            synchronized (md5) {
                while ((numRead = fis.read(buffer)) > 0) {
                    md5.update(buffer, 0, numRead);
                }
                fis.close();
                md5dist = md5.digest();
            }
            return toHexString(md5dist, false);
        } catch (Exception e) {
            System.out.println("error");
            return null;
        }
    }

    public static String toHexString(byte[] b, boolean bBig) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            if (bBig) {
                sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
                sb.append(HEX_DIGITS[b[i] & 0x0f]);
            } else {
                sb.append(LITTLEHEX_DIGITS[(b[i] & 0xf0) >>> 4]);
                sb.append(LITTLEHEX_DIGITS[b[i] & 0x0f]);
            }
        }
        return sb.toString();
    }

    private static String format(@NonNull byte[] input) {
        StringBuffer sb = new StringBuffer(input.length * 2);

        for (int i = 0; i < input.length; ++i) {
            sb.append(Character.forDigit((input[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(input[i] & 15, 16));
        }

        return sb.toString();
    }
}
