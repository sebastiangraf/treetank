/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import static com.google.common.base.Preconditions.checkState;

import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.ByteNode;

/**
 * @author Andreas Rain
 */
public class IscsiWriteTrx implements IIscsiWriteTrx {

    /** Session for abort/commit. */
    private final ISession mSession;

    /** Delegator for the read access */
    private final IscsiReadTrx mDelegate;

    /**
     * Create a new IscsiWriteTrx
     * 
     * @param pPageTrx
     *            delegation page transaction
     * @param pSession
     *            session from treetank
     * @throws TTException
     */
    public IscsiWriteTrx(IPageWriteTrx pPageTrx, ISession pSession) throws TTException {

        mSession = pSession;
        mDelegate = new IscsiReadTrx(pPageTrx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bootstrap(byte[] bytes, boolean hasNextNode) throws TTException {
        ByteNode node = new ByteNode(getPageTransaction().incrementNodeKey(), bytes);
        if (hasNextNode) {
            node.setNextNodeKey(node.getNodeKey() + 1);
        }

        if (mDelegate.getCurrentNode() != null) {
            node.setIndex(node.getNodeKey());
            node.setPreviousNodeKey(node.getNodeKey() - 1);
            getPageTransaction().setNode(node);

            mDelegate.moveTo(node.getNodeKey());
        } else {
            node.setIndex(0);
            getPageTransaction().setNode(node);
            mDelegate.moveTo(node.getNodeKey());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(byte[] val) throws TTException {

        ByteNode node = (ByteNode)getPageTransaction().getNode(mDelegate.getCurrentNode().getNodeKey());
        node.setVal(val);
        getPageTransaction().setNode(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");

        // CommitStrategy uber page.
        getPageTransaction().commit();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");

        long revisionToSet = 0;
        revisionToSet = mDelegate.mPageReadTrx.getRevision() - 1;

        getPageTransaction().close();

        // Reset internal transaction state to last committed uber page.
        mDelegate.setPageTransaction(mSession.beginPageWriteTransaction(revisionToSet));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveTo(long pKey) {

        return mDelegate.moveTo(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nextNode() {

        return mDelegate.nextNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getValueOfCurrentNode() {

        return mDelegate.getValueOfCurrentNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteNode getCurrentNode() {
        return mDelegate.getCurrentNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {

        if (!isClosed())
            mDelegate.close();
    }

    /**
     * {@inheritDoc}
     */
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

    @Override
    public boolean previousNode() {
        return mDelegate.previousNode();
    }

}
