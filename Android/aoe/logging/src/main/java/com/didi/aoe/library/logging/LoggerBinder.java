package com.didi.aoe.library.logging;

/**
 * @author noctis
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface LoggerBinder {
    Logger getLogger(String tag);
}
