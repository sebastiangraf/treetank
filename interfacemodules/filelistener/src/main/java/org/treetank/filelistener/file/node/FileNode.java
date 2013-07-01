package org.treetank.filelistener.file.node;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.api.INode;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.exceptions.WrongFilenodeDataLengthException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

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
     * Enum for FileNodeFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum FileNodeFunnel implements Funnel<INode> {
        INSTANCE;
        public void funnel(INode node, PrimitiveSink into) {
            final FileNode from = (FileNode)node;
            into.putLong(from.nodeKey).putLong(from.nextNodeKey).putBytes(from.val).putBoolean(from.header)
                .putBoolean(from.eof);
        }
    }

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
    public static final int FILENODESIZE = 524288;

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
    public FileNode(long nodeKey, byte[] content) {
        this.nodeKey = nodeKey;
        val = content;
    }

    /**
     * Serializing to given dataput
     * 
     * @param pOutput
     *            to serialize to
     * @throws TTIOException
     */
    public void serialize(final DataOutput output) throws TTIOException {
        try {
            output.writeLong(nodeKey);
            output.writeLong(nextNodeKey);
            output.writeBoolean(header);
            output.writeBoolean(eof);
            output.writeInt(val.length);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Funnel<INode> getFunnel() {
        return FileNodeFunnel.INSTANCE;
    }

}
