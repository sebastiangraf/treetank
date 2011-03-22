/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.util.concurrent.locks.Lock;

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.api.IStructuralItem;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.AbsNode;
import org.treetank.node.AbsStructNode;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.page.UberPage;
import org.treetank.settings.EFixed;
import org.treetank.utils.TypedValue;

public class SynchWriteTransaction extends WriteTransaction {

    private LockManager lock;
    protected SynchWriteTransaction(long mTransactionID, SessionState mSessionState,
        WriteTransactionState mTransactionState, int maxNodeCount, int maxTime) throws AbsTTException {
        super(mTransactionID, mSessionState, mTransactionState, maxNodeCount, maxTime);
       lock = LockManager.getLockManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQname) throws AbsTTException {
       lock.getWritePermission(getCurrentNode().getNodeKey(), this);
       return super.insertElementAsFirstChild(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName mQname) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertElementAsRightSibling(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertTextAsFirstChild(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return insertTextAsRightSibling(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws AbsTTException {
        if (getCurrentNode() instanceof ElementNode) {
            lock.getWritePermission(getCurrentNode().getNodeKey(), this);
            return super.insertAttribute(mQname, mValueAsString);
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertNamespace(final String mUri, final String mPrefix)
        throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertNamespace(mUri, mPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setName(final String mName) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setName(mName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setURI(final String mUri) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setURI(mUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final int mValueType, final byte[] mValue) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setValue(mValueType, mValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final String mValue) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setValue(mValue);
    }
}
