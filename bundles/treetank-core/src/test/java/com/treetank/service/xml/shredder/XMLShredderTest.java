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

package com.treetank.service.xml.shredder;

import java.io.File;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.IConstants;
import com.treetank.utils.TypedValue;

public class XMLShredderTest extends XMLTestCase {

    public static final String XML =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.xml";

    public static final String XML2 =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test2.xml";

    public static final String XML3 =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test3.xml";

    @Override
    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @Override
    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testSTAXShredder() throws Exception {
        // Setup expected session.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());

        final ISession expectedSession = database.getSession();
        final IWriteTransaction expectedTrx = expectedSession.beginWriteTransaction();
        DocumentCreater.create(expectedTrx);
        expectedTrx.commit();

        // Setup parsed session.
        XMLShredder.main(XML, PATHS.PATH2.getFile().getAbsolutePath());

        // Verify.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session = database2.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);
        final Iterator<Long> descendants = new DescendantAxis(rtx);

        while (expectedDescendants.hasNext() && descendants.hasNext()) {
            assertEquals(expectedTrx.getNode().getNodeKey(), rtx.getNode().getNodeKey());
            assertEquals(expectedTrx.getNode().getParentKey(), rtx.getNode().getParentKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getFirstChildKey(), ((AbsStructNode)rtx
                .getNode()).getFirstChildKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getLeftSiblingKey(), ((AbsStructNode)rtx
                .getNode()).getLeftSiblingKey());
            assertEquals(((AbsStructNode)expectedTrx.getNode()).getRightSiblingKey(), ((AbsStructNode)rtx
                .getNode()).getRightSiblingKey());

            if (expectedTrx.getNode().getKind() == ENodes.ELEMENT_KIND
                || rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                assertEquals(((ElementNode)expectedTrx.getNode()).getChildCount(), ((ElementNode)rtx
                    .getNode()).getChildCount());
                assertEquals(((ElementNode)expectedTrx.getNode()).getAttributeCount(), ((ElementNode)rtx
                    .getNode()).getAttributeCount());
                assertEquals(((ElementNode)expectedTrx.getNode()).getNamespaceCount(), ((ElementNode)rtx
                    .getNode()).getNamespaceCount());
            }
            assertEquals(expectedTrx.getNode().getKind(), rtx.getNode().getKind());
            assertEquals(expectedTrx.nameForKey(expectedTrx.getNode().getNameKey()), rtx.nameForKey(rtx
                .getNode().getNameKey()));
            assertEquals(expectedTrx.nameForKey(expectedTrx.getNode().getURIKey()), rtx.nameForKey(rtx
                .getNode().getURIKey()));
            if (expectedTrx.getNode().getKind() == ENodes.TEXT_KIND
                || rtx.getNode().getKind() == ENodes.TEXT_KIND) {
                assertEquals(new String(expectedTrx.getNode().getRawValue(), IConstants.DEFAULT_ENCODING),
                    new String(rtx.getNode().getRawValue(), IConstants.DEFAULT_ENCODING));
            }
        }

        expectedTrx.close();
        expectedSession.close();
        rtx.close();
        session.close();
    }

    @Test
    public void testShredIntoExisting() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        assertEquals(1, wtx.getRevisionNumber());
        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        final XMLShredder shredder2 =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML)), EShredderInsert.ADDASRIGHTSIBLING);
        shredder2.call();
        assertEquals(2, wtx.getRevisionNumber());
        wtx.close();

        // Setup expected session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession expectedSession = database2.getSession();

        final IWriteTransaction expectedTrx = expectedSession.beginWriteTransaction();
        DocumentCreater.create(expectedTrx);
        expectedTrx.commit();
        expectedTrx.moveToDocumentRoot();

        // Verify.
        final IReadTransaction rtx = session.beginReadTransaction();

        final Iterator<Long> descendants = new DescendantAxis(rtx);
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);

        while (expectedDescendants.hasNext()) {
            expectedDescendants.next();
            descendants.hasNext();
            descendants.next();
            assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

        expectedTrx.moveToDocumentRoot();
        final Iterator<Long> expectedDescendants2 = new DescendantAxis(expectedTrx);
        while (expectedDescendants2.hasNext()) {
            expectedDescendants2.next();
            descendants.hasNext();
            descendants.next();
            assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

        expectedTrx.close();
        expectedSession.close();
        rtx.close();
        session.close();
        database.close();
        database2.close();

    }

    @Test
    public void testAttributesNSPrefix() throws Exception {
        // Setup expected session.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession expectedSession2 = database.getSession();
        final IWriteTransaction expectedTrx2 = expectedSession2.beginWriteTransaction();
        DocumentCreater.createWithoutNamespace(expectedTrx2);
        expectedTrx2.commit();

        // Setup parsed session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session2 = database2.getSession();
        final IWriteTransaction wtx = session2.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML2)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.commit();
        wtx.close();

        // Verify.
        final IReadTransaction rtx = session2.beginReadTransaction();
        rtx.moveToDocumentRoot();
        final Iterator<Long> expectedAttributes = new DescendantAxis(expectedTrx2);
        final Iterator<Long> attributes = new DescendantAxis(rtx);

        while (expectedAttributes.hasNext() && attributes.hasNext()) {
            if (expectedTrx2.getNode().getKind() == ENodes.ELEMENT_KIND
                || rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                assertEquals(((ElementNode)expectedTrx2.getNode()).getNamespaceCount(), ((ElementNode)rtx
                    .getNode()).getNamespaceCount());
                assertEquals(((ElementNode)expectedTrx2.getNode()).getAttributeCount(), ((ElementNode)rtx
                    .getNode()).getAttributeCount());
                for (int i = 0; i < ((ElementNode)expectedTrx2.getNode()).getAttributeCount(); i++) {
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode().getNameKey()), rtx
                        .nameForKey(rtx.getNode().getNameKey()));
                    assertEquals(expectedTrx2.getNode().getNameKey(), rtx.getNode().getNameKey());
                    assertEquals(expectedTrx2.nameForKey(expectedTrx2.getNode().getURIKey()), rtx
                        .nameForKey(rtx.getNode().getURIKey()));

                }
            }
        }

        assertEquals(expectedAttributes.hasNext(), attributes.hasNext());

        expectedTrx2.close();
        expectedSession2.close();
        rtx.close();
        session2.close();
        database.close();
        database2.close();
    }

    @Test
    public void testShreddingLargeText() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(new File(XML3)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        assertTrue(rtx.moveToFirstChild());
        assertTrue(rtx.moveToFirstChild());

        final StringBuilder tnkBuilder = new StringBuilder();
        do {
            tnkBuilder.append(TypedValue.parseString(rtx.getNode().getRawValue()));
        } while (rtx.moveToRightSibling());

        final String tnkString = tnkBuilder.toString();

        rtx.close();
        session.close();

        final XMLEventReader validater = XMLShredder.createReader(new File(XML3));
        final StringBuilder xmlBuilder = new StringBuilder();
        while (validater.hasNext()) {
            final XMLEvent event = validater.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.CHARACTERS:
                final String text = ((Characters)event).getData().trim();
                if (text.length() > 0) {
                    xmlBuilder.append(text);
                }
                break;
            }
        }

        assertEquals(xmlBuilder.toString(), tnkString);
    }
}
