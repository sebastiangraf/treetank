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
package org.treetank.node.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.treetank.io.ITTSink;

/**
 * {@link NodeSink} implementation for writing node output.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class NodeOutputSink implements ITTSink {

    /**
     * Output stream for node sink.
     */
    private final ByteArrayOutputStream mNodeOutput;

    /**
     * Constructor.
     */
    public NodeOutputSink() {
        this.mNodeOutput = new ByteArrayOutputStream();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    public void writeLong(final long mLongVal) {
        try {
            mNodeOutput.write(longToByteArray(mLongVal));
        } catch (final IOException mExp) {
            mExp.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeInt(final int mIntVal) {
        try {
            mNodeOutput.write(intToByteArray(mIntVal));
        } catch (final IOException mExp) {
            mExp.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeByte(byte mByteVal) {
        mNodeOutput.write(mByteVal);
    }

    /**
     * Converting an integer value to byte array.
     * 
     * @param mValue
     *            Integer value to convert.
     * @return Byte array of integer value.
     */
    private byte[] intToByteArray(final int mIntVal) {
        final byte[] mBuffer = new byte[4];

        mBuffer[0] = (byte)(0xff & (mIntVal >>> 24));
        mBuffer[1] = (byte)(0xff & (mIntVal >>> 16));
        mBuffer[2] = (byte)(0xff & (mIntVal >>> 8));
        mBuffer[3] = (byte)(0xff & mIntVal);

        return mBuffer;
    }

    /**
     * Converting a Long value to byte array.
     * 
     * @param mValue
     *            Long value to convert.
     * @return Byte array of long value.
     */
    private byte[] longToByteArray(final long mLongVal) {
        final byte[] mBuffer = new byte[8];

        mBuffer[0] = (byte)(0xff & (mLongVal >> 56));
        mBuffer[1] = (byte)(0xff & (mLongVal >> 48));
        mBuffer[2] = (byte)(0xff & (mLongVal >> 40));
        mBuffer[3] = (byte)(0xff & (mLongVal >> 32));
        mBuffer[4] = (byte)(0xff & (mLongVal >> 24));
        mBuffer[5] = (byte)(0xff & (mLongVal >> 16));
        mBuffer[6] = (byte)(0xff & (mLongVal >> 8));
        mBuffer[7] = (byte)(0xff & mLongVal);

        return mBuffer;
    }

    /**
     * {@inheritDoc}
     */
    public ByteArrayOutputStream getOutputStream() {
        return this.mNodeOutput;
    }

}
