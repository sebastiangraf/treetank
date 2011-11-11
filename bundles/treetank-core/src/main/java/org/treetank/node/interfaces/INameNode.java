package org.treetank.node.interfaces;

public interface INameNode extends INode {
    /**
     * Gets key of qualified name.
     * 
     * @return key of qualified name
     */
    int getNameKey();

    /**
     * Gets key of the URI.
     * 
     * @return URI key
     */
    int getURIKey();

    /**
     * Setting the name key.
     * 
     * @param paramNameKey
     *            the namekey to be set.
     */
    void setNameKey(int paramNameKey);

    /**
     * Setting the uri key.
     * 
     * @param paramUriKey
     *            the urikey to be set.
     */
    void setURIKey(int paramUriKey);
}
