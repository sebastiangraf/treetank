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
package com.treetank.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.treetank.exception.AbsTTException;
import com.treetank.exception.TTIOException;
import com.treetank.exception.TTUsageException;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EStoragePaths;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

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
    public DatabaseConfiguration(final File paramFile, final Properties paramProps)
        throws TTUsageException {
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
