/**
 * 
 */
package org.treetank.node;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;
import org.treetank.bucket.NodeBucket.DeletedNode;
import org.treetank.exception.TTIOException;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;

/**
 * Factory necessary to deserialize tree-nodes.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TreeNodeFactory implements INodeFactory {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public INode deserializeNode(DataInput input) throws TTIOException {
        try {
            final int kind = input.readInt();

            NodeDelegate nodeDel;
            StructNodeDelegate strucDel;
            NameNodeDelegate nameDel;
            ValNodeDelegate valDel;

            INode returnVal = null;
            switch (kind) {
            case IConstants.ELEMENT:
                nodeDel = new NodeDelegate(input.readLong(), input.readLong());
                strucDel =
                    new StructNodeDelegate(nodeDel, input.readLong(), input.readLong(), input.readLong(),
                        input.readLong());
                nameDel = new NameNodeDelegate(nodeDel, input.readInt(), input.readInt());

                final List<Long> attrKeys = new ArrayList<Long>();
                final List<Long> namespKeys = new ArrayList<Long>();

                // Attributes getting
                int attrCount = input.readInt();
                for (int i = 0; i < attrCount; i++) {
                    attrKeys.add(input.readLong());
                }

                // Namespace getting
                int nsCount = input.readInt();
                for (int i = 0; i < nsCount; i++) {
                    namespKeys.add(input.readLong());
                }

                returnVal = new ElementNode(nodeDel, strucDel, nameDel, attrKeys, namespKeys);
                break;
            case IConstants.TEXT:
                nodeDel = new NodeDelegate(input.readLong(), input.readLong());
                // Struct Node are 4*8 bytes (+4 (kind) + 24 (nodedel))
                strucDel =
                    new StructNodeDelegate(nodeDel, input.readLong(), input.readLong(), input.readLong(),
                        input.readLong());
                // Val is the rest
                byte[] rawValText = new byte[input.readInt()];
                input.readFully(rawValText);
                valDel = new ValNodeDelegate(nodeDel, rawValText);
                returnVal = new TextNode(nodeDel, strucDel, valDel);
                break;
            case IConstants.ROOT:
                nodeDel = new NodeDelegate(input.readLong(), input.readLong());
                // Struct Node are 4*8 bytes
                strucDel =
                    new StructNodeDelegate(nodeDel, input.readLong(), input.readLong(), input.readLong(),
                        input.readLong());
                returnVal = new DocumentRootNode(nodeDel, strucDel);
                break;
            case IConstants.ATTRIBUTE:
                nodeDel = new NodeDelegate(input.readLong(), input.readLong());
                // Name Node are 2*4 bytes (+4 (kind) + 24 (nodedel))
                nameDel = new NameNodeDelegate(nodeDel, input.readInt(), input.readInt());
                // Val is the rest
                byte[] rawValAttr = new byte[input.readInt()];
                input.readFully(rawValAttr);
                valDel = new ValNodeDelegate(nodeDel, rawValAttr);
                returnVal = new AttributeNode(nodeDel, nameDel, valDel);
                break;
            case IConstants.NAMESPACE:
                nodeDel = new NodeDelegate(input.readLong(), input.readLong());
                // Name Node are 2*4 bytes
                nameDel = new NameNodeDelegate(nodeDel, input.readInt(), input.readInt());
                returnVal = new NamespaceNode(nodeDel, nameDel);
                break;
            case org.treetank.bucket.IConstants.DELETEDNODE:
                returnVal = new DeletedNode(input.readLong());
                break;
            default:
                throw new IllegalStateException(
                    "Invalid Kind of Node. Something went wrong in the serialization/deserialization");
            }
            return returnVal;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

}
