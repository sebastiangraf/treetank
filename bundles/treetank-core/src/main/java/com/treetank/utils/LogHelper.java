package com.treetank.utils;

import org.apache.commons.logging.Log;

/**
 * Provides some logging helper methods.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class LogHelper {

    /** Logger. */
    private static Log LOGGER;

    /** Determines if debugging is enabled. */
    public static boolean DEBUG;

    /** Determines if info is enabled. */
    public static boolean INFO;

    /**
     * Constructor.
     * 
     * @param LOGGER
     *            logger.
     */
    public LogHelper(final Log logger) {
        LOGGER = logger;
        DEBUG = LOGGER.isDebugEnabled();
        INFO = LOGGER.isInfoEnabled();
    }

    /**
     * Log debugging information.
     * 
     * @param message
     *            Message to log.
     */
    public synchronized void debug(final String message) {
        if (DEBUG) {
            LOGGER.debug(message);
        }
    }

    /**
     * Log information.
     * 
     * @param message
     *            Message to log.
     */
    public synchronized void info(final String message) {
        if (INFO) {
            LOGGER.info(message);
        }
    }

}
