package com.didi.aoe.library.logging;

/**
 * @author noctis
 */
public interface Logger {
    /**
     * Log a message at DEBUG level
     *
     * @param msg  The format string
     * @param args The arguments
     */
    void debug(final String msg, final Object... args);

    /**
     * Log a message at INFO level
     *
     * @param msg  The format string
     * @param args The arguments
     */
    void info(final String msg, final Object... args);

    /**
     * Log a message at WARN level
     *
     * @param msg The message accompanying the exception
     * @param t   The exception to log
     */
    void warn(final String msg, final Throwable t);

    /**
     * Log a message at WARN level
     *
     * @param msg  The format string
     * @param args The arguments
     */
    void warn(final String msg, final Object... args);

    /**
     * Log a message at ERROR level
     *
     * @param msg The message accompanying the exception
     * @param t   The exception to log
     */
    void error(final String msg, final Throwable t);

    /**
     * Log a message at ERROR level
     *
     * @param msg  The format string
     * @param args The arguments
     */
    void error(final String msg, final Object... args);
}
