/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.service.xml.xpath;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.node.AbsNode;
import org.treetank.node.ENodes;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.settings.EFixed;
import org.treetank.utils.NamePageHash;
import org.treetank.utils.TypedValue;

/**
 * <h1>AtomicValue</h1>
 * <p>
 * An item represents either an atomic value or a node. An atomic value is a value in the value space of an
 * atomic type, as defined in <a href="http://www.w3.org/TR/xmlschema11-2/">XMLSchema 1.1</a>. (Definition:
 * Atomic types are anyAtomicType and all types derived from it.)
 * </p>
 */
public class AtomicValue implements IItem {

    /** value of the item as byte array. */
    private byte[] mValue;

    /** The item's value type. */
    private int mType;

    /**
     * The item's key. In case of an Atomic value this is always a negative to
     * make them distinguishable from nodes.
     */
    private long mItemKey;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mValue
     *            the value of the Item
     * @param mType
     *            the item's type
     */
    public AtomicValue(final byte[] mValue, final int mType) {

        this.mValue = mValue;
        this.mType = mType;
    }

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mValue
     *            the value of the Item
     */
    public AtomicValue(final boolean mValue) {

        this.mValue = TypedValue.getBytes(Boolean.toString(mValue));
        this.mType = NamePageHash.generateHashForString("xs:boolean");

    }

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mValue
     *            the value of the Item
     * @param mType
     *            the item's type
     */
    public AtomicValue(final Number mValue, final Type mType) {

        this.mValue = TypedValue.getBytes(mValue.toString());
        this.mType = NamePageHash.generateHashForString(mType.getStringRepr());
    }

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mValue
     *            the value of the Item
     * @param mType
     *            the item's type
     */
    public AtomicValue(final String mValue, final Type mType) {

        this.mValue = TypedValue.getBytes(mValue);
        this.mType = NamePageHash.generateHashForString(mType.getStringRepr());
    }

    /**
     * {@inheritDoc}
     */
    public void setNodeKey(final long mItemKey) {

        this.mItemKey = mItemKey;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNode() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getRawValue() {

        return mValue.clone();
    }

    /**
     * {@inheritDoc}
     */
    public long getParentKey() {

        return (Integer)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasFirstChild() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasLeftSibling() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasParent() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRightSibling() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getAttributeKey(final int index) {

        return (Integer)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public long getNodeKey() {

        return mItemKey;
    }

    /**
     * {@inheritDoc}
     */
    public int getAttributeCount() {

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getChildCount() {

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public ENodes getKind() {

        return ENodes.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    public int getNameKey() {

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getNamespaceKey(final int mIndex) {

        return (Integer)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getNamespaceCount() {

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getURIKey() {

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAttribute() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDocumentRoot() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isElement() {

        return false;
    }

    /**
     * Check if is fulltext.
     * 
     * @return true if fulltext, false otherwise
     */
    public boolean isFullText() {

        return false;
    }

    /**
     * Test if the lead is tes.
     * 
     * @return true if fulltest leaf, false otherwise
     */
    public boolean isFullTextLeaf() {

        return false;
    }

    /**
     * Test if the root is full text.
     * 
     * @return true if fulltest root, false otherwise
     */
    public boolean isFullTextRoot() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isText() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final int getTypeKey() {
        return mType;
    }

    /**
     * Getting the type of the value.
     * 
     * @return the type of this value
     */
    public final String getType() {
        return Type.getType(mType).getStringRepr();
    }

    /**
     * Returns the atomic value as an integer.
     * 
     * @return the value as an integer
     */
    public int getInt() {

        return (int)getDBL();
    }

    /**
     * Returns the atomic value as a boolean.
     * 
     * @return the value as a boolean
     */
    public boolean getBool() {

        return Boolean.parseBoolean(TypedValue.parseString(mValue));
    }

    /**
     * Returns the atomic value as a float.
     * 
     * @return the value as a float
     */
    public float getFLT() {

        return Float.parseFloat(TypedValue.parseString(mValue));
    }

    /**
     * Returns the atomic value as a double.
     * 
     * @return the value as a double
     */
    public double getDBL() {

        return Double.parseDouble(TypedValue.parseString(mValue));
    }

    /**
     * To String method.
     * 
     * @return String String representation of this node
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Atomic Value: ");
        builder.append(new String(mValue));
        builder.append("\nKey: ");
        builder.append(mItemKey);
        return builder.toString();
    }

    @Override
    public void setHash(long hash) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getHash() {
        // TODO Auto-generated method stub
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IItem> T accept(final IReadTransaction paramTransaction) {
        return (T)paramTransaction.getNode(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        // Do nothing.
    }
}
