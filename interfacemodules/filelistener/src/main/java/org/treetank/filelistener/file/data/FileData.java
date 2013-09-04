package org.treetank.filelistener.file.data;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.api.IData;
import org.treetank.exception.TTIOException;

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
public class FileData implements IData {

    /**
     * Enum for FileNodeFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum FileNodeFunnel implements Funnel<IData> {
        INSTANCE;
        public void funnel(IData data, PrimitiveSink into) {
            final FileData from = (FileData)data;
            into.putLong(from.dataKey).putLong(from.nextDataKey).putBytes(from.val).putBoolean(from.header)
                .putBoolean(from.eof);
        }
    }

    /**
     * The nodes key value, which is equal with it's position in the list.
     */
    private long dataKey = 0;

    /**
     * The following nodes key
     */
    private long nextDataKey = 0;

    /**
     * The size of the filenode
     */
    public static final int FILENODESIZE = 1024*16;

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
     * @param dataKey 
     * @param content
     *            , as byte array
     */
    public FileData(long dataKey, byte[] content) {
        this.dataKey = dataKey;
        val = content;
    }

    /**
     * Serializing to given dataput
     * 
     * @throws TTIOException
     */
    public void serialize(final DataOutput output) throws TTIOException {
        try {
            output.writeLong(dataKey);
            output.writeLong(nextDataKey);
            output.writeBoolean(header);
            output.writeBoolean(eof);
            output.writeInt(val.length);
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
     * Check whether or not this filenode is the first in the sequence.
     * 
     * @return true if is a header element
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * Set header element
     * @param header
     */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /**
     * Check whether or not this filenode is the last in the sequence.
     * 
     * @return true if this is the last element in the sequence
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
    public void setNextDataKey(long nextNodeKey) {
        this.nextDataKey = nextNodeKey;
    }

    /**
     * The node key of the next node
     * 
     * @return returns the key as long
     */
    public long getNextDataKey() {
        return nextDataKey;
    }

    /**
     * Determine if a node follows after this one.
     * 
     * @return returns true if a node follows
     */
    public boolean hasNext() {
        return !this.isEof();
    }

    /**
     * @return value held by this node
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * @param val
     */
    public void setVal(byte[] val) {
        this.val = val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Funnel<IData> getFunnel() {
        return FileNodeFunnel.INSTANCE;
    }

}
