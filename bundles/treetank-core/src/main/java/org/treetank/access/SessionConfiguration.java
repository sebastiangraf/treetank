/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
