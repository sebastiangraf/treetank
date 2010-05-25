package com.treetank.settings;

/**
 * Enumeration for different nodes. All nodes are determined by a unique id.
 * 
 * @author Sebastian Graf, University of Konstanzs
 * 
 */
public enum ENodes {

    /** Unknown kind */
    UNKOWN_KIND(0),
    /** Node kind is element. */
    ELEMENT_KIND(1),
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2),
    /** Node kind is text. */
    TEXT_KIND(3),
    /** Node kind is namespace. */
    NAMESPACE_KIND(13),
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7),
    /** Node kind is comment. */
    COMMENT_KIND(8),
    /** Node kind is document root. */
    ROOT_KIND(9),
    /** Whitespace text*/
    WHITESPACE_KIND(4),
    /** Node kind is deleted node. */
    DELETE_KIND(5);

    /** Identifier */
    private final int mKind;

    /**
     * Constructor
     * 
     * @param kind
     *            the identifier
     */
    private ENodes(final int kind) {
        this.mKind = kind;
    }

    /**
     * Getter for the identifier
     * 
     * @return the unique identifier
     */
    public int getNodeIdentifier() {
        return mKind;
    }

    /**
     * Public method to get the related node based on the identifier
     * 
     * @param intKind
     *            the identifier for the node
     * @return the related nodekind
     */
    public static ENodes getEnumKind(final int intKind) {
        ENodes returnVal;
        switch (intKind) {
        case 0:
            returnVal = UNKOWN_KIND;
            break;
        case 1:
            returnVal = ELEMENT_KIND;
            break;
        case 2:
            returnVal = ATTRIBUTE_KIND;
            break;
        case 3:
            returnVal = TEXT_KIND;
            break;
        case 13:
            returnVal = NAMESPACE_KIND;
            break;
        case 7:
            returnVal = PROCESSING_KIND;
            break;
        case 8:
            returnVal = COMMENT_KIND;
            break;
        case 9:
            returnVal = ROOT_KIND;
            break;
        case 5:
            returnVal = DELETE_KIND;
            break;
        case 4:
            returnVal = WHITESPACE_KIND;
            break;
        default:
            returnVal = null;
            break;
        }
        return returnVal;
    }
}
