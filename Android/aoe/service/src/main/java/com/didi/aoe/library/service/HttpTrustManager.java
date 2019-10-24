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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author noctis
 * @since 1.1.0
 */
class HttpTrustManager implements X509TrustManager {

    private static final X509Certificate[] sAcceptedCertificates = new X509Certificate[]{};

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

    }


    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return sAcceptedCertificates;
    }

    static HostnameVerifier getHostnameVerifier() {

        return new HostnameVerifier() {

            @Override
            public boolean verify(String hostname, SSLSession session) {
                if ("star.xiaojukeji.com".equalsIgnoreCase(hostname)) {
                    return true;
                } else {
                    HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify(hostname, session);
                }
            }
        };
    }

    static SSLSocketFactory getSSLSocketFactory() {

        SSLSocketFactory factory = null;

        javax.net.ssl.TrustManager[] trustManagers = new javax.net.ssl.TrustManager[]{
                new HttpTrustManager()
        };

        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            factory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return factory;
    }
}
