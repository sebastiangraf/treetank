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

package org.treetank.service.xml.shredder;

import static org.treetank.node.IConstants.ROOT_NODE;

import java.io.File;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.custommonkey.xmlunit.XMLTestCase;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.NodeReadTrx;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.IDatabase;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.ISession;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.TTException;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.service.xml.DocumentCreater;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class XMLShredderTest extends XMLTestCase {

    public static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test.xml";

    public static final String XML2 = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test2.xml";

    public static final String XML3 = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test3.xml";

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        mResource = mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10);
        holder = Holder.generateWtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testSTAXShredder() throws Exception {

        // Setup parsed session.
        XMLShredder.main(XML, PATHS.PATH2.getFile().getAbsolutePath());
        final INodeWriteTrx expectedTrx = holder.getNWtx();
        NodeHelper.createDocumentRootNode(expectedTrx);

        // Verify.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        database2.createResource(mResourceConfig.create(PATHS.PATH2.getFile(), "shredded", 1));
        final ISession session =
            database2.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));
        final INodeReadTrx rtx =
            new NodeReadTrx(session.beginPageReadTransaction(session.getMostRecentVersion()));
        rtx.moveTo(ROOT_NODE);
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);
        final Iterator<Long> descendants = new DescendantAxis(rtx);

        while (expectedDescendants.hasNext() && descendants.hasNext()) {
            final IStructNode expDesc = ((IStructNode)expectedTrx.getNode());
            final IStructNode desc = ((IStructNode)rtx.getNode());
            AssertJUnit.assertEquals(expDesc.getNodeKey(), desc.getNodeKey());
            AssertJUnit.assertEquals(expDesc.getParentKey(), desc.getParentKey());
            AssertJUnit.assertEquals(expDesc.getFirstChildKey(), desc.getFirstChildKey());
            AssertJUnit.assertEquals(expDesc.getLeftSiblingKey(), desc.getLeftSiblingKey());
            AssertJUnit.assertEquals(expDesc.getRightSiblingKey(), desc.getRightSiblingKey());
            AssertJUnit.assertEquals(expDesc.getChildCount(), desc.getChildCount());
            if (expDesc.getKind() == IConstants.ELEMENT || desc.getKind() == IConstants.ELEMENT) {

                AssertJUnit.assertEquals(((ElementNode)expDesc).getAttributeCount(), ((ElementNode)desc)
                    .getAttributeCount());
                AssertJUnit.assertEquals(((ElementNode)expDesc).getNamespaceCount(), ((ElementNode)desc)
                    .getNamespaceCount());
            }
            AssertJUnit.assertEquals(expDesc.getKind(), desc.getKind());
            AssertJUnit.assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
            AssertJUnit
                .assertEquals(expectedTrx.getValueOfCurrentNode(), expectedTrx.getValueOfCurrentNode());
        }

        rtx.close();
        session.close();
    }

    @Test
    public void testShredIntoExisting() throws Exception {

        final INodeWriteTrx wtx = holder.getNWtx();
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createFileReader(new File(XML)), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.moveTo(ROOT_NODE);
        wtx.moveTo(((IStructNode)wtx.getNode()).getFirstChildKey());

        final XMLShredder shredder2 =
            new XMLShredder(wtx, XMLShredder.createFileReader(new File(XML)),
                EShredderInsert.ADDASRIGHTSIBLING);
        shredder2.call();

        // Setup expected session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession expectedSession =
            database2.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));

        final INodeWriteTrx expectedTrx =
            new NodeWriteTrx(expectedSession, expectedSession.beginPageWriteTransaction(), HashKind.Rolling);
        org.treetank.DocumentCreater.create(expectedTrx);
        expectedTrx.commit();
        expectedTrx.moveTo(ROOT_NODE);

        // Verify.
        final INodeReadTrx rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));

        final Iterator<Long> descendants = new DescendantAxis(rtx);
        final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);

        while (expectedDescendants.hasNext()) {
            expectedDescendants.next();
            descendants.hasNext();
            descendants.next();
            AssertJUnit.assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

        expectedTrx.moveTo(ROOT_NODE);
        final Iterator<Long> expectedDescendants2 = new DescendantAxis(expectedTrx);
        while (expectedDescendants2.hasNext()) {
            expectedDescendants2.next();
            descendants.hasNext();
            descendants.next();
            AssertJUnit.assertEquals(expectedTrx.getQNameOfCurrentNode(), rtx.getQNameOfCurrentNode());
        }

    }

    @Test
    public void testAttributesNSPrefix() throws Exception {
        // Setup expected session.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        database.createResource(mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(),
            TestHelper.RESOURCENAME, 1));
        final ISession expectedSession2 =
            database.getSession(new SessionConfiguration(TestHelper.RESOURCENAME, StandardSettings.KEY));
        final INodeWriteTrx expectedTrx2 =
            new NodeWriteTrx(expectedSession2, expectedSession2.beginPageWriteTransaction(), HashKind.Rolling);
        DocumentCreater.createWithoutNamespace(expectedTrx2);
        expectedTrx2.commit();

        // Setup parsed session.
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session2 =
            database2.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));
        final INodeWriteTrx wtx =
            new NodeWriteTrx(session2, session2.beginPageWriteTransaction(), HashKind.Rolling);
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createFileReader(new File(XML2)),
                EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.commit();

        // Verify.
        final INodeReadTrx rtx =
            new NodeReadTrx(session2.beginPageReadTransaction(session2.getMostRecentVersion()));
        rtx.moveTo(ROOT_NODE);
        final Iterator<Long> expectedAttributes = new DescendantAxis(expectedTrx2);
        final Iterator<Long> attributes = new DescendantAxis(rtx);

        while (expectedAttributes.hasNext() && attributes.hasNext()) {
            if (expectedTrx2.getNode().getKind() == IConstants.ELEMENT
                || rtx.getNode().getKind() == IConstants.ELEMENT) {
                AssertJUnit.assertEquals(((ElementNode)expectedTrx2.getNode()).getNamespaceCount(),
                    ((ElementNode)rtx.getNode()).getNamespaceCount());
                AssertJUnit.assertEquals(((ElementNode)expectedTrx2.getNode()).getAttributeCount(),
                    ((ElementNode)rtx.getNode()).getAttributeCount());
                for (int i = 0; i < ((ElementNode)expectedTrx2.getNode()).getAttributeCount(); i++) {
                    AssertJUnit.assertEquals(expectedTrx2.getQNameOfCurrentNode(), rtx
                        .getQNameOfCurrentNode());
                }
            }
        }

        AssertJUnit.assertEquals(expectedAttributes.hasNext(), attributes.hasNext());
    }

    @Test
    public void testShreddingLargeText() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH2.getFile());
        final ISession session =
            database.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));
        final INodeWriteTrx wtx =
            new NodeWriteTrx(session, session.beginPageWriteTransaction(), HashKind.Rolling);
        final XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createFileReader(new File(XML3)),
                EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();

        final INodeReadTrx rtx =
            new NodeReadTrx(session.beginPageReadTransaction(session.getMostRecentVersion()));
        AssertJUnit.assertTrue(rtx.moveTo(((IStructNode)rtx.getNode()).getFirstChildKey()));
        AssertJUnit.assertTrue(rtx.moveTo(((IStructNode)rtx.getNode()).getFirstChildKey()));

        final StringBuilder tnkBuilder = new StringBuilder();
        do {
            tnkBuilder.append(rtx.getValueOfCurrentNode());
        } while (rtx.moveTo(((IStructNode)rtx.getNode()).getRightSiblingKey()));

        final String tnkString = tnkBuilder.toString();

        rtx.close();
        session.close();

        final XMLEventReader validater = XMLShredder.createFileReader(new File(XML3));
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

        AssertJUnit.assertEquals(xmlBuilder.toString(), tnkString);
    }
}
