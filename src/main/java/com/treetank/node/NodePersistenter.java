package com.treetank.node;

import com.treetank.io.ITTSource;
import com.treetank.utils.ENodes;

public final class NodePersistenter {

    /**
     * Empty constructor, not needed since access occures with static methods.
     */
    private NodePersistenter() {
        // Not needed over here
    }

    public static AbstractNode createNode(final ITTSource source, final int kind) {
        AbstractNode returnVal = null;

        final ENodes node = ENodes.getEnumKind(kind);
        switch (node) {
        case UNKOWN_KIND:
            // Was null node, do nothing here.
            break;
        case ROOT_KIND:
            returnVal = new DocumentRootNode(source);
            break;
        case ELEMENT_KIND:
            returnVal = new ElementNode(source);
            break;
        case ATTRIBUTE_KIND:
            returnVal = new AttributeNode(source);
            break;
        case NAMESPACE_KIND:
            returnVal = new NamespaceNode(source);
            break;
        case TEXT_KIND:
            returnVal = new TextNode(source);
            break;
        default:
            throw new IllegalStateException(
                    "Unsupported node kind encountered during read: " + kind);
        }
        return returnVal;
    }

    public static AbstractNode createNode(final AbstractNode source) {
        AbstractNode returnVal = null;

        final ENodes kind = source.getKind();

        switch (kind) {
        case UNKOWN_KIND:
            // Was null node, do nothing here.
            break;
        case ROOT_KIND:
            returnVal = new DocumentRootNode(source);
            break;
        case ELEMENT_KIND:
            returnVal = new ElementNode(source);
            break;
        case ATTRIBUTE_KIND:
            returnVal = new AttributeNode(source);
            break;
        case NAMESPACE_KIND:
            returnVal = new NamespaceNode(source);
            break;
        case TEXT_KIND:
            returnVal = new TextNode(source);
            break;
        default:
            throw new IllegalStateException(
                    "Unsupported node kind encountered during read: " + kind);
        }
        return returnVal;

    }

}
