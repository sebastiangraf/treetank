/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.api;

/**
 * <h1>IReadTransaction</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to access nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount
 * encoding. This encoding keeps the children ordered but has no knowledge of
 * the global node ordering. The underlying tree is accessed in a cursor-like
 * fashion.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 *  <ol>
 *   <li><strong>Precondition</strong> before moving cursor:
 *       <code>IReadTransaction.getNodeKey() == n</code>.</li>
 *   <li><strong>Postcondition</strong> after moving cursor:
 *       <code>(IReadTransaction.moveX() == true &&
 *       IReadTransaction.getNodeKey() == m) ||
 *       (IReadTransaction.moveX() == false &&
 *       IReadTransaction.getNodeKey() == n)</code>.</li>
 *  </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 *  <pre>
 *   final IReadTransaction rtx = session.beginReadTransaction();
 *   
 *   // Either test before moving...
 *   if (rtx.hasFirstChild()) {
 *     rtx.moveToFirstChild();
 *     ...
 *   }
 *   
 *   // or test after moving. Whatever, do the test!
 *   if (rtx.moveToFirstChild()) {
 *     ...
 *   }
 *   
 *   // Access local part of element.
 *   if (rtx.isElement() && rtx.getLocalPart().equalsIgnoreCase("foo") {
 *     ...
 *   }
 *   
 *   // Access value of first attribute of element.
 *   if (rtx.isElement() && (rtx.getAttributeCount() > 0)) {
 *     rtx.moveToAttribute(0);
 *     System.out.println(UTF.parseString(rtx.getValue()));
 *   }
 *   
 *   rtx.close();
 *  </pre>
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 *  <pre>
 *   public final void someIReadTransactionMethod() {
 *     // This must be called to make sure the transaction is not closed.
 *     assertNotClosed();
 *     ...
 *   }
 *  </pre>
 * </p>
 */
public interface IReadTransaction {

  // --- Keys ------------------------------------------------------------------

  /** Null key for node references. */
  public static final long NULL_NODE_KEY = -1L;

  /** Null key for name references. */
  public static final int NULL_NAME_KEY = -1;

  /** Key of document root node. */
  public static final long DOCUMENT_ROOT_KEY = 0L;

  /** Key of full text root node. */
  public static final long FULLTEXT_ROOT_KEY = 1L;

  //--- Kinds ------------------------------------------------------------------

  /** Node kind is element. */
  public static final int ELEMENT_KIND = 1;

  /** Node kind is attribute. */
  public static final int ATTRIBUTE_KIND = 2;

  /** Node kind is text. */
  public static final int TEXT_KIND = 3;

  /** Node kind is namespace. */
  public static final int NAMESPACE_KIND = 4;

  /** Node kind is processing instruction. */
  public static final int PROCESSING_INSTRUCTION_KIND = 7;

  /** Node kind is comment. */
  public static final int COMMENT_KIND = 8;

  /** Node kind is document root. */
  public static final int DOCUMENT_ROOT_KIND = 9;

  /** Node kind is fulltext root. */
  public static final int FULLTEXT_ROOT_KIND = 13;

  /** Node kind is fulltext. */
  public static final int FULLTEXT_KIND = 14;

  /** Node kind is fulltext leaf. */
  public static final int FULLTEXT_LEAF_KIND = 15;

  //--- Types ------------------------------------------------------------------

  /** Node type is UTF-8 String. */
  public static final int STRING_TYPE = 0;

  /** Node type is raw byte[]. */
  public static final int BYTEARRAY_TYPE = 1;

  /** Node type is int. */
  public static final int INT_TYPE = 2;

  /** Node type is long. */
  public static final int LONG_TYPE = 3;

  /** Node type is float. */
  public static final int FLOAT_TYPE = 4;

  /** Node type is double. */
  public static final int DOUBLE_TYPE = 5;

  /** Node type is boolean. */
  public static final int BOOLEAN_TYPE = 6;

  /**
   * What is the revision number of this IReadTransaction?
   * 
   * @return Immutable revision number of this IReadTransaction.
   */
  public long getRevisionNumber();

  /**
   * How many nodes are stored in the revision of this IReadTransaction?
   * 
   * @return Immutable number of nodes of this IReadTransaction.
   */
  public long getRevisionSize();

  /**
   * UNIX-style timestamp of the commit of the revision.
   * 
   * @return Timestamp of revision commit.
   */
  public long getRevisionTimestamp();

  // --- Node Selectors --------------------------------------------------------

  /**
   * Move cursor to a node by its node key.
   * 
   * @param nodeKey Key of node to select.
   * @return True if the node with the given node key is selected.
   */
  public boolean moveTo(final long nodeKey);

  /**
   * Move cursor to token in full text index. Note that this might only be
   * the prefix of a token. If the token can not be found, the cursor is
   * left off at the full text root node.
   * 
   * @param token Token to find.
   * @return True if the token is contained.
   */
  public boolean moveToToken(final String token);

  /**
   * Move cursor to document root node.
   * 
   * @return True if the document root node is selected.
   */
  public boolean moveToDocumentRoot();

  /**
   * Move cursor to fulltext root node.
   * 
   * @return True if the full text root node is selected.
   */
  public boolean moveToFullTextRoot();

  /**
   * Move cursor to parent node of currently selected node.
   * 
   * @return True if the parent node is selected.
   */
  public boolean moveToParent();

  /**
   * Move cursor to first child node of currently selected node.
   * 
   * @return True if the first child node is selected.
   */
  public boolean moveToFirstChild();

  /**
   * Move cursor to left sibling node of the currently selected node.
   * 
   * @return True if the left sibling node is selected.
   */
  public boolean moveToLeftSibling();

  /**
   * Move cursor to right sibling node of the currently selected node.
   * 
   * @return True if the right sibling node is selected.
   */
  public boolean moveToRightSibling();

  /**
   * Move cursor to attribute by its index.
   * 
   * @param index Index of attribute to move to.
   * @return True if the attribute node is selected.
   */
  public boolean moveToAttribute(final int index);

  // --- Node Getters ----------------------------------------------------------

  /**
   * Get node key of currently selected node.
   * 
   * @return Node key of currently selected node.
   */
  public long getNodeKey();

  /**
   * Is there a parent?
   * 
   * @return True if there is a parent. False else.
   */
  public boolean hasParent();

  /**
   * Get parent key of currently selected node.
   * 
   * @return Parent key of currently selected node.
   */
  public long getParentKey();

  /**
   * Is there a first child?
   * 
   * @return True if there is a first child. False else.
   */
  public boolean hasFirstChild();

  /**
   * Get first child key of currently selected node.
   * 
   * @return First child key of currently selected node.
   */
  public long getFirstChildKey();

  /**
   * Is there a left sibling?
   * 
   * @return True if there is a left sibling. False else.
   */
  public boolean hasLeftSibling();

  /**
   * Get left sibling key of currently selected node.
   * 
   * @return Left sibling key of currently selected node.
   */
  public long getLeftSiblingKey();

  /**
   * Is there a right sibling?
   * 
   * @return True if there is a right sibling. False else.
   */
  public boolean hasRightSibling();

  /**
   * Get right sibling key of currently selected node.
   * 
   * @return Right sibling key of currently selected node.
   */
  public long getRightSiblingKey();

  /**
   * Get child count (including element and text nodes) of currently selected
   * node.
   * 
   * @return Child count of currently selected node.
   */
  public long getChildCount();

  /**
   * Get attribute count (including attribute nodes) of currently selected
   * node.
   * 
   * @return Attribute count of currently selected node.
   */
  public int getAttributeCount();

  /**
   * Get local part key of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Local part key of attribute at given index.
   */
  public int getAttributeLocalPartKey(final int index);

  /**
   * Get local part of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Local part of attribute at given index.
   */
  public String getAttributeLocalPart(final int index);

  /**
   * Get prefix key of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Prefix key of attribute at given index.
   */
  public int getAttributePrefixKey(final int index);

  /**
   * Get prefix of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Prefix of attribute at given index.
   */
  public String getAttributePrefix(final int index);

  /**
   * Get URI key of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return URI key of attribute at given index.
   */
  public int getAttributeURIKey(final int index);

  /**
   * Get URI of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return URI of attribute at given index.
   */
  public String getAttributeURI(final int index);

  /**
   * Get type of value of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Type of value of attribute at given index.
   */
  public int getAttributeValueType(final int index);

  /**
   * Get value of attribute at given index.
   * 
   * @param index Index of attribute [0..getAttributeCount()].
   * @return Value of attribute at given index.
   */
  public byte[] getAttributeValue(final int index);

  /**
   * Get namespace declaration count of currently selected node.
   * 
   * @return Namespace declaration count of currently selected node.
   */
  public int getNamespaceCount();

  /**
   * Get prefix key of namespace at given index.
   * 
   * @param index Index of namespace [0..getNamespaceCount()].
   * @return Prefix key of namespace at given index.
   */
  public int getNamespacePrefixKey(final int index);

  /**
   * Get prefix of namespace at given index.
   * 
   * @param index Index of namespace [0..getNamespaceCount()].
   * @return Prefix of namespace at given index.
   */
  public String getNamespacePrefix(final int index);

  /**
   * Get URI key of namespace at given index.
   * 
   * @param index Index of namespace [0..getNamespaceCount()].
   * @return URI key of namespace at given index.
   */
  public int getNamespaceURIKey(final int index);

  /**
   * Get URI of namespace at given index.
   * 
   * @param index Index of namespace [0..getNamespaceCount()].
   * @return URI of namespace at given index.
   */
  public String getNamespaceURI(final int index);

  /**
   * Get kind of node.
   * 
   * @return Kind of node.
   */
  public int getKind();

  /**
   * Is this node the document root node?
   * 
   * @return True if it is the document root node, false else.
   */
  public boolean isDocumentRootKind();

  /**
   * Is node a element?
   * 
   * @return True if node is element. False else.
   */
  public boolean isElementKind();

  /**
   * Is node a attribute?
   * 
   * @return True if node is attribute. False else.
   */
  public boolean isAttributeKind();

  /**
   * Is node a text?
   * 
   * @return True if node is text. False else.
   */
  public boolean isTextKind();

  /**
   * Is node a full text?
   * 
   * @return True if node is full text. False else.
   */
  public boolean isFullTextKind();

  /**
   * Is node a full text leaf?
   * 
   * @return True if node is full text leaf. False else.
   */
  public boolean isFullTextLeafKind();

  /**
   * Is node a full text root?
   * 
   * @return True if node is full text root. False else.
   */
  public boolean isFullTextRootKind();

  /**
   * Get local part key of node.
   * 
   * @return Local part key of node.
   */
  public int getLocalPartKey();

  /**
   * Get local part of node.
   * 
   * @return Local part of node.
   */
  public String getLocalPart();

  /**
   * Get URI key of node. Note that this actually is an IRI but the
   * W3C decided to continue using URI not to confuse anyone.
   * 
   * @return URI key of node.
   */
  public int getURIKey();

  /**
   * Get URI of node. Note that this actually is an IRI but the
   * W3C decided to continue using URI not to confuse anyone.
   * 
   * @return URI of node.
   */
  public String getURI();

  /**
   * Get prefix key of node.
   * 
   * @return Prefix key of node.
   */
  public int getPrefixKey();

  /**
   * Get prefix of node.
   * 
   * @return Prefix of node.
   */
  public String getPrefix();

  /**
   * Get type of node value.
   * 
   * @return Type of node value.
   */
  public int getValueType();

  /**
   * Get atomized value of node.
   * 
   * @return Atomized value of node.
   */
  public String getAtomizedValue();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public byte[] getValueAsByteArray();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public String getValueAsString();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public int getValueAsInt();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public long getValueAsLong();

  /**
   * Get key for given name. This is used for efficient name testing.
   * 
   * @param name Name, i.e., local part, URI, or prefix.
   * @return Internal key assigned to given name.
   */
  public int keyForName(final String name);

  /**
   * Get name for key. This is used for efficient key testing.
   * 
   * @param key Key, i.e., local part key, URI key, or prefix key.
   * @return Byte array containing name for given key.
   */
  public String nameForKey(final int key);

  /**
   * Close shared read transaction and immediately release all resources.
   * 
   * This is an idempotent operation and does nothing if the transaction is
   * already closed.
   */
  public void close();

}
