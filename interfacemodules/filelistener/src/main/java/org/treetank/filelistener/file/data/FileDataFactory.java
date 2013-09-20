package org.treetank.filelistener.file.data;

import java.io.DataInput;
import java.io.IOException;

import org.treetank.api.IData;
import org.treetank.api.IDataFactory;
import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public class FileDataFactory implements IDataFactory {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public IData deserializeData(DataInput input) throws TTIOException {
        try {
            long nodeKey = input.readLong();
            boolean eof = input.readBoolean();
            int length = input.readInt();
            byte[] data = new byte[length];
            input.readFully(data);

            FileData node = null;
            node = new FileData(nodeKey, data, eof);
            return node;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

}
