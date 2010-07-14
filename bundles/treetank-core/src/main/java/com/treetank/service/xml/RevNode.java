package com.treetank.service.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

/**
 * <h1>RevNode</h1>
 * 
 * <p>
 * Container which holds the full qualified name of a "timestamp" node.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class RevNode {
    /** QName of the node, which has the timestamp attribute. */
    private transient final QName mQName;
    
    /** Attribute which specifies the timestamp value. */
    private transient final Attribute mAttribute;

    /**
     * Constructor.
     * 
     * @param qName
     *            Full qualified name of the timestamp node.
     * @param att
     *            Attribute which specifies the timestamp value.
     */
    public RevNode(final QName qName, final Attribute att) {
        mQName = qName;
        mAttribute = att;
    }

    /**
     * Get mQName.
     * 
     * @return the full qualified name.
     */
    public QName getQName() {
        return mQName;
    }
    
    /**
     * Get attribute.
     * 
     * @return the attribute.
     */
    public Attribute getAttribute() {
      return mAttribute;
    }
}
