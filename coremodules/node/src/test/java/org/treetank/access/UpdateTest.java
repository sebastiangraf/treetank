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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.DocumentCreater;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.INodeWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.IConstants;
import org.treetank.node.interfaces.IStructNode;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class UpdateTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        Properties props = new Properties();
        props.put(IConstants.DBFILE, TestHelper.PATHS.PATH1.getFile());
        props.put(IConstants.RESOURCE, TestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props, 10);
        NodeHelper.createTestDocument(mResource);
        holder = Holder.generateWtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testNodeTransactionIsolation() throws TTException {

        INodeReadTrx rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));
        INodeWriteTrx wtx = holder.getNWtx();
        wtx.insertElementAsFirstChild(new QName(""));
        nodeIsolation(rtx);
        wtx.commit();
        nodeIsolation(rtx);
        wtx.moveTo(((IStructNode)wtx.getNode()).getFirstChildKey());
        wtx.insertElementAsFirstChild(new QName(""));
        nodeIsolation(rtx);
        wtx.commit();
        nodeIsolation(rtx);
        rtx.close();
        wtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testNodeTransactionIsolation()} for
     * having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws TTException
     */
    private final static void nodeIsolation(final INodeReadTrx pRtx) throws TTException {
        assertTrue(pRtx.moveTo(ROOT_NODE));
        assertEquals(0, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getFirstChildKey()));
        assertEquals(1, pRtx.getNode().getNodeKey());
        assertEquals(5, ((IStructNode)pRtx.getNode()).getChildCount());
    }

    @Test
    public void testInsertChild() throws TTException {

        INodeWriteTrx wtx = holder.getNWtx();
        NodeHelper.createDocumentRootNode(wtx);
        wtx.commit();
        wtx.close();

        INodeReadTrx rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));

        assertEquals(1L, holder.getPRtx().getActualRevisionRootPage().getRevision());

        // Insert 100 children.
        for (int i = 1; i <= 10; i++) {
            wtx =
                new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                    HashKind.Rolling);

            wtx.moveTo(ROOT_NODE);
            wtx.insertTextAsFirstChild(Integer.toString(i));
            wtx.commit();
            wtx.close();

            rtx =
                new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                    holder.getSession().getMostRecentVersion()));
            rtx.moveTo(ROOT_NODE);
            rtx.moveTo(((IStructNode)rtx.getNode()).getFirstChildKey());
            assertEquals(Integer.toString(i), rtx.getValueOfCurrentNode());
            assertEquals(i + 1, holder.getSession().getMostRecentVersion());
            rtx.close();
        }

        rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));
        rtx.moveTo(ROOT_NODE);
        rtx.moveTo(((IStructNode)rtx.getNode()).getFirstChildKey());
        assertEquals("10", rtx.getValueOfCurrentNode());
        assertEquals(11L, holder.getSession().getMostRecentVersion());
        rtx.close();

    }

    @Test
    public void testInsertPath() throws TTException {
        INodeWriteTrx wtx = holder.getNWtx();
        wtx.commit();
        wtx.close();

        wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        NodeHelper.createDocumentRootNode(wtx);
        wtx.moveTo(ROOT_NODE);
        assertEquals(15L, wtx.insertElementAsFirstChild(new QName("")));
        assertEquals(16L, wtx.insertElementAsFirstChild(new QName("")));
        assertEquals(17L, wtx.insertElementAsFirstChild(new QName("")));
        assertTrue(wtx.moveTo(wtx.getNode().getParentKey()));
        assertEquals(18L, wtx.insertElementAsRightSibling(new QName("")));
        wtx.commit();
        wtx.close();

        wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        assertTrue(wtx.moveTo(ROOT_NODE));
        assertEquals(19L, wtx.insertElementAsFirstChild(new QName("")));
        wtx.commit();
        wtx.close();

    }

    @Test
    public void testPageBoundary() throws TTException {
        INodeWriteTrx wtx = holder.getNWtx();
        NodeHelper.createDocumentRootNode(wtx);

        // Document root.
        wtx.insertElementAsFirstChild(new QName(""));
        for (int i = 0; i < 256 * 256 + 1; i++) {
            // wtx.insertTextAsRightSibling("");
            wtx.insertElementAsRightSibling(new QName(""));
        }

        pageBoundary(wtx);
        wtx.commit();
        pageBoundary(wtx);
        wtx.close();
        final INodeReadTrx rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));
        pageBoundary(rtx);
        rtx.close();

    }

    /**
     * Testmethod for {@link UpdateTest#testPageBoundary()} for having different
     * rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws TTException
     */
    private final static void pageBoundary(final INodeReadTrx pRtx) throws TTException {
        assertTrue(pRtx.moveTo(2L));
        assertEquals(2L, pRtx.getNode().getNodeKey());
    }

    @Test(expectedExceptions = TTUsageException.class)
    public void testRemoveDocument() throws TTException {
        final INodeWriteTrx wtx = holder.getNWtx();
        DocumentCreater.create(wtx);
        wtx.moveTo(ROOT_NODE);
        try {
            wtx.remove();
        } finally {
            wtx.abort();
            wtx.close();
        }

    }

    @Test
    public void testRemoveDescendant() throws TTException {
        final INodeWriteTrx wtx = holder.getNWtx();
        wtx.moveTo(5L);
        wtx.remove();
        removeDescendant(wtx);
        wtx.commit();
        removeDescendant(wtx);
        wtx.close();
        final INodeReadTrx rtx =
            new NodeReadTrx(holder.getSession().beginPageReadTransaction(
                holder.getSession().getMostRecentVersion()));
        removeDescendant(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testRemoveDescendant()} for having
     * different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws TTException
     */
    private final static void removeDescendant(final INodeReadTrx pRtx) throws TTException {
        assertTrue(pRtx.moveTo(ROOT_NODE));
        assertEquals(0, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getFirstChildKey()));
        assertEquals(1, pRtx.getNode().getNodeKey());
        assertEquals(4, ((IStructNode)pRtx.getNode()).getChildCount());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getFirstChildKey()));
        assertEquals(4, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getRightSiblingKey()));
        assertEquals(8, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getRightSiblingKey()));
        assertEquals(9, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveTo(((IStructNode)pRtx.getNode()).getRightSiblingKey()));
        assertEquals(13, pRtx.getNode().getNodeKey());
    }

}
