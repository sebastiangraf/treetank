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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.settings.EFixed;
import org.treetank.utils.DocumentCreater;

public class UpdateTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        holder = Holder.generateSession();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testNodeTransactionIsolation() throws AbsTTException {
        IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        wtx.insertElementAsFirstChild(new QName(""));
        testNodeTransactionIsolation(wtx);
        wtx.commit();
        testNodeTransactionIsolation(wtx);
        IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testNodeTransactionIsolation(rtx);
        wtx.moveToFirstChild();
        wtx.insertElementAsFirstChild(new QName(""));
        testNodeTransactionIsolation(rtx);
        wtx.commit();
        testNodeTransactionIsolation(rtx);
        rtx.close();
        wtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testNodeTransactionIsolation()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testNodeTransactionIsolation(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveToDocumentRoot());
        assertEquals(0, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveToFirstChild());
        assertEquals(1, pRtx.getNode().getNodeKey());
        assertEquals(0, ((IStructNode)pRtx.getNode()).getChildCount());
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getLeftSiblingKey());
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getRightSiblingKey());
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getFirstChildKey());
    }

    @Test
    public void testInsertChild() throws AbsTTException {

        IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        wtx.commit();
        wtx.close();

        IReadTransaction rtx = holder.getSession().beginReadTransaction();
        assertEquals(0L, rtx.getRevisionNumber());
        rtx.close();

        // Insert 100 children.
        for (int i = 1; i <= 10; i++) {
            wtx = holder.getSession().beginWriteTransaction();
            wtx.moveToDocumentRoot();
            wtx.insertTextAsFirstChild(Integer.toString(i));
            wtx.commit();
            wtx.close();

            rtx = holder.getSession().beginReadTransaction();
            rtx.moveToDocumentRoot();
            rtx.moveToFirstChild();
            assertEquals(Integer.toString(i), rtx.getValueOfCurrentNode());
            assertEquals(i, rtx.getRevisionNumber());
            rtx.close();
        }

        rtx = holder.getSession().beginReadTransaction();
        rtx.moveToDocumentRoot();
        rtx.moveToFirstChild();
        assertEquals("10", rtx.getValueOfCurrentNode());
        assertEquals(10L, rtx.getRevisionNumber());
        rtx.close();

    }

    @Test
    public void testInsertPath() throws AbsTTException {
        IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        wtx.commit();
        wtx.close();

        wtx = holder.getSession().beginWriteTransaction();
        assertTrue(wtx.moveToDocumentRoot());
        assertEquals(1L, wtx.insertElementAsFirstChild(new QName("")));
        assertEquals(2L, wtx.insertElementAsFirstChild(new QName("")));
        assertEquals(3L, wtx.insertElementAsFirstChild(new QName("")));
        assertTrue(wtx.moveToParent());
        assertEquals(4L, wtx.insertElementAsRightSibling(new QName("")));
        wtx.commit();
        wtx.close();

        final IWriteTransaction wtx2 = holder.getSession().beginWriteTransaction();
        assertTrue(wtx2.moveToDocumentRoot());
        assertEquals(5L, wtx2.insertElementAsFirstChild(new QName("")));
        wtx2.commit();
        wtx2.close();

    }

    @Test
    public void testPageBoundary() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();

        // Document root.
        wtx.insertElementAsFirstChild(new QName(""));
        for (int i = 0; i < 256 * 256 + 1; i++) {
            // wtx.insertTextAsRightSibling("");
            wtx.insertElementAsRightSibling(new QName(""));
        }

        testPageBoundary(wtx);
        wtx.commit();
        testPageBoundary(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testPageBoundary(rtx);
        rtx.close();

    }

    /**
     * Testmethod for {@link UpdateTest#testPageBoundary()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testPageBoundary(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveTo(2L));
        assertEquals(2L, pRtx.getNode().getNodeKey());
    }

    @Test(expected = TTUsageException.class)
    public void testRemoveDocument() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveToDocumentRoot();
        try {
            wtx.remove();
        } finally {
            wtx.abort();
            wtx.close();
        }

    }

    @Test
    public void testRemoveDescendant() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.moveTo(5L);
        wtx.remove();
        testRemoveDescendant(wtx);
        wtx.commit();
        testRemoveDescendant(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testRemoveDescendant(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testRemoveDescendant()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testRemoveDescendant(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveToDocumentRoot());
        assertEquals(0, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveToFirstChild());
        assertEquals(1, pRtx.getNode().getNodeKey());
        assertEquals(4, ((IStructNode)pRtx.getNode()).getChildCount());
        assertTrue(pRtx.moveToFirstChild());
        assertEquals(4, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveToRightSibling());
        assertEquals(8, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveToRightSibling());
        assertEquals(9, pRtx.getNode().getNodeKey());
        assertTrue(pRtx.moveToRightSibling());
        assertEquals(13, pRtx.getNode().getNodeKey());
    }

    @Test
    public void testFirstMoveToFirstChild() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(7);
        wtx.moveSubtreeToFirstChild(6);
        testFirstMoveToFirstChild(wtx);
        wtx.commit();
        testFirstMoveToFirstChild(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testFirstMoveToFirstChild(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testFirstMoveToFirstChild()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testFirstMoveToFirstChild(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveTo(4));
        assertEquals(pRtx.getValueOfCurrentNode(), "oops1");
        assertTrue(pRtx.moveTo(7));
        assertFalse(pRtx.getStructuralNode().hasLeftSibling());
        assertTrue(pRtx.getStructuralNode().hasFirstChild());
        assertTrue(pRtx.moveToFirstChild());
        assertFalse(pRtx.getStructuralNode().hasFirstChild());
        assertFalse(pRtx.getStructuralNode().hasLeftSibling());
        assertFalse(pRtx.getStructuralNode().hasRightSibling());
        assertEquals("foo", pRtx.getValueOfCurrentNode());
    }

    @Test
    public void testSecondMoveToFirstChild() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(5);
        wtx.moveSubtreeToFirstChild(4);
        testSecondMoveToFirstChild(wtx);
        wtx.commit();
        testSecondMoveToFirstChild(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testSecondMoveToFirstChild(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testThirdMoveToFirstChild()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testSecondMoveToFirstChild(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveTo(5));
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getLeftSiblingKey());
        assertEquals(4L, pRtx.getStructuralNode().getFirstChildKey());
        assertFalse(pRtx.moveTo(6));
        assertTrue(pRtx.moveTo(4));
        assertEquals("oops1foo", pRtx.getValueOfCurrentNode());
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getLeftSiblingKey());
        assertEquals(5L, pRtx.getStructuralNode().getParentKey());
        assertEquals(7L, pRtx.getStructuralNode().getRightSiblingKey());
        assertTrue(pRtx.moveTo(7));
        assertEquals(4L, pRtx.getStructuralNode().getLeftSiblingKey());
    }

    @Test
    public void testThirdMoveToFirstChild() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(5);
        wtx.moveSubtreeToFirstChild(11);
        testThirdMoveToFirstChild(wtx);
        wtx.commit();
        testThirdMoveToFirstChild(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testThirdMoveToFirstChild(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testThirdMoveToFirstChild()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testThirdMoveToFirstChild(final IReadTransaction pRtx) throws AbsTTException {
        assertTrue(pRtx.moveTo(5));
        assertEquals(11L, pRtx.getStructuralNode().getFirstChildKey());
        assertTrue(pRtx.moveTo(11));
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getLeftSiblingKey());
        assertEquals(5L, pRtx.getStructuralNode().getParentKey());
        assertEquals(6L, pRtx.getStructuralNode().getRightSiblingKey());
        assertTrue(pRtx.moveTo(6L));
        assertEquals(11L, pRtx.getStructuralNode().getLeftSiblingKey());
        assertEquals(7L, pRtx.getStructuralNode().getRightSiblingKey());
    }

    @Test(expected = TTUsageException.class)
    public void testFourthMoveToFirstChild() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(4);
        wtx.moveSubtreeToFirstChild(11);
        wtx.commit();
        wtx.close();
    }

    @Test
    public void testFirstMoveSubtreeToRightSibling() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(7);
        wtx.moveSubtreeToRightSibling(6);
        testFirstMoveSubtreeToRightSibling(wtx);
        wtx.commit();
        testFirstMoveSubtreeToRightSibling(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testFirstMoveSubtreeToRightSibling(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testFirstMoveSubtreeToRightSibling()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testFirstMoveSubtreeToRightSibling(final IReadTransaction pRtx)
        throws AbsTTException {
        assertTrue(pRtx.moveTo(7));
        assertFalse(pRtx.getStructuralNode().hasLeftSibling());
        assertTrue(pRtx.getStructuralNode().hasRightSibling());
        assertTrue(pRtx.moveToRightSibling());
        assertEquals(6L, pRtx.getNode().getNodeKey());
        assertEquals("foo", pRtx.getValueOfCurrentNode());
        assertTrue(pRtx.getStructuralNode().hasLeftSibling());
        assertEquals(7L, pRtx.getStructuralNode().getLeftSiblingKey());
    }

    @Test
    public void testSecondMoveSubtreeToRightSibling() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(9);
        wtx.moveSubtreeToRightSibling(5);
        // TODO FIX IT!
        // testFourthMoveSubtreeToRightSibling(wtx);
        wtx.commit();
        testSecondMoveSubtreeToRightSibling(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testSecondMoveSubtreeToRightSibling(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testSecondMoveSubtreeToRightSibling()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testSecondMoveSubtreeToRightSibling(final IReadTransaction pRtx)
        throws AbsTTException {
        assertTrue(pRtx.moveToDocumentRoot());
        assertTrue(pRtx.moveTo(4));
        // Assert that oops1 and oops2 text nodes merged.
        assertEquals("oops1oops2", pRtx.getValueOfCurrentNode());
        assertFalse(pRtx.moveTo(8));
        assertTrue(pRtx.moveTo(9));
        assertEquals(5L, pRtx.getStructuralNode().getRightSiblingKey());
        assertTrue(pRtx.moveTo(5));
        assertEquals(9L, pRtx.getStructuralNode().getLeftSiblingKey());
        assertEquals(13L, pRtx.getStructuralNode().getRightSiblingKey());
    }

    @Test
    public void testThirdMoveSubtreeToRightSibling() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(9);
        wtx.moveSubtreeToRightSibling(4);
        // TODO FIX IT!
        // testFourthMoveSubtreeToRightSibling(wtx);
        wtx.commit();
        testThirdMoveSubtreeToRightSibling(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testThirdMoveSubtreeToRightSibling(rtx);
        rtx.close();
    }

    /**
     * Testmethod for {@link UpdateTest#testThirdMoveSubtreeToRightSibling()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testThirdMoveSubtreeToRightSibling(final IReadTransaction pRtx)
        throws AbsTTException {
        assertTrue(pRtx.moveToDocumentRoot());
        assertTrue(pRtx.moveTo(4));
        // Assert that oops1 and oops3 text nodes merged.
        assertEquals("oops1oops3", pRtx.getValueOfCurrentNode());
        assertFalse(pRtx.moveTo(13));
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getRightSiblingKey());
        assertEquals(9L, pRtx.getStructuralNode().getLeftSiblingKey());
        assertTrue(pRtx.moveTo(9));
        assertEquals(4L, pRtx.getStructuralNode().getRightSiblingKey());
    }

    @Test
    public void testFourthMoveSubtreeToRightSibling() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.moveTo(8);
        wtx.moveSubtreeToRightSibling(4);
        testFourthMoveSubtreeToRightSibling(wtx);
        wtx.commit();
        testFourthMoveSubtreeToRightSibling(wtx);
        wtx.close();
        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        testFourthMoveSubtreeToRightSibling(rtx);
        rtx.close();

    }

    /**
     * Testmethod for {@link UpdateTest#testFourthMoveSubtreeToRightSibling()} for having different rtx.
     * 
     * @param pRtx
     *            to test with
     * @throws AbsTTException
     */
    private final static void testFourthMoveSubtreeToRightSibling(final IReadTransaction pRtx)
        throws AbsTTException {
        assertTrue(pRtx.moveTo(4));
        // Assert that oops2 and oops1 text nodes merged.
        assertEquals("oops2oops1", pRtx.getValueOfCurrentNode());
        assertFalse(pRtx.moveTo(8));
        assertEquals(9L, pRtx.getStructuralNode().getRightSiblingKey());
        assertEquals(5L, pRtx.getStructuralNode().getLeftSiblingKey());
        assertTrue(pRtx.moveTo(5L));
        assertEquals(4L, pRtx.getStructuralNode().getRightSiblingKey());
        assertEquals(Long.parseLong(EFixed.NULL_NODE_KEY.getStandardProperty().toString()), pRtx
            .getStructuralNode().getLeftSiblingKey());
        assertTrue(pRtx.moveTo(9));
        assertEquals(4L, pRtx.getStructuralNode().getLeftSiblingKey());
    }

}
