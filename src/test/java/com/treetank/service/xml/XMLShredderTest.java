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
 * $Id: XMLShredderTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.service.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.IConstants;
import com.treetank.utils.TypedValue;

public class XMLShredderTest {

    public static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "test.xml";

    public static final String XML2 = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "test2.xml";

    public static final String XML3 = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "test3.xml";

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testSTAXShredder() {
        try {
            // Setup expected session.
            final ISession expectedSession = Session
                    .beginSession(ITestConstants.PATH1);
            final IWriteTransaction expectedTrx = expectedSession
                    .beginWriteTransaction();
            DocumentCreater.create(expectedTrx);
            expectedTrx.commit();

            // Setup parsed session.
            XMLShredder.shred(XML, new SessionConfiguration(
                    ITestConstants.PATH2));

            // Verify.
            final ISession session = Session.beginSession(ITestConstants.PATH2);
            final IReadTransaction rtx = session.beginReadTransaction();
            rtx.moveToDocumentRoot();
            final Iterator<Long> expectedDescendants = new DescendantAxis(
                    expectedTrx);
            final Iterator<Long> descendants = new DescendantAxis(rtx);

            assertEquals(expectedTrx.getNodeCount(), expectedTrx.getNodeCount());
            while (expectedDescendants.hasNext() && descendants.hasNext()) {
                assertEquals(expectedTrx.getNode().getNodeKey(), rtx.getNode()
                        .getNodeKey());
                assertEquals(expectedTrx.getNode().getParentKey(), rtx
                        .getNode().getParentKey());
                assertEquals(expectedTrx.getNode().getFirstChildKey(), rtx
                        .getNode().getFirstChildKey());
                assertEquals(expectedTrx.getNode().getLeftSiblingKey(), rtx
                        .getNode().getLeftSiblingKey());
                assertEquals(expectedTrx.getNode().getRightSiblingKey(), rtx
                        .getNode().getRightSiblingKey());
                assertEquals(expectedTrx.getNode().getChildCount(), rtx
                        .getNode().getChildCount());
                assertEquals(expectedTrx.getNode().getAttributeCount(), rtx
                        .getNode().getAttributeCount());
                assertEquals(expectedTrx.getNode().getNamespaceCount(), rtx
                        .getNode().getNamespaceCount());
                assertEquals(expectedTrx.getNode().getKind(), rtx.getNode()
                        .getKind());
                assertEquals(expectedTrx.nameForKey(expectedTrx.getNode()
                        .getNameKey()), rtx.nameForKey(rtx.getNode()
                        .getNameKey()));
                assertEquals(expectedTrx.nameForKey(expectedTrx.getNode()
                        .getURIKey()), rtx
                        .nameForKey(rtx.getNode().getURIKey()));
                if (expectedTrx.getNode().isText()) {
                    assertEquals(new String(
                            expectedTrx.getNode().getRawValue(),
                            IConstants.DEFAULT_ENCODING), new String(rtx
                            .getNode().getRawValue(),
                            IConstants.DEFAULT_ENCODING));
                }
            }

            expectedTrx.close();
            expectedSession.close();
            rtx.close();
            session.close();
        } catch (Exception e) {
            fail();
        }

    }

    // @Test
    // public void testShredIntoExisting() throws IOException,
    // XMLStreamException {
    // try {
    // XMLShredder.shred(XML, new SessionConfiguration(PATH));
    // TestCase.fail();
    // } catch (Exception e) {
    // // Must fail.
    // }
    // }

    @Test
    public void testAttributesNSPrefix() {
        try {
            // Setup expected session.
            final ISession expectedSession2 = Session
                    .beginSession(ITestConstants.PATH1);
            final IWriteTransaction expectedTrx2 = expectedSession2
                    .beginWriteTransaction();
            DocumentCreater.createWithoutNamespace(expectedTrx2);
            expectedTrx2.commit();

            // Setup parsed session.
            XMLShredder.shred(XML2, new SessionConfiguration(
                    ITestConstants.PATH2));

            // Verify.
            final ISession session = Session.beginSession(ITestConstants.PATH2);
            final IReadTransaction rtx = session.beginReadTransaction();
            rtx.moveToDocumentRoot();
            final Iterator<Long> expectedAttributes = new DescendantAxis(
                    expectedTrx2);
            final Iterator<Long> attributes = new DescendantAxis(rtx);

            assertEquals(expectedTrx2.getNodeCount(), rtx.getNodeCount());
            while (expectedAttributes.hasNext() && attributes.hasNext()) {
                assertEquals(expectedTrx2.getNode().getNamespaceCount(), rtx
                        .getNode().getNamespaceCount());
                assertEquals(expectedTrx2.getNode().getAttributeCount(), rtx
                        .getNode().getAttributeCount());
                for (int i = 0; i < expectedTrx2.getNode().getAttributeCount(); i++) {
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode()
                            .getNameKey()), rtx.nameForKey(rtx.getNode()
                            .getNameKey()));
                    assertEquals(expectedTrx2.getNode().getNameKey(), rtx
                            .getNode().getNameKey());
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode()
                            .getURIKey()), rtx.nameForKey(rtx.getNode()
                            .getURIKey()));

                }
            }

            expectedTrx2.close();
            expectedSession2.close();
            rtx.close();
            session.close();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testShreddingLargeText() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH2);

            final InputStream in = new FileInputStream(XML3);
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            final XMLStreamReader parser = factory.createXMLStreamReader(in);

            XMLShredder.shred(0, parser, session);

            final IReadTransaction rtx = session.beginReadTransaction();
            assertTrue(rtx.moveToFirstChild());
            assertTrue(rtx.moveToFirstChild());

            final StringBuilder tnkBuilder = new StringBuilder();
            do {
                tnkBuilder.append(TypedValue.parseString(rtx.getNode()
                        .getRawValue()));
            } while (rtx.moveToRightSibling());

            final String tnkString = tnkBuilder.toString();

            rtx.close();
            session.close();

            final InputStream in2 = new FileInputStream(XML3);
            final XMLStreamReader validater = factory
                    .createXMLStreamReader(in2);
            final StringBuilder xmlBuilder = new StringBuilder();
            while (validater.hasNext()) {
                switch (validater.next()) {
                case XMLStreamConstants.CHARACTERS:
                    final String text = validater.getText().trim();
                    if (text.length() > 0) {
                        xmlBuilder.append(text);
                    }
                    break;
                }
            }

            assertEquals(xmlBuilder.toString(), tnkString);
        } catch (Exception e) {
            fail();
        }
    }

}
