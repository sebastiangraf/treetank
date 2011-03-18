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

package org.treetank.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


import org.slf4j.LoggerFactory;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.settings.ESessionSetting;
import org.treetank.utils.LogWrapper;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the session-wide settings that can not change. This included stuff like commit-threshold and number
 * of usable write/read transactions
 * 
 */
public final class SessionConfiguration {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(SessionConfiguration.class));

    /** Props to hold all related data. */
    private final Properties mProps;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @throws TTUsageException
     *             if session is not valid
     */
    public SessionConfiguration() throws TTUsageException {
        this(new Properties());
    }

    /**
     * Constructor using specified properties. Every property which is not
     * specified is set by the standard-one
     * 
     * @param props
     *            to be specified
     * @throws TTUsageException
     *             if session could not be established
     */
    public SessionConfiguration(final Properties props) throws TTUsageException {
        this.mProps = new Properties();
        for (final ESessionSetting enumProps : ESessionSetting.values()) {
            if (props.containsKey(enumProps.name())) {
                this.getProps().setProperty(enumProps.name(), props.getProperty(enumProps.name()));
            } else {
                this.getProps().setProperty(enumProps.name(), enumProps.getValue());
            }
        }

    }

    /**
     * Constructor using specified properties stored in a file. Every property
     * which is not specified is set by the standard-one
     * 
     * @param propFile
     *            to be specified
     * @throws AbsTTException
     *             if session could not be established
     */
    public SessionConfiguration(final File propFile) throws AbsTTException {
        this(new Properties());
        try {
            getProps().load(new FileInputStream(propFile));
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }

    }

    /**
     * Getting the properties set in this configuration. All properties must
     * refer to <code>ESessionSetting</code>.
     * 
     * @return the properties to this session
     */
    public Properties getProps() {
        return mProps;
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        return new StringBuilder(mProps.toString()).toString();
    }
    
}
