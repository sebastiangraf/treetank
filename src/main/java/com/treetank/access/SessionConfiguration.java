/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: SessionConfiguration.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.ESessionSetting;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the session-wide settings that can not change.
 * 
 * The following logic applies:
 * <li>If the encryption key is null, no encryption is used.</li>
 * <li>If the checksum algorithm is null, no checksumming is used.</li>
 * </p>
 */
public final class SessionConfiguration {

    /** Props to hold all related data */
    private final Properties mProps;

    /**
     * Convenience constructor binding to .tnk folder without encryption or
     * end-to-end integrity.
     * 
     * @param path
     *            Path to .tnk folder.
     */
    public SessionConfiguration() throws TreetankUsageException {
        this(new StandardProperties().getProps());
    }

    public SessionConfiguration(final Properties props)
            throws TreetankUsageException {
        this.mProps = new Properties();
        for (final ESessionSetting enumProps : ESessionSetting.values()) {
            if (props.containsKey(enumProps.name())) {
                this.getProps().setProperty(enumProps.name(),
                        props.getProperty(enumProps.name()));
            } else {
                this.getProps().setProperty(enumProps.name(),
                        enumProps.getValue());
            }
        }

    }

    public SessionConfiguration(final File propFile) throws TreetankException {
        this(new Properties());
        try {
            getProps().load(new FileInputStream(propFile));
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }

    }

    public Properties getProps() {
        return mProps;
    }

    private static class StandardProperties {

        private final Properties props;

        StandardProperties() {
            props = new Properties();

            for (ESessionSetting prop : ESessionSetting.values()) {
                getProps().put(prop.name(), prop.getValue());
            }
        }

        public Properties getProps() {
            return props;
        }

    }

}
