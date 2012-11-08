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

import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.INode;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.iscsi.node.ByteNode;
import org.treetank.page.NodePage;

/**
 * @author Andreas Rain
 * 
 */
public class IscsiWriteTrx implements IIscsiWriteTrx {

    /** Session for abort/commit. */
    private final ISession mSession;

    /** Delegator for the read access */
    private final IscsiReadTrx mDelegate;

    /**
     * {@inheritDoc}
     */
    public IscsiWriteTrx(IPageWriteTrx pPageTrx, ISession pSession) throws TTException {

        mSession = pSession;
        mDelegate = new IscsiReadTrx(pPageTrx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(INode node) throws TTException {
        ByteNode lastNode =
            (ByteNode)getPageTransaction().prepareNodeForModification(getPageTransaction().getMaxNodeKey());
        node = getPageTransaction().createNode(node);
        lastNode.setNextNodeKey(node.getNodeKey());
        getPageTransaction().finishNodeModification(lastNode);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertAfter(INode node) throws TTException {
        ByteNode currNode =
            (ByteNode)getPageTransaction()
                .prepareNodeForModification(mDelegate.getCurrentNode().getNodeKey());
        node = getPageTransaction().createNode(node);
        currNode.setNextNodeKey(node.getNodeKey());
        getPageTransaction().finishNodeModification(currNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() throws TTException {
        checkAccessAndCommit();

        long nodeKey = mDelegate.getCurrentNode().getNodeKey();
        INode deleteNode = new NodePage.DeletedNode(nodeKey);
        getPageTransaction().createNode(deleteNode);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(byte[] val) throws TTException {
        ByteNode node =
            (ByteNode)getPageTransaction()
                .prepareNodeForModification(mDelegate.getCurrentNode().getNodeKey());
        node.setVal(val);
        getPageTransaction().finishNodeModification(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkAccessAndCommit();

        // Commit uber page.
        getPageTransaction().commit();
        final long revNumber = mDelegate.mPageReadTrx.getActualRevisionRootPage().getRevision();

        getPageTransaction().close();
        // Reset internal transaction state to new uber page.
        mDelegate.setPageTransaction(mSession.beginPageWriteTransaction(revNumber, revNumber));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() throws TTException {
        mDelegate.assertNotClosed();

        long revisionToSet = 0;
        if (!getPageTransaction().getUberPage().isBootstrap()) {
            revisionToSet = mDelegate.mPageReadTrx.getActualRevisionRootPage().getRevision() - 1;
        }

        getPageTransaction().close();

        // Reset internal transaction state to last committed uber page.
        mDelegate.setPageTransaction(mSession.beginPageWriteTransaction(revisionToSet, revisionToSet));

    }

    /**
     * Checking write access and intermediate commit.
     * 
     * @throws TTException
     *             if anything weird happens
     */
    private void checkAccessAndCommit() throws TTException {
        mDelegate.assertNotClosed();
    }

    @Override
    public boolean moveTo(long pKey) {
        return mDelegate.moveTo(pKey);
    }

    @Override
    public boolean nextNode() {
        return mDelegate.nextNode();
    }

    @Override
    public byte[] getValueOfCurrentNode() {
        return mDelegate.getValueOfCurrentNode();
    }

    @Override
    public INode getCurrentNode() {
        return mDelegate.getCurrentNode();
    }

    @Override
    public void close() throws TTIOException {
        if (!isClosed())
            mDelegate.close();
    }

    @Override
    public boolean isClosed() {
        return mDelegate.isClosed();
    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    private PageWriteTrx getPageTransaction() {
        return (PageWriteTrx)mDelegate.mPageReadTrx;
    }

}
