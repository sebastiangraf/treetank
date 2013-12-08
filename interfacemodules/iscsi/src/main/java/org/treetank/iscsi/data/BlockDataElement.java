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

package org.treetank.iscsi.data;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.api.IData;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * This implementation of {@link IData} is used to store byte arrays in datas.
 * 
 * @author Andreas Rain
 */
public class BlockDataElement implements IData {
    /**
     * Enum for ByteDataFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum ByteDataFunnel implements Funnel<IData> {
        INSTANCE;
        public void funnel(IData data, PrimitiveSink into) {
            final BlockDataElement from = (BlockDataElement)data;
            into.putLong(from.dataKey).putBytes(from.val).putInt(from.size);
        }
    }

    /**
     * The datas key value, which is equal with it's position in the list.
     */
    private final long dataKey;

    /**
     * The size of the byte array in the data. The maximum size of a byte array in
     * a {@link BlockDataElement} is 2^32 - 1. This is because in the deserialization the
     * first 4 bytes determine the size of each data.
     */
    private int size = 0;

    /**
     * The content of this data in form of a byte array.
     */
    private byte[] val;

    /**
     * Creates a BlockDataElement with given bytes
     * 
     * @param pDataKey
     * @param pContent
     */
    public BlockDataElement(long pDataKey, byte[] pContent) {
        dataKey = pDataKey;
        size = pContent.length;
        val = pContent;
    }

    /**
     * Serializing to given dataput
     * 
     * @param output
     *            to serialize to
     * @throws TTIOException
     */
    public void serialize(final DataOutput output) throws TTIOException {
        try {
            output.writeInt(size);
            output.writeLong(dataKey);
            output.write(val);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public long getDataKey() {

        return this.dataKey;
    }

    /**
     * Getting the byte array contained by this data.
     * 
     * @return returns the byte array
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Replace the existing byte array with another byte array.
     * 
     * @param pVal
     */
    public void setVal(byte[] pVal) {
        val = pVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 36919;
        int result = 1;
        result = prime * result + (int)(dataKey ^ (dataKey >>> 32));
        result = prime * result + size;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockDataElement other = (BlockDataElement)obj;
        if (dataKey != other.dataKey)
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Funnel<IData> getFunnel() {
        return ByteDataFunnel.INSTANCE;
    }

}
