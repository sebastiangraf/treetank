/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.settings.EStoragePaths;
import org.treetank.utils.LogWrapper;

/**
 * <h1>Database Configuration</h1> class represents a configuration of a
 * database. includes all
 * settings which have to be made when it comes to the creation of the database.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class DatabaseConfiguration {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(DatabaseConfiguration.class));

    /** Absolute path to tnk directory. */
    private final File mFile;

    /** Props to hold all related data. */
    private final Properties mProps;

    /**
     * Constructor, just the location of either an existing or a new database.
     * 
     * @param paramFile
     *            the path to the database
     * @throws AbsTTException
     *             if the reading of the props is failing or properties are not
     *             valid
     */
    public DatabaseConfiguration(final File paramFile) throws AbsTTException {
        this(paramFile, new File(paramFile, EStoragePaths.DBSETTINGS.getFile().getName()));
    }

    /**
     * Constructor with all possible properties.
     * 
     * @param paramFile
     *            the path to the database
     * @param paramProps
     *            properties to be set for setting
     * @throws TTUsageException
     *             if properties are not valid
     */
    public DatabaseConfiguration(final File paramFile, final Properties paramProps) throws TTUsageException {
        mFile = paramFile;
        mProps = new Properties();
        buildUpProperties(paramProps);

    }

    /**
     * Constructor with all possible properties stored in a file.
     * 
     * @param paramFile
     *            the path to the database
     * @param paramProp
     *            properties to be set
     * @throws AbsTTException
     *             if the reading of the props is failing or properties are not
     *             valid
     */
    public DatabaseConfiguration(final File paramFile, final File paramProp) throws AbsTTException {
        mFile = paramFile;
        mProps = new Properties();
        final Properties loadProps = new Properties();

        try {
            if (!paramProp.exists() || paramProp.length() == 0) {
                buildUpProperties(mProps);
            } else {
                loadProps.load(new FileInputStream(paramProp));
                buildUpProperties(loadProps);

                // Check if property file comes from external
                if ((paramProp.getName().equals(EStoragePaths.DBSETTINGS.getFile().getName()) && paramProp
                    .getParentFile().equals(paramFile))
                    // and check if the loaded checksum is valid
                    && !loadProps.getProperty(EDatabaseSetting.CHECKSUM.name()).equals(
                        Integer.toString(hashCode()))) {
                    throw new TTUsageException("Checksums differ: Loaded", getProps().toString(),
                        "and expected", toString());

                }
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    /**
     * Building up the properties and replacing all missing with the values from
     * the standard one.
     * 
     * @param props
     *            to be included
     * @throws TTUsageException
     *             if wrong properties are into existing database
     */
    private void buildUpProperties(final Properties props) throws TTUsageException {
        for (final EDatabaseSetting enumProps : EDatabaseSetting.values()) {
            if (enumProps != EDatabaseSetting.CHECKSUM) {
                if (props.containsKey(enumProps.name())) {
                    getProps().setProperty(enumProps.name(), props.getProperty(enumProps.name()));
                } else {
                    getProps().setProperty(enumProps.name(), enumProps.getStandardProperty());
                }
            }
        }

    }

    /**
     * Getter for the properties. The values are refered over {@link EDatabaseSetting}.
     * 
     * @return the properties
     */
    public final Properties getProps() {
        return mProps;
    }

    /**
     * Get tnk folder.
     * 
     * @return Path to tnk folder.
     */
    public final File getFile() {
        return mFile;
    }

    /**
     * Serializing the data.
     * 
     * @return test if serializing the properties was successful
     */
    public final boolean serialize() {
        try {
            final Integer hashCode = hashCode();
            getProps().setProperty(EDatabaseSetting.CHECKSUM.name(), Integer.toString(hashCode));
            getProps().store(
                new FileOutputStream(new File(mFile, EStoragePaths.DBSETTINGS.getFile().getName())), "");
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return mProps.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object paramObj) {
        return mProps.equals(paramObj);
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        return new StringBuilder(mProps.toString()).toString();
    }

}
