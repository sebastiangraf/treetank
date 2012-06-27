/**
 * 
 */
package org.treetank.node;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.api.INode;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeFactory {

    public final static int UNKNOWN = 0;
    public final static int ELEMENT = 1;
    public final static int ATTRIBUTE = 2;
    public final static int TEXT = 3;
    public final static int WHITESPACE = 4;
    public final static int DELETE = 5;
    public final static int PROCESSING = 7;
    public final static int COMMENT = 8;
    public final static int ROOT = 9;
    public final static int NAMESPACE = 13;

    /**
     * Create page.
     * 
     * @param paramSource
     *            source to read from
     * @return the created page
     */
    public static INode createNode(final byte[] pSource) {
        final ByteBuffer buffer = ByteBuffer.wrap(pSource);
        final int kind = buffer.getInt();

        final NodeDelegate nodeDel = new NodeDelegate(buffer.getLong(),
                buffer.getLong(), buffer.getLong());
        StructNodeDelegate strucDel;
        NameNodeDelegate nameDel;
        ValNodeDelegate valDel;

        INode returnVal = null;
        switch (kind) {
        case ELEMENT:
            strucDel = new StructNodeDelegate(nodeDel, buffer.getLong(),
                    buffer.getLong(), buffer.getLong(), buffer.getLong());
            nameDel = new NameNodeDelegate(nodeDel, buffer.getInt(),
                    buffer.getInt());

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();

            // Attributes getting
            int attrCount = buffer.getInt();
            for (int i = 0; i < attrCount; i++) {
                attrKeys.add(buffer.getLong());
            }

            // Namespace getting
            int nsCount = buffer.getInt();
            for (int i = 0; i < nsCount; i++) {
                namespKeys.add(buffer.getLong());
            }

            returnVal = new ElementNode(nodeDel, strucDel, nameDel, attrKeys,
                    namespKeys);
        case TEXT:
            // Struct Node are 4*8 bytes
            strucDel = new StructNodeDelegate(nodeDel, buffer.getLong(),
                    buffer.getLong(), buffer.getLong(), buffer.getLong());
            // Val is the rest
            valDel = new ValNodeDelegate(nodeDel, Arrays.copyOfRange(pSource,
                    24, pSource.length));
            returnVal = new TextNode(nodeDel, strucDel, valDel);
        case ROOT:
            // Struct Node are 4*8 bytes
            strucDel = new StructNodeDelegate(nodeDel, buffer.getLong(),
                    buffer.getLong(), buffer.getLong(), buffer.getLong());
            returnVal = new DocumentRootNode(nodeDel, strucDel);
        case ATTRIBUTE:
            // Name Node are 2*4 bytes
            nameDel = new NameNodeDelegate(nodeDel, buffer.getInt(),
                    buffer.getInt());
            // Val is the rest
            valDel = new ValNodeDelegate(nodeDel, Arrays.copyOfRange(pSource,
                    8, pSource.length));
            returnVal = new AttributeNode(nodeDel, nameDel, valDel);
        case NAMESPACE:
            // Name Node are 2*4 bytes
            nameDel = new NameNodeDelegate(nodeDel, buffer.getInt(),
                    buffer.getInt());
            returnVal = new NamespaceNode(nodeDel, nameDel);
            break;
        case DELETE:
            returnVal = new DeletedNode(nodeDel);
            break;
        default:
            throw new IllegalStateException(
                    "Invalid Kind of Page. Something went wrong in the serialization/deserialization");
        }
        return returnVal;
    }

}
