package org.treetank.filelistener.file.node;

import org.treetank.api.INode;
import org.treetank.filelistener.exceptions.WrongFilenodeDataLengthException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * A sequence of Filenodes represents a full file. A Filnode has a content size
 * of 512 bytes and has a reference to the following node if such a node exists.
 * 
 * The header filenode is being referenced by the page layer in a hash map. It
 * consists of a relative path.
 * 
 * @author andreas
 * 
 */
public class FileNode implements INode {

    /**
     * The nodes key value, which is equal with it's position in the list.
     */
    private long nodeKey = 0;

    /**
     * The following nodes key
     */
    private long nextNodeKey = 0;

    /**
     * The size of the filenode
     */
    public static final int FILENODESIZE = 512;

    /**
     * NodeKey for EOF filenodes
     */

    public static final long EOF_KEY = -1;

    /**
     * The content of this node in form of a byte array.
     */
    private byte[] val;

    /**
     * Determines whether or not this filenode is the first in the sequence.
     */
    private boolean header;

    /**
     * Determines whether or not this filenode is the last in the sequence.
     */
    private boolean eof;

    /**
     * Creates a Filenode with given bytes
     * 
     * @param content
     *            , as byte array
     * @throws WrongFilenodeDataLengthException
     */
    public FileNode(long nodeKey, byte[] content) throws WrongFilenodeDataLengthException {
        this.nodeKey = nodeKey;

        if (content.length != FILENODESIZE) {
            throw new WrongFilenodeDataLengthException();
        }

        val = content;
    }

    @Override
    public byte[] getByteRepresentation() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeLong(nodeKey);
        output.writeLong(nextNodeKey);
        output.writeBoolean(header);
        output.writeBoolean(eof);
        output.write(val);

        return output.toByteArray();
    }

    @Override
    public long getNodeKey() {
        return this.nodeKey;
    }

    @Override
    public void setHash(long pHash) {
    }

    @Override
    public long getHash() {
        return this.nodeKey * nextNodeKey * 31;
    }

    /**
     * Check whether or not this filenode is the first in the sequence.
     * 
     * @return
     */
    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    /**
     * Check whether or not this filenode is the last in the sequence.
     * 
     * @return
     */
    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
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
        return !this.isEof();
    }

    public byte[] getVal() {
        return val;
    }

    public void setVal(byte[] val) {
        this.val = val;
    }

}
