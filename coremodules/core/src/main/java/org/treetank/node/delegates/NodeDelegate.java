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
package org.treetank.node.delegates;

import static org.treetank.node.IConstants.NULL_NODE;
import static org.treetank.node.IConstants.TYPE_KEY;

import org.treetank.node.ENode;
import org.treetank.node.IConstants;
import org.treetank.node.interfaces.INode;

import com.google.common.hash.Hasher;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Delegate method for all nodes. That means that all nodes stored in Treetank
 * are represented by an instance of the interface {@link INode} namely
 * containing the position in the tree related to a parent-node, the related
 * type and the corresponding hash recursivly computed.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeDelegate implements INode {

    /** Key of the current node. Must be unique for all nodes. */
    private long mNodeKey;
    /** Key of the parent node. */
    private long mParentKey;
    /** Hash of the parent node. */
    private long mHash;
    /**
     * TypeKey of the parent node. Can be referenced later on over special
     * pages.
     */
    private int mTypeKey;

    /**
     * Constructor.
     * 
     * @param pNodeKey
     *            to be represented by this delegate.
     * @param pParentKey
     *            to be represented by this delegate
     * @param pHash
     *            to be represented by this delegate
     */
    public NodeDelegate(final long pNodeKey, final long pParentKey, final long pHash) {
        mNodeKey = pNodeKey;
        mParentKey = pParentKey;
        mHash = pHash;
        mTypeKey = TYPE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENode getKind() {
        return ENode.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeKey() {
        return mNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeKey(final long pNodeKey) {
        this.mNodeKey = pNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return mParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentKey(final long pParentKey) {
        this.mParentKey = pParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return mHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHash(final long pHash) {
        this.mHash = pHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putLong(mHash);
        hc.putLong(mNodeKey);
        hc.putLong(mParentKey);
        hc.putInt(mTypeKey);
        return hc.hash().asInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("node key: ");
        builder.append(getNodeKey());
        builder.append("\nparent key: ");
        builder.append(getParentKey());
        builder.append("\ntype key: ");
        builder.append(getTypeKey());
        builder.append("\nhash: ");
        builder.append(getHash());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return mTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTypeKey(int pTypeKey) {
        this.mTypeKey = pTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return mParentKey != NULL_NODE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeLong(getNodeKey());
        pOutput.writeLong(getParentKey());
        pOutput.writeLong(getHash());
        return pOutput.toByteArray();
    }

}
