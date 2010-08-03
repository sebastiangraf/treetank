/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.utils;

import org.slf4j.Logger;

/**
 * Provides some logging helper methods.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class LogWrapper {

    /** Logger. */
    private final Logger mLogger;

    /**
     * Constructor.
     * 
     * @param paramLogger
     *            logger.
     */
    public LogWrapper(final Logger paramLogger) {
        mLogger = paramLogger;
    }

    /**
     * Log error information.
     * 
     * @param paramMessage
     *            Message to log.
     * @param paramObjects
     *            Objects for message
     */
    public void error(final String paramMessage, final Object... paramObjects) {
        if (mLogger.isErrorEnabled()) {
            mLogger.error(paramMessage, paramObjects);
        }
    }

    /**
     * Log error information.
     * 
     * @param paramExc
     *            Exception to log.
     */
    public void error(final Exception paramExc) {
        if (mLogger.isErrorEnabled()) {
            mLogger.error(paramExc.getMessage(), paramExc);
        }
    }

    /**
     * Log debugging information.
     * 
     * @param paramMessage
     *            Message to log.
     * @param paramObjects
     *            objects for data
     */
    public void debug(final String paramMessage, final Object... paramObjects) {
        if (mLogger.isDebugEnabled()) {
            mLogger.debug(paramMessage, paramObjects);
        }
    }

    /**
     * Log information.
     * 
     * @param paramMessage
     *            Message to log.
     * @param paramObjects
     *            objects for data
     */
    public void info(final String paramMessage, final Object... paramObjects) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info(paramMessage, paramObjects);
        }
    }

    /**
     * Warn information.
     * 
     * @param paramMessage
     *            Message to log.
     * @param paramObjects
     *            objects for data
     */
    public void warn(final String paramMessage, final Object... paramObjects) {
        if (mLogger.isWarnEnabled()) {
            mLogger.warn(paramMessage, paramObjects);
        }
    }

}
