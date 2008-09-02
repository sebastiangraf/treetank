/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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


import org.treetank.xpath.types.Type;
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
 *   <li>Only a single thread accesses each IReadTransaction instance.</li>
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
 *   if (rtx.isElement() && rtx.getName().equalsIgnoreCase("foo") {
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
  
  /** String constants used by xpath. */
  public static final String[] XPATHCONSTANTS = {
    "xs:anyType", "xs:anySimpleType", "xs:anyAtomicType", "xs:untypedAtomic",
    "xs:untyped", "xs:string", "xs:duration",  "xs:yearMonthDuration",
    "xs:dayTimeDuration", "xs:dateTime", "xs:time", "xs:date", "xs:gYearMonth", 
    "xs:gYear", "xs:gMonthDay", "xs:gDay", "xs:gMonth", "xs:boolean", 
    "xs:base64Binary", "xs:hexBinary", "xs:anyURI", "xs:QName", "xs:NOTATION", 
    "xs:float", "xs:double", "xs:pDecimal", "xs:decimal", "xs:integer", 
    "xs:long", "xs:int", "xs:short", "xs:byte", "xs:nonPositiveInteger",  
    "xs:negativeInteger", "xs:nonNegativeInteger", "xs:positiveInteger", 
    "xs:unsignedLong", "xs:unsignedInt", "xs:unsignedShort", "xs:unsignedByte", 
    "xs:normalizedString", "xs:token", "xs:language", "xs:name", "xs:NCName", 
    "xs:ID", "xs:IDREF", "xs:ENTITY", "xs:IDREFS", "xs:NMTOKEN", "xs:NMTOKENS", 
  };
 

  /**
   * Get ID of transaction.
   * 
   * @return ID of transaction.
   */
  public long getTransactionID();

  /**
   * What is the revision number of this IReadTransaction?
   * 
   * @return Immutable revision number of this IReadTransaction.
   */
  public long getRevisionNumber();

  /**
   * UNIX-style timestamp of the commit of the revision.
   * 
   * @return Timestamp of revision commit.
   */
  public long getRevisionTimestamp();

  /**
   * How many nodes are stored in the revision of this IReadTransaction?
   * 
   * @return Immutable number of nodes of this IReadTransaction.
   */
  public long getNodeCount();

  // --- Node Selectors --------------------------------------------------------

  /**
   * Move cursor to a node by its node key.
   * 
   * @param nodeKey Key of node to select.
   * @return True if the node with the given node key is selected.
   */
  public boolean moveTo(final long nodeKey);

  /**
   * Move cursor to document root node.
   * 
   * @return True if the document root node is selected.
   */
  public boolean moveToDocumentRoot();

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

  /**
   * Move cursor to namespace declaration by its index.
   * 
   * @param index Index of attribute to move to.
   * @return True if the namespace node is selected.
   */
  public boolean moveToNamespace(final int index);

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
   * Get namespace declaration count of currently selected node.
   * 
   * @return Namespace declaration count of currently selected node.
   */
  public int getNamespaceCount();

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
   * Get qualified name key of node.
   * 
   * @return Qualified name key of node.
   */
  public int getNameKey();

  /**
   * Get qualified name of node.
   * 
   * @return Qualified name of node.
   */
  public String getName();

  /**
   * Get qualified raw name of node.
   * 
   * @return Qualified raw name of node.
   */
  public byte[] getRawName();

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
   * Get type key of node value.
   * 
   * @return Type of node value.
   */
  public int getTypeKey();

  /**
   * Get type of node value.
   * 
   * @return Type of node value.
   */
  public String getType();

  /**
   * Get raw type of node value.
   * 
   * @return Type of node value.
   */
  public byte[] getRawType();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public byte[] getRawValue();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public String getValue();

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
   * @return String containing name for given key.
   */
  public String nameForKey(final int key);

  /**
   * Get raw name for key. This is used for efficient key testing.
   * 
   * @param key Key, i.e., local part key, URI key, or prefix key.
   * @return Byte array containing name for given key.
   */
  public byte[] rawNameForKey(final int key);

  /**
   * Get item list containing volatile items such as atoms or fragments.
   * 
   * @return Item list.
   */
  public IItemList getItemList();

  /**
   * Close shared read transaction and immediately release all resources.
   * 
   * This is an idempotent operation and does nothing if the transaction is
   * already closed.
   */
  public void close();

}
