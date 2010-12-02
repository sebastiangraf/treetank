/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * $Id: XMLSerializerTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

package com.treetank.service.xml.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XMLSerializerTest {

    /**
     * The resource name.
     */
    private static final transient String RESOURCENAME = "books";

    /**
     * The path where the databases will be stored.
     */
    private final static transient String STOREDBPATH =
        File.separatorChar + "tmp" + File.separatorChar + "tt";

    /**
     * This field the begin result element of a XQuery or XPath expression.
     */
    private static transient String beginResult = "<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">";

    /**
     * This field the end result element of a XQuery or XPath expression.
     */
    private static transient String endResult = "</jaxrx:result>";

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testXMLSerializer() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializer serializer = new XMLSerializerBuilder(session, out).build();
        serializer.call();
        assertEquals(DocumentCreater.XML, out.toString());
        session.close();
        database.close();
    }

    @Test
    public void testRestSerializer() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(session, out);
        builder.setREST(true);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.REST, out.toString());

        session.close();
        database.close();
    }

    @Test
    public void testIDSerializer() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(session, out);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.ID, out.toString());
        session.close();
        database.close();
    }

    @Test
    public void testSampleCompleteSerializer() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // generate serialize all from this session
        DocumentCreater.createVersioned(wtx);
        wtx.commit();
        wtx.close();

        XMLSerializer serializerall = new XMLSerializerBuilder(session, out, -1).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        out.reset();

        serializerall = new XMLSerializerBuilder(session, out, 0, 1, 2).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        session.close();
        database.close();
    }

    /**
     * This test check the XPath //books expression and expects 6 books as result. But the failure is, that
     * only
     * the children of the books will be serialized and NOT the book node itself.
     */
    @Test
    @Ignore
    public void testKeyStart() throws Exception {

        final File dir = new File(STOREDBPATH);
        deleteDirectory(dir);
        final InputStream xmlInput = XMLSerializerTest.class.getResourceAsStream("/books.xml");
        shred(xmlInput, RESOURCENAME);
        xmlInput.close();
        String xPath = "//book";
        OutputStream output = new ByteArrayOutputStream();
        output.write(beginResult.getBytes());

        IDatabase database = null;
        ISession session = null;
        IReadTransaction rtx = null;
        try {
            database = Database.openDatabase(new File(STOREDBPATH + File.separatorChar + "books.tnk"));
            // Creating a new session
            session = database.getSession();
            // Creating a transaction
            rtx = session.beginReadTransaction();

            final IAxis axis = new XPathAxis(rtx, xPath);
            for (final long key : axis) {
                if (key >= 0) {
                    final XMLSerializerProperties props = new XMLSerializerProperties();
                    final XMLSerializerBuilder builder =
                        new XMLSerializerBuilder(session, key, output, props);

                    builder.setREST(true);
                    builder.setID(true);
                    builder.setDeclaration(false);
                    final XMLSerializer serializer = builder.build();
                    serializer.call();
                } else {
                    output.write(rtx.getNode().getRawValue());
                }
            }

        } catch (final TreetankException ttExcep) {
            ttExcep.printStackTrace();
        } catch (final IOException ioExcep) {
            ioExcep.printStackTrace();
        } catch (final Exception globExcep) {
            globExcep.printStackTrace();
        } finally {
            rtx.moveToDocumentRoot();
            synchronized (database) {
                database.close();
            }
        }

        output.write(endResult.getBytes());

        InputStream xmlResult = new ByteArrayInputStream(((ByteArrayOutputStream)output).toByteArray());
        Document resultDoc = xmlDocument(xmlResult);
        final NodeList bNodes = resultDoc.getElementsByTagName("book");
        assertEquals("test for items size of books is 6", 6, bNodes.getLength());

    }

    /**
     * This method deletes a not empty directory.
     * 
     * @param path
     *            The director that has to be deleted.
     * @return <code>true</code> if the deletion process has been successful. <code>false</code> otherwise.
     */
    private static boolean deleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }

    /**
     * This method creates of an input stream an XML document.
     * 
     * @param input
     *            The input stream.
     * @return The packed XML document.
     * @throws SAXException
     *             Exception occurred.
     * @throws IOException
     *             Exception occurred.
     * @throws ParserConfigurationException
     *             Exception occurred.
     */
    private Document xmlDocument(final InputStream input) throws SAXException, IOException,
        ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
    }

    /**
     * This method is responsible to save the XML file, which is in an {@link InputStream}, as a TreeTank
     * object.
     * 
     * @param xmlInput
     *            The XML file in an {@link InputStream}.
     * @param resource
     *            The name of the resource.
     * @return <code>true</code> when the shredding process has been successful. <code>false</code> otherwise.
     * @throws TreetankException
     */
    public final boolean shred(final InputStream xmlInput, final String resource) throws TreetankException {
        boolean allOk = false;
        IWriteTransaction wtx = null;
        IDatabase database = null;
        ISession session = null;
        boolean abort = false;
        try {
            final StringBuilder tnkFileName = new StringBuilder(STOREDBPATH + File.separatorChar + resource);
            tnkFileName.append(".tnk");
            final File tnk = new File(tnkFileName.toString());

            // Shredding the database to the file as XML
            final Properties dbProps = new Properties();
            dbProps.setProperty(EDatabaseSetting.REVISION_TO_RESTORE.name(), "1");
            final DatabaseConfiguration conf = new DatabaseConfiguration(tnk, dbProps);

            Database.createDatabase(conf);
            database = Database.openDatabase(tnk);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            wtx.moveToDocumentRoot();
            final boolean exist = wtx.moveToFirstChild();
            if (exist) {
                wtx.remove();
                wtx.commit();
            }
            final XMLShredder shredder = new XMLShredder(wtx, createReader(xmlInput), true);
            shredder.call();
            allOk = true;
        } catch (final Exception exce) {
            abort = true;
        } finally {
            synchronized (database) {
                if (abort) {
                    wtx.abort();
                }
                database.close();
            }
        }
        return allOk;
    }

    /**
     * This method creates an {@link XMLEventReader} out of an {@link InputStream}.
     * 
     * @param inputStream
     *            The {@link InputStream} containing the XML file that has to be
     *            stored.
     * @return The {@link XMLStreamReader} object.
     * @throws IOException
     *             The exception occurred.
     * @throws XMLStreamException
     *             The exception occurred.
     */
    private XMLEventReader createReader(final InputStream inputStream) throws IOException, XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory.createXMLEventReader(inputStream);
    }

}
