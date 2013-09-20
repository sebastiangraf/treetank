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
            into.putLong(from.dataKey).putBytes(from.val).putBoolean(from.eof);
        }
    }

    /**
     * The nodes key value, which is equal with it's position in the list.
     */
    private final long dataKey;

    /**
     * The size of the filenode
     */
    public static final int FILENODESIZE = 1024 * 16;

    /**
     * The content of this node in form of a byte array.
     */
    private final byte[] val;

    /**
     * Determines whether or not this filenode is the last in the sequence.
     */
    private final boolean eof;

    /**
     * Creates a Filenode with given bytes
     * 
     * @param dataKey
     * @param content
     *            , as byte array
     */
    public FileData(long dataKey, byte[] content, boolean eof) {
        this.dataKey = dataKey;
        this.val = content;
        this.eof = eof;
    }

    /**
     * Serializing to given dataput
     * 
     * @throws TTIOException
     */
    public void serialize(final DataOutput output) throws TTIOException {
        try {
            output.writeLong(dataKey);
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
     * Check whether or not this filenode is the last in the sequence.
     * 
     * @return true if this is the last element in the sequence
     */
    public boolean isEof() {
        return eof;
    }

    /**
     * @return value held by this node
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Funnel<IData> getFunnel() {
        return FileNodeFunnel.INSTANCE;
    }

}
