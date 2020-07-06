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

import android.support.annotation.NonNull;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import java.util.Objects;

/**
 * AoE 实例类
 *
 * @author noctis
 * @since 1.1.0
 */
public final class AoeService {
    private static final Logger LOGGER = LoggerFactory.getLogger("AoeService");

    private static AoeService sInstance;

    private final Context applicationContext;

    private AoeDataProvider dataProvider;

    private AppInfoProvider appInfoProvider;
    private AoeService(@NonNull Context context) {
        if (context.getApplicationContext() == null) {
            applicationContext = context;
        } else {
            applicationContext = context.getApplicationContext();
        }

        HttpManager.Companion.init(applicationContext);
    }

    public static void init(@NonNull Context context) {

        if (sInstance == null) {
            Context applicationContext;
            if (context.getApplicationContext() == null) {
                applicationContext = context;
            } else {
                applicationContext = context.getApplicationContext();
            }
            sInstance = new AoeService(applicationContext);

        }

        sInstance.dataProvider = new AoeDataProvider() {
            @Override
            public double latitude() {
                return 30.28;
            }

            @Override
            public double longitude() {
                return 120.15;
            }
        };

        sInstance.appInfoProvider = new AppInfoProvider(context);
    }

    public static AoeService getInstance() {
        return sInstance;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public AoeDataProvider getDataProvider() {
        Objects.requireNonNull(dataProvider);
        return dataProvider;
    }

    public AppInfoProvider getAppInfoProvider(){
        return appInfoProvider;
    }
}
