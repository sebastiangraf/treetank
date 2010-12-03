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

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.treetank.utils.LogWrapper;

// import net.sf.saxon.s9api.DocumentBuilder;
// import net.sf.saxon.s9api.Processor;
// import net.sf.saxon.s9api.SaxonApiException;
// import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
// import net.sf.saxon.s9api.XPathCompiler;
// import net.sf.saxon.s9api.XPathSelector;
// import net.sf.saxon.s9api.XdmItem;
// import net.sf.saxon.s9api.XdmNode;

import org.slf4j.LoggerFactory;

/**
 * <h1>RelationalDBImport</h1>
 * 
 * <p>
 * Import temporal data from a relational database like PostgreSQL, Oracle or MySQL. Some order must be
 * specified (ORDER BY clause has to be used inside the SQL statement).
 * </p>
 * 
 * <p>
 * Test environment setup before using this class:
 * <code><pre>ssh -L 3333:localhost:5555 johannes@xen4.disy.inf.uni-konstanz.de</pre></code>
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class RelationalDBImport implements IImport<Object> {

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

    /** IANA IPv4 address space Registry. */
    private static final String IANA_IPV4 =
        "http://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml";

    /**
     * Constructor.
     * 
     * @param paramDriverClass
     *            Driver class used for specific database driver.
     * @param paramConnURL
     *            URL to connect to.
     * @param paramUserName
     *            Username credential.
     * @param paramUserPass
     *            Password credential.
     */
    public RelationalDBImport(final String paramDriverClass, final String paramConnURL,
        final String paramUserName, final String paramUserPass) {
        mDriverClass = paramDriverClass;
        mConnURL = paramConnURL;
        mUserName = paramUserName;
        mUserPass = paramUserPass;
        try {
            Class.forName(mDriverClass).newInstance();
            mConnection = DriverManager.getConnection(mConnURL, mUserName, mUserPass);
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
    public void importData(final char paramDateRange, final List<Object> mObj) {
        // try {
        // final PreparedStatement prepStatement = mConnection.prepareStatement((String) mObj);
        // final ResultSet result = prepStatement.executeQuery();
        // String tmpTimestamp;
        //
        // // Initial temporal timestamp.
        // if (result.next()) {
        // tmpTimestamp = result.getString("timestamp");
        // } else {
        // throw new IllegalStateException("No result!");
        // }
        //
        // int i = 0;
        // File resFile = new File("target" + File.separator + "result" + i);
        // final List<NDataTuple> dataList = new ArrayList<NDataTuple>();
        //
        // final Processor proc = new Processor(false);
        // final XPathCompiler xpath = proc.newXPathCompiler();
        //
        // final DocumentBuilder builder = proc.newDocumentBuilder();
        // builder.setLineNumbering(true);
        // builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
        // final XdmNode booksDoc = builder.build(new StreamSource(IANA_IPV4));
        //
        // // Iterate over result set.
        // while (result.next()) {
        // final String timestamp = result.getString("timestamp");
        //
        // // Write into a new file if timestamp changes.
        // if (tmpTimestamp != timestamp) {
        // resFile = new File("target" + File.separator + "result" + ++i);
        // }
        //
        // final String srcIP = result.getString("srcaddr");
        // String prefix = srcIP.split(".")[0];
        //
        // if (prefix.toCharArray()[0] != '0') {
        // switch (prefix.length()) {
        // case 1:
        // prefix = "00" + prefix;
        // break;
        // case 2:
        // prefix = "0" + prefix;
        // break;
        // case 3:
        // break;
        // default:
        // throw new IllegalStateException(
        // "IPv4 address prefixes don't have more than 3 digits.");
        // }
        // }
        //
        // final XPathSelector selector = xpath.compile("//record[./prefix/" + prefix + "]/").load();
        // selector.setContextItem(booksDoc);
        //
        // for (final XdmItem item : selector) {
        //
        // }
        // }
        //
        // prepStatement.close();
        // mConnection.close();
        // } catch (final SQLException e) {
        // LOGWRAPPER.error(e);
        // } catch (SaxonApiException e) {
        // LOGWRAPPER.error(e);
        // } finally {
        // try {
        // mConnection.close();
        // } catch (final SQLException e) {
        // LOGWRAPPER.error(e);
        // }
        // }
    }
}
