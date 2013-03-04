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

package org.treetank.node;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.treetank.api.INode;
import org.treetank.exception.TTIOException;

/**
 * This implementation of {@link INode} is used to store byte arrays in nodes.
 * 
 * @author Andreas Rain
 */
public class ByteNode implements INode {

    /**
     * The nodes key value, which is equal with it's position in the list.
     */
    private final long nodeKey;

    /**
     * The following nodes key
     */
    private long nextNodeKey = 0;

    /**
     * The previous nodes key
     */
    private long previousNodeKey = -1;

    /**
     * The real index of this byte.
     */
    private long index = 0;

    /**
     * The size of the byte array in the node. The maximum size of a byte array in
     * a {@link ByteNode} is 2^32 - 1. This is because in the deserialization the
     * first 4 bytes determine the size of each node.
     */
    private int size = 0;

    /**
     * The content of this node in form of a byte array.
     */
    private byte[] val;

    /**
     * Creates a ByteNode with given bytes
     * @param pNodeKey 
     * @param pContent 
     */
    public ByteNode(long pNodeKey, byte[] pContent) {
        nodeKey = pNodeKey;
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
            output.writeLong(index);
            output.writeLong(nodeKey);
            output.writeLong(previousNodeKey);
            output.writeLong(nextNodeKey);
            output.write(val);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public long getNodeKey() {

        return this.nodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return Objects.hash(nodeKey,nextNodeKey,previousNodeKey,index);
    }

    /**
     * Getting the byte array contained by this node.
     * 
     * @return returns the byte array
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Replace the existing byte array with another byte array.
     * @param pVal 
     */
    public void setVal(byte[] pVal) {

        val = pVal;
    }

    /**
     * The node key of the next node
     * 
     * @return returns the key as long
     */
    public long getNextNodeKey() {

        return nextNodeKey;
    }

    /**
     * Determine if a node follows after this one.
     * 
     * @return returns true if a node follows
     */
    public boolean hasNext() {
        return (this.nextNodeKey != 0);
    }

    /**
     * Set the link to the next node. Use the nodekey of that node.
     * 
     * @param nextNodeKey
     *            as a long
     */
    public void setNextNodeKey(long nextNodeKey) {

        this.nextNodeKey = nextNodeKey;
    }

    /**
     * The node key of the previous node
     * 
     * @return returns the key as long
     */
    public long getPreviousNodeKey() {

        return previousNodeKey;
    }

    /**
     * Set the link to the previous node. Use the nodekey of that node.
     * 
     * @param previousNodeKey
     *            as a long
     */
    public void setPreviousNodeKey(long previousNodeKey) {

        this.previousNodeKey = previousNodeKey;
    }

    /**
     * Determine if a node preceids this node.
     * 
     * @return returns true if a node follows
     */
    public boolean hasPrevious() {
        return (this.previousNodeKey != -1);
    }

    /**
     * Getting the index of the node.
     * 
     * @return returns the index as an int
     */
    public long getIndex() {

        return index;
    }

    /**
     * Reset the index of this node.
     * 
     * @param index
     */
    public void setIndex(long index) {

        this.index = index;
    }

    /**
     * Increment the index of this node
     * 
     * @return return the new index
     */
    public long incIndex() {
        this.index++;
        return this.index;
    }

    /**
     * Decrement the index of this node
     * 
     * @return returns the new index of this node
     */
    public long decIndex() {
        this.index--;
        return this.index;
    }

}
