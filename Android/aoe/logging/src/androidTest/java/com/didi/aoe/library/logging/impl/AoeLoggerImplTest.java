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

package com.didi.aoe.library.logging.impl;

import com.didi.aoe.library.logging.Logger;
import com.didi.aoe.library.logging.LoggerFactory;

import org.junit.Test;

/**
 * @author noctis
 * @since 1.1.0
 */
public class AoeLoggerImplTest {

    @Test
    public void debug() {
        Logger logger = LoggerFactory.getLogger("Logger");
        logger.debug("%d", 123);
    }

    @Test
    public void info() {
        Logger logger = LoggerFactory.getLogger("Logger");
        logger.info("%d", 123);
    }

    @Test
    public void warn() {
        Logger logger = LoggerFactory.getLogger("Logger");
        logger.warn("%d", 123);

    }

    @Test
    public void error() {
        Logger logger = LoggerFactory.getLogger("Logger");
        try {
            int value = 1 / 0;
        } catch (Exception e) {
            logger.error("error: ", e);
        }
    }

}