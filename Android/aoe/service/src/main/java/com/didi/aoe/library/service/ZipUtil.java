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


import com.didi.aoe.library.lang.AoeException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipUtil {
    /**
     * 解压后，把解压的文件夹重命名为changeName，解压对象必须有一个根目录。
     *
     * @param zipPath    压缩包路径
     * @param outputPath 压缩包解压目录
     * @param changeName 压缩包解压后的目录名
     * @return true 成功
     */
    static boolean unpackZipAndRename(String zipPath, String outputPath, String changeName) {
        try (InputStream is = new FileInputStream(zipPath);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ) {
            return unpackZipAndChangeName(zis, outputPath, changeName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean unpackZipAndChangeName(ZipInputStream zis, String outputPath, String changeName) {
        try {
            String filename;
            if (!outputPath.endsWith(File.separator)) {
                outputPath = outputPath + File.separator;
            }
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (filename.contains("../")) {
                    throw new AoeException("unsecurity file");
                }
                int idx = filename.indexOf('/');
                if (idx != -1) {
                    filename = filename.replaceFirst(filename.substring(0, idx), changeName);
                }
                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(outputPath + filename);
                    fmd.mkdirs();
                    continue;
                } else {
                    String fp = outputPath + filename;
                    File pDir = new File(fp.substring(0, fp.lastIndexOf('/')));
                    if (!pDir.exists()) {
                        pDir.mkdirs();
                    }
                }

                FileOutputStream fout = new FileOutputStream(outputPath + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unpackZip(ZipInputStream zis, String outputPath) {
        try {
            String filename;
            if (!outputPath.endsWith(File.separator)) {
                outputPath = outputPath + File.separator;
            }
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (filename.contains("../")) {
                    throw new AoeException("unsecurity file");
                }
                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(outputPath + filename);
                    fmd.mkdirs();
                    continue;
                } else {
                    String fp = outputPath + filename;
                    File pDir = new File(fp.substring(0, fp.lastIndexOf('/')));
                    if (!pDir.exists()) {
                        pDir.mkdirs();
                    }
                }

                FileOutputStream fout = new FileOutputStream(outputPath + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                zis.close();
            } catch (Exception ignored) {
            }
        }
    }

}
