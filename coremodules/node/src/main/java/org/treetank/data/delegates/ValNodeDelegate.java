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
package org.treetank.data.delegates;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.api.IData;
import org.treetank.data.IConstants;
import org.treetank.data.interfaces.ITreeValData;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;

/**
 * Delegate method for all nodes containing \"value\"-data. That means that
 * independent values are stored by the nodes delegating the calls of the
 * interface {@link ITreeValData} to this class.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ValNodeDelegate implements ITreeValData {

    /**
     * Enum for ValNodeFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum ValNodeDelegateFunnel implements Funnel<org.treetank.api.IData> {
        INSTANCE;
        public void funnel(org.treetank.api.IData data, PrimitiveSink into) {
            final ITreeValData from = (ITreeValData)data;
            into.putBytes(from.getRawValue());
        }
    }

    /** Delegate for common node information. */
    private NodeDelegate mDelegate;
    /** Storing the value. */
    private byte[] mVal;

    /**
     * Constructor
     * 
     * @param pNodeDel
     *            the common data.
     * @param pVal
     *            the own value.
     */
    public ValNodeDelegate(final NodeDelegate pNodeDel, final byte[] pVal) {
        this.mDelegate = pNodeDel;
        mVal = pVal;
    }

    /**
     * Delegate method for getKind.
     * 
     * @return the kind of the node
     * @see org.treetank.data.delegates.NodeDelegate#getKind()
     */
    public int getKind() {
        return mDelegate.getKind();
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return the node of the key
     * @see org.treetank.data.delegates.NodeDelegate#getDataKey()
     */
    public long getDataKey() {
        return mDelegate.getDataKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return the key of the parent
     * @see org.treetank.data.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.data.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long pParentKey) {
        mDelegate.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return the key of the type
     * @see org.treetank.data.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.data.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int pTypeKey) {
        mDelegate.setTypeKey(pTypeKey);
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return if the node has a parent
     * @see org.treetank.data.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return mVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final byte[] pVal) {
        mVal = pVal;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putBytes(mVal);
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
        return toStringHelper(this).add("mDelegate", mDelegate).add("mVal", mVal).toString();
    }

    /**
     * Serializing to given dataput
     * 
     * @param pOutput
     *            to serialize to
     * @throws TTIOException
     */
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(getRawValue().length);
            pOutput.write(getRawValue());
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<IData> getFunnel() {
        return ValNodeDelegateFunnel.INSTANCE;
    }
}
