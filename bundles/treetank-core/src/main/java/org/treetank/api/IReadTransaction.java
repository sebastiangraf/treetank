/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.api;

import javax.xml.namespace.QName;

import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.*;
import org.treetank.service.xml.xpath.AtomicValue;

/**
 * <h1>IReadTransaction</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to access nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount encoding. This encoding keeps the
 * children ordered but has no knowledge of the global node ordering. The underlying tree is accessed in a
 * cursor-like fashion.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 * <ol>
 * <li>Only a single thread accesses each IReadTransaction instance.</li>
 * <li><strong>Precondition</strong> before moving cursor:
 * <code>IReadTransaction.getRelatedNode().getNodeKey() == n</code>.</li>
 * <li><strong>Postcondition</strong> after moving cursor: <code>(IReadTransaction.moveX() == true &&
 *       IReadTransaction.getRelatedNode().getNodeKey() == m) ||
 *       (IReadTransaction.moveX() == false &&
 *       IReadTransaction.getRelatedNode().getNodeKey() == n)</code>.</li>
 * </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 * 
 * <pre>
 *   final IReadTransaction rtx = session.beginReadTransaction();
 *   
 *   // Either test before moving...
 *   if (rtx.getRelatedNode().hasFirstChild()) {
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
 *   if (rtx.getRelatedNode().isElement() &amp;&amp; 
 *   rtx.getRelatedNode().getName().equalsIgnoreCase(&quot;foo&quot;) {
 *     ...
 *   }
 *   
 *   // Access value of first attribute of element.
 *   if (rtx.getRelatedNode().isElement() &amp;&amp; (rtx.getRelatedNode().getAttributeCount() &gt; 0)) {
 *     rtx.moveToAttribute(0);
 *     System.out.println(UTF.parseString(rtx.getValue()));
 *   }
 *   
 *   rtx.close();
 * </pre>
 * 
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 * 
 * <pre>
 *   public final void someIReadTransactionMethod() {
 *     // This must be called to make sure the transaction is not closed.
 *     assertNotClosed();
 *     ...
 *   }
 * </pre>
 * 
 * </p>
 */
public interface IReadTransaction {

    /** String constants used by xpath. */
    String[] XPATHCONSTANTS = {
        "xs:anyType", "xs:anySimpleType", "xs:anyAtomicType", "xs:untypedAtomic", "xs:untyped", "xs:string",
        "xs:duration", "xs:yearMonthDuration", "xs:dayTimeDuration", "xs:dateTime", "xs:time", "xs:date",
        "xs:gYearMonth", "xs:gYear", "xs:gMonthDay", "xs:gDay", "xs:gMonth", "xs:boolean", "xs:base64Binary",
        "xs:hexBinary", "xs:anyURI", "xs:QName", "xs:NOTATION", "xs:float", "xs:double", "xs:pDecimal",
        "xs:decimal", "xs:integer", "xs:long", "xs:int", "xs:short", "xs:byte", "xs:nonPositiveInteger",
        "xs:negativeInteger", "xs:nonNegativeInteger", "xs:positiveInteger", "xs:unsignedLong",
        "xs:unsignedInt", "xs:unsignedShort", "xs:unsignedByte", "xs:normalizedString", "xs:token",
        "xs:language", "xs:name", "xs:NCName", "xs:ID", "xs:IDREF", "xs:ENTITY", "xs:IDREFS", "xs:NMTOKEN",
        "xs:NMTOKENS",
    };

    /**
     * Get ID of transaction.
     * 
     * @return ID of transaction.
     */
    long getTransactionID();

    /**
     * What is the revision number of this transaction?
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return Immutable revision number of this IReadTransaction.
     */
    long getRevisionNumber() throws TTIOException;

    /**
     * UNIX-style timestamp of the commit of the revision.
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return Timestamp of revision commit.
     */
    long getRevisionTimestamp() throws TTIOException;

    /**
     * Getting the maximum nodekey avaliable in this revision.
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return the maximum nodekey
     */
    long getMaxNodeKey() throws TTIOException;

    // --- Node Selectors
    // --------------------------------------------------------

    /**
     * Move cursor to a node by its node key.
     * 
     * @param mNodeKey
     *            Key of node to select.
     * @return True if the node with the given node key is selected.
     */
    boolean moveTo(final long mNodeKey);

    /**
     * Move cursor to document root node.
     * 
     * @return True if the document root node is selected.
     */
    boolean moveToDocumentRoot();

    /**
     * Move cursor to parent node of currently selected node.
     * 
     * @return True if the parent node is selected.
     */
    boolean moveToParent();

    /**
     * Move cursor to first child node of currently selected node.
     * 
     * @return True if the first child node is selected.
     */
    boolean moveToFirstChild();

    /**
     * Move cursor to left sibling node of the currently selected node.
     * 
     * @return True if the left sibling node is selected.
     */
    boolean moveToLeftSibling();

    /**
     * Move cursor to right sibling node of the currently selected node.
     * 
     * @return True if the right sibling node is selected.
     */
    boolean moveToRightSibling();

    /**
     * Move cursor to attribute by its index.
     * 
     * @param mIndex
     *            Index of attribute to move to.
     * @return True if the attribute node is selected.
     */
    boolean moveToAttribute(final int mIndex);

    /**
     * Move cursor to namespace declaration by its index.
     * 
     * @param mIndex
     *            Index of attribute to move to.
     * @return True if the namespace node is selected.
     */
    boolean moveToNamespace(final int mIndex);

    // --- Node Getters
    // ----------------------------------------------------------

    /**
     * Getting the value of the current node.
     * 
     * @return the current value of the node
     */
    String getValueOfCurrentNode();

    /**
     * Getting the name of a current node.
     * 
     * @return the {@link QName} of the node
     */
    QName getQNameOfCurrentNode();

    /**
     * Getting the type of the current node.
     * 
     * @return the normal type of the node
     */
    String getTypeOfCurrentNode();

    /**
     * Get key for given name. This is used for efficient name testing.
     * 
     * @param mName
     *            Name, i.e., local part, URI, or prefix.
     * @return Internal key assigned to given name.
     */
    int keyForName(final String mName);

    /**
     * Get name for key. This is used for efficient key testing.
     * 
     * @param mKey
     *            Key, i.e., local part key, URI key, or prefix key.
     * @return String containing name for given key.
     */
    String nameForKey(final int mKey);

    /**
     * Get raw name for key. This is used for efficient key testing.
     * 
     * @param mKey
     *            Key, i.e., local part key, URI key, or prefix key.
     * @return Byte array containing name for given key.
     */
    byte[] rawNameForKey(final int mKey);

    /**
     * Get item list containing volatile items such as atoms or fragments.
     * 
     * @return Item list.
     */
    IItemList getItemList();

    // /**
    // * Getting the current node.
    // *
    // * @return the node
    // */
    // IItem getNode();

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link DummyNode} instance
     * @return the node
     */
    DummyNode getNode(final DummyNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link TextNode} instance
     * @return the node
     */
    TextNode getNode(final TextNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link AtomicValue} instance
     * @return the node
     */
    IItem getNode(final AtomicValue paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link ElementNode} instance
     * @return the node
     */
    ElementNode getNode(final ElementNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link AttributeNode} instance
     * @return the node
     */
    AttributeNode getNode(final AttributeNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link NamespaceNode} instance
     * @return the node
     */
    NamespaceNode getNode(final NamespaceNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link DeletedNode} instance
     * @return the node
     */
    DeletedNode getNode(final DeletedNode paramNode);

    /**
     * Getting the current node.
     * 
     * @param paramNode
     *            {@link DocumentRootNode} instance
     * @return the node
     */
    DocumentRootNode getNode(final DocumentRootNode paramNode);

    /**
     * Get the current node.
     * 
     * @param <T>
     *            node which implements {@link IItem}
     * @return current node which implements {@link IItem}
     */
    <T extends IItem> T getNode();

    /**
     * Close shared read transaction and immediately release all resources.
     * 
     * This is an idempotent operation and does nothing if the transaction is
     * already closed.
     * 
     * @throws AbsTTException
     *             If can't close Read Transaction.
     */
    void close() throws AbsTTException;

    /**
     * Is this transaction closed?
     * 
     * @return true if closed, false otherwise
     */
    boolean isClosed();

    /**
     * This method returns the current {@link IItem} as a {@link IStructuralItem}.
     * 
     * @return the current node as {@link IStructuralItem} if possible, otherwise wrap the {@link AbsNode} in
     *         a {@link IStructuralItem}
     */
    IStructuralItem getStructuralNode();
}
