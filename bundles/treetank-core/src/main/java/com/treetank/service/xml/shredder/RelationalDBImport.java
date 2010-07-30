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

package com.treetank.service.xml.shredder;

import java.sql.*;

import com.treetank.utils.LogWrapper;
import org.slf4j.LoggerFactory;
/**
 * <h1>RelationalDBImport</h1>
 * 
 * <p>
 * Import temporal data from a relational database like PostgreSQL, Oracle or MySQL.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class RelationalDBImport implements IImport {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(RelationalDBImport.class));
    
    /** Driver class string. */
    private final String mDriverClass;

    /** Connection URL. */
    private final String mConnURL;

    /** Username for database access. */
    private final String mUserName;

    /** Password for database access. */
    private final String mUserPass;

    /** Database connection. */
    private transient Connection mConnection;

    /**
     * Constructor.
     * 
     * @param mDriverClass
     *            Driver class used for specific database driver.
     * @param mConnURL
     *            URL to connect to.
     * @param mUserName
     *            Username credential.
     * @param mUserPass
     *            Password credential.
     */
    public RelationalDBImport(final String mDriverClass, final String mConnURL, final String mUserName,
        final String mUserPass) {
        this.mDriverClass = mDriverClass;
        this.mConnURL = mConnURL;
        this.mUserName = mUserName;
        this.mUserPass = mUserPass;
        try {
            Class.forName(this.mDriverClass).newInstance();
            mConnection = DriverManager.getConnection(this.mConnURL, this.mUserName, this.mUserPass);
        } catch (final InstantiationException e) {
            LOGWRAPPER.error(e);
        } catch (final IllegalAccessException e) {
            LOGWRAPPER.error(e);
        } catch (final ClassNotFoundException e) {
            LOGWRAPPER.error(e);
        } catch (final SQLException e) {
            LOGWRAPPER.error(e);
        }
    }

    @Override
    public void check(final Object mDatabase, final Object mObj) {
        try {
            final PreparedStatement prepStatement = mConnection.prepareStatement((String)mObj);
            final ResultSet result = prepStatement.executeQuery();

            while (result.next()) {
            }

            prepStatement.close();
            mConnection.close();
        } catch (final SQLException e) {
            LOGWRAPPER.error(e);
        } finally {
            try {
                mConnection.close();
            } catch (final SQLException e) {
                LOGWRAPPER.error(e);
            }
        }
    }

}
