package org.treetank.filelistener.file.node;

import java.io.DataInput;
import java.io.IOException;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public class FileNodeFactory implements INodeFactory {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public INode deserializeNode(DataInput input) throws TTIOException {
        try {
            long nodeKey = input.readLong();
            long nextNodeKey = input.readLong();
            boolean header = input.readBoolean();
            boolean eof = input.readBoolean();
            int length = input.readInt();
            byte[] data = new byte[length];
            input.readFully(data);

            FileNode node = null;
            node = new FileNode(nodeKey, data);
            node.setNextNodeKey(nextNodeKey);
            node.setHeader(header);
            node.setEof(eof);
            return node;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

}
