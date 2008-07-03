/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: $
 */

package org.treetank.xpath;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.nodelayer.AbstractNode;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.types.Type;

/**
 * <h1>AtomicValue</h1>
 * <p>
 * An item represents either an atomic value or a node. An atomic value is a
 * value in the value space of an atomic type, as defined in <a
 * href="http://www.w3.org/TR/xmlschema11-2/">XMLSchema 1.1</a>. (Definition:
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
   * @param value
   *          the value of the Item
   * @param type
   *          the item's type
   */
  public AtomicValue(final byte[] value, final int type) {

    mValue = value;

    mType = type;
  }


 
  /**
   * Constructor. Initializes the internal state.
   * 
   * @param value
   *          the value of the Item
   */
  public AtomicValue(final boolean value) {
    
    mValue = TypedValue.getBytes(Boolean.toString(value));
    mType = "xs:boolean".hashCode();
   
  }



  /**
   * Constructor. Initializes the internal state.
   * 
   * @param value
   *          the value of the Item
   * @param type
   *          the item's type
   */
  public AtomicValue(final Number value, final Type type) {

    
    mValue = TypedValue.getBytes(value.toString());
    mType = type.getStringRepr().hashCode();
  }
  
  /**
   * Constructor. Initializes the internal state.
   * 
   * @param value
   *          the value of the Item
   * @param type
   *          the item's type
   */
  public AtomicValue(final String value, final Type type) {

    
  mValue = TypedValue.getBytes(value);
  mType = type.getStringRepr().hashCode();
}



 
  /**
   * {@inheritDoc}
   */
  public void setNodeKey(final long itemKey) {

    mItemKey = itemKey;
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

    return mValue; 
  }

  
  /**
   * {@inheritDoc}
   */
  public long getFirstChildKey() {

    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public long getLeftSiblingKey() {

    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public long getParentKey() {

    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public long getRightSiblingKey() {

    return IReadTransaction.NULL_NODE_KEY;
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
  public AbstractNode getAttribute(final int index) {

    return null;
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
  public int getKind() {

    return 0;
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
  public AbstractNode getNamespace(final int index) {

    return null;
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
   * {@inheritDoc}
   */
  public boolean isFullText() {

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isFullTextLeaf() {

    return false;
  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
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

    return (int) getDBL();
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




  
}
