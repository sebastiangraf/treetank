/*
 * Copyright (c) 2008, Johannes Lichtenberger (HiWi), University of Konstanz
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

package org.treetank.xmllayer;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>TreeTankStreamWriter</h1>
 * 
 * <p>
 * XMLStreamWriter implementation writing streamed XML into a TreeTank. The
 * incoming document node will be written as the first child of the currently
 * selected node in the TreeTank transaction. The following nodes will be
 * written as the descendants of just created node.
 * 
 * Must be used within a single thread.
 * </p>
 */
public final class TreeTankStreamWriter implements XMLStreamWriter {

  /** Write transaction to write to. */
  private final IWriteTransaction mWTX;

  /** Namespace Context. */
  private NamespaceContext nspContext;

  /** Flag to track if start tag is opened. */
  private boolean startTagOpened = false;

  /** Stack containing left sibling nodeKey of each level. */
  private final FastStack<Long> mLeftSiblingKeyStack;

  /** Aggregated pending text node. */
  private final StringBuilder mCharacters;

  /**
   * Initialize XMLStreamWriter implementation with transaction.
   * 
   * @param wtx
   *          Transaction with cursor pointing to the parent node of the tree to
   *          be inserted.
   */
  public TreeTankStreamWriter(final IWriteTransaction wtx) {
    nspContext = new NamespaceContextImpl();
    mWTX = wtx;
    mLeftSiblingKeyStack = new FastStack<Long>();
    mCharacters = new StringBuilder();
  }

  /**
   * Close this TreeTankStreamWriter by closing the underlying 
   * Write-Transaction.
   */
  public final void close() {
    mWTX.close();
  }

  /**
   * Flush this TreeTankStreamWriter.
   */
  public final void flush() {
    // Nothing to do here.
  }

  /**
   * Return <code>NamespaceContext</code> being used by the writer.
   * 
   * @return Current NamespaceContext.
   */
  public final NamespaceContext getNamespaceContext() {
    return nspContext;
  }

  /**
   * Return a prefix associated with specified uri, or null if the
   * uri is unknown.
   * 
   * @param uri
   *            The URI from which to get the prefix, which is bound to it. 
   */
  public final String getPrefix(final String uri) throws XMLStreamException {

    if (uri == null) {
      throw new XMLStreamException("URI may not be null.");
    }

    return nspContext.getPrefix(uri);
  }

  /**
   * {@inheritDoc}
   */
  public final Object getProperty(final String arg0) {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");
  }

  /**
   * Binds a URI to the default namespace This URI is bound in 
   * the scope of the current START_ELEMENT / END_ELEMENT pair. 
   * If this method is called before a START_ELEMENT has been 
   * written the uri is bound in the root scope.
   * 
   * @param uri
   *            The uri to bind to the default namespace, may 
   *            be null.
   */
  public final void setDefaultNamespace(final String uri)
      throws XMLStreamException {
    ((NamespaceContextImpl) nspContext).setNamespace("", uri);
  }

  /**
   * Sets the current namespace context for prefix and uri bindings. 
   * This context becomes the root namespace context for writing and 
   * will replace the current root namespace context. Subsequent calls 
   * to setPrefix and setDefaultNamespace will bind namespaces using 
   * the context passed to the method as the root context for resolving 
   * namespaces. This method may only be called once at the start of the 
   * document. It does not cause the namespaces to be declared. If a 
   * namespace URI to prefix mapping is found in the namespace context 
   * it is treated as declared and the prefix may be used by the 
   * StreamWriter.
   * 
   * @param context
   *                The namespace context to use for this writer, may 
   *                not be null.
   * @throws XMLStreamException
   *                If context is null.
   */
  public final void setNamespaceContext(final NamespaceContext context)
      throws XMLStreamException {
    if (context == null) {
      throw new XMLStreamException("context may not be null.");
    }

    nspContext = context;
  }

  /**
   * Sets the prefix the uri is bound to. This prefix is bound in the scope 
   * of the current START_ELEMENT / END_ELEMENT pair. If this method is 
   * called before a START_ELEMENT has been written the prefix is bound in 
   * the root scope.
   * 
   * @param prefix
   *               The prefix to bind to the uri, may not be null.
   * @param uri
   *               The uri to bind to the prefix, may be null.
   * @throws XMLStreamException
   *               If either prefix or uri is null.
   */
  public final void setPrefix(final String prefix, final String uri)
      throws XMLStreamException {

    if (prefix == null) {
      throw new XMLStreamException("setPrefix(): Prefix cannot be null!");
    }

    if (uri == null) {
      throw new XMLStreamException("setPrefix(): URI cannot be null!");
    }

    ((NamespaceContextImpl) nspContext).setNamespace(prefix, uri);
  }

  /**
   * Writes an attribute to the output stream without a prefix.
   * 
   * @param localName
   *                  The local name of the attribute.
   * @param value
   *                  The value of the attribute.
   * @throws IllegalStateException
   *                  If the current state does not allow Attribute writing.
   * @throws XMLStreamStreamException
   */
  public final void writeAttribute(final String localName, final String value)
      throws XMLStreamException {
    if (!startTagOpened) {
      throw new IllegalStateException(
          "writeAttribute(): Attribute not associated with any element!");
    }

    mWTX.insertAttribute(localName, "", value);
  }

  /**
   * Writes an attribute to the output stream.
   * 
   * @param namespaceURI
   *                  The uri of the prefix for this attribute.
   * @param localName
   *                  The local name of the attribute.
   * @param value
   *                  The value of the attribute.
   * @throws IllegalStateException
   *                  If the current state does not allow Attribute 
   *                  writing.
   * @throws XMLStreamException
   *                  If the namespace URI is null or has not been 
   *                  bound to a prefix.
   */
  public final void writeAttribute(
      final String namespaceURI,
      final String localName,
      final String value) throws XMLStreamException {

    if (!startTagOpened) {
      throw new IllegalStateException("Attribute not associated "
          + "with any element!");
    }

    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null!");
    }

    String prefix = nspContext.getPrefix(namespaceURI);

    if (prefix == null) {
      throw new XMLStreamException("Prefix cannot be null!");
    }

    mWTX.insertAttribute(prefix + ":" + localName, "", value);
  }

  /**
   * Writes an attribute to the output stream.
   * 
   * @param prefix
   *               The prefix for this attribute.
   * @param namespaceURI
   *                  The uri of the prefix for this attribute.
   * @param localName
   *                  The local name of the attribute.
   * @param value
   *                  The value of the attribute.
   * @throws IllegalStateException
   *                  If attribute isn't associated with an element.
   * @throws XMLStreamException
   *                  If namespaceURI or localName is null or prefix 
   *                  either is null or an empty string and 
   *                  namespaceURI isn't empty.
   */
  public final void writeAttribute(
      final String prefix,
      final String namespaceURI,
      final String localName,
      final String value) throws XMLStreamException {

    if (!startTagOpened) {
      throw new IllegalStateException("Attribute not associated "
          + "with any element!");
    }

    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null!");
    }

    if (localName == null) {
      throw new XMLStreamException("Local name cannot be null!");
    }

    if (prefix == null || prefix.equals("")) {
      if (!namespaceURI.equals("")) {
        throw new XMLStreamException("Prefix cannot be null or empty!");
      }

      mWTX.insertAttribute(localName, namespaceURI, value);
    }

    /*
     * If prefix isn't equal to the official xml namespace prefix or the
     * namespaceURI isn't equal to the official xml namespace uri.
     */
    if (!prefix.equals(XMLConstants.XML_NS_PREFIX)
        || !namespaceURI.equals(XMLConstants.XML_NS_URI)) {
      mWTX.insertAttribute(prefix + ":" + localName, namespaceURI, value);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void writeCData(final String cdata) throws XMLStreamException {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");
  }

  /**
   * Write text to the output.
   * 
   * @param text
   *             The value to write.
   * @throws XMLStreamException
   */
  public final void writeCharacters(final String text)
      throws XMLStreamException {
    if (startTagOpened) {
      closeStartTag();
    }

    mCharacters.append(text);

    //    // Insert text node and maintain stacks.
    //    long key;
    //
    //    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
    //      key = mWTX.insertTextAsFirstChild(data);
    //    } else {
    //      key = mWTX.insertTextAsRightSibling(data);
    //    }
    //
    //    mLeftSiblingKeyStack.pop();
    //    mLeftSiblingKeyStack.push(key);
  }

  /**
   * Write text to the output.
   * 
   * @param text
   *             The value to write.
   * @param start
   *             The starting position in the array.
   * @param len
   *             The number of characters to write.
   * @throws XMLStreamException
   */
  public final void writeCharacters(
      final char[] text,
      final int start,
      final int len) throws XMLStreamException {
    if (startTagOpened) {
      closeStartTag();
    }

    mCharacters.append(text, start, len);

    //    char[] tempData = new char[length];
    //
    //    for (int i = 0; i < length; i++) {
    //      tempData[i] = data[start + i];
    //    }
    //
    //    String strData = tempData.toString();
    //
    //    mWTX.insertTextAsFirstChild(strData);
  }

  /**
   * Insert aggregated pending text node.
   * 
   * @throws Exception of any kind.
   */
  private final void insertPendingText() {

    final String text = mCharacters.toString().trim();
    mCharacters.setLength(0);

    if (text.length() > 0) {

      // Insert text node and maintain stacks.
      long key;
      if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
        key = mWTX.insertTextAsFirstChild(text);
      } else {
        key = mWTX.insertTextAsRightSibling(text);
      }
      mLeftSiblingKeyStack.pop();
      mLeftSiblingKeyStack.push(key);

    }

  }

  /**
   * Marks close of start tag.
   */
  private final void closeStartTag() {
    startTagOpened = false;
  }

  /**
   * {@inheritDoc}
   */
  public final void writeComment(final String comment)
      throws XMLStreamException {
    // TODO: Currently not implemented?
    throw new UnsupportedOperationException("Currently not "
        + "supported by TreeTank.");
  }

  /**
   * {@inheritDoc}
   */
  public final void writeDTD(final String dtd) throws XMLStreamException {
    throw new UnsupportedOperationException("Currently not "
        + "supported by TreeTank.");
  }

  /**
   * Writes the default namespace to the stream.
   * 
   * @param namespaceURI
   *                     The uri to bind the default namespace to.
   * @throws IllegalStateException
   *                     If the current state doesn't allow namespace writing.
   */
  public final void writeDefaultNamespace(final String namespaceURI)
      throws XMLStreamException {
    if (!startTagOpened) {
      throw new IllegalStateException(
          "Namespace Attribute not associated with any element!");
    }

    mWTX.insertNamespace(namespaceURI, "xmlns");
  }

  /**
   * Writes an empty element tag to the output.
   * 
   * @param localName
   *                  Local name of the tag, may not be null.
   * @throws XMLStreamException
   *                  If localName is null.
   */
  public final void writeEmptyElement(final String localName)
      throws XMLStreamException {

    if (localName == null) {
      throw new XMLStreamException("localName must not be null!");
    }

    // Insert text node.
    insertPendingText();

    // Insert element node and maintain stack.      
    long key;

    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
      key = mWTX.insertElementAsFirstChild(localName, "");
    } else {
      key = mWTX.insertElementAsRightSibling(localName, "");
    }

    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * Writes an empty element tag to the output.
   * 
   * @param namespaceURI
   *                  The uri to bind the tag to, may not be null.
   * @param localName
   *                  Local name of the tag, may not be null.
   * @throws IllegalStateException
   *                  If namespaceURI or localName is null.
   */
  public final void writeEmptyElement(
      final String namespaceURI,
      final String localName) throws XMLStreamException {
    if (namespaceURI == null) {
      throw new IllegalStateException("namespaceURI may not be null!");
    }

    if (localName == null) {
      throw new IllegalStateException("localName may not be null!");
    }

    // Insert text node.
    insertPendingText();

    String prefix = nspContext.getPrefix(namespaceURI);

    writeEmptyElement(prefix, localName, namespaceURI);
  }

  /**
   * Writes an empty element tag to the output.
   * 
   * @param prefix
   *                  The prefix of the tag, may not be null.
   * @param namespaceURI
   *                  The uri to bind the tag to, may not be null.
   * @param localName
   *                  Local name of the tag, may not be null.
   * @throws XMLStreamException
   *                  If prefix or namespaceURI or localName is null.
   */
  public final void writeEmptyElement(
      final String prefix,
      final String localName,
      final String namespaceURI) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }

    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null");
    }

    if (prefix == null) {
      throw new XMLStreamException("NamespaceURI "
          + namespaceURI
          + " has not been bound to any prefix");
    }

    if (localName == null) {
      throw new XMLStreamException("localName must not be null!");
    }

    // Insert text node.
    insertPendingText();

    // Insert element node and maintain stack.      
    long key;

    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
      if ((prefix != null) && (prefix != XMLConstants.DEFAULT_NS_PREFIX)) {
        key =
            mWTX.insertElementAsFirstChild(
                prefix + ":" + localName,
                namespaceURI);
      } else {
        key = mWTX.insertElementAsFirstChild(localName, namespaceURI);
      }
    } else {
      if ((prefix != null) && (prefix != XMLConstants.DEFAULT_NS_PREFIX)) {
        key =
            mWTX.insertElementAsRightSibling(
                prefix + ":" + localName,
                namespaceURI);
      } else {
        key = mWTX.insertElementAsRightSibling(localName, namespaceURI);
      }
    }

    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * Does nothing.
   */
  public final void writeEndDocument() throws XMLStreamException {
    //    if (startTagOpened) {
    //      startTagOpened = false;
    //    }
  }

  /**
   * Writes an end tag.
   */
  public final void writeEndElement() {
    if (startTagOpened) {
      startTagOpened = false;
    }

    insertPendingText();
    mLeftSiblingKeyStack.pop();
    mWTX.moveTo(mLeftSiblingKeyStack.peek());
  }

  /**
   * {@inheritDoc}
   */
  public final void writeEntityRef(final String refName)
      throws XMLStreamException {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");
  }

  /**
   * Writes the default namespace to the stream.
   * 
   * @param prefix
   *               The prefix to bind this namespace to
   * @param namespaceURI
   *               The uri to bind the default namespace to.
   * @throws XMLStreamException 
   * @throws IllegalStateException
   *               If the current state does not allow Namespace writing.
   */
  public final void writeNamespace(
      final String prefix,
      final String namespaceURI) throws XMLStreamException {
    if (!startTagOpened) {
      throw new IllegalStateException(
          "Namespace not associated with any element or attribute!");
    }

    if (prefix == null || prefix.equals("") || prefix.equals("xmlns")) {
      writeDefaultNamespace(namespaceURI);
    }

    mWTX.insertNamespace(namespaceURI, prefix);
  }

  /**
   * {@inheritDoc}
   */
  public final void writeProcessingInstruction(final String target)
      throws XMLStreamException {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");

  }

  /**
   * {@inheritDoc}
   */
  public final void writeProcessingInstruction(
      final String target,
      final String data) throws XMLStreamException {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");
  }

  /**
   * Pushes NULL_NODE_KEY to the left sibling key stack.
   */
  public final void writeStartDocument() {
    mLeftSiblingKeyStack.push(mWTX.getLeftSiblingKey());
  }

  /**
   * Calls writeStartDocument.
   * 
   * @param version version-Attribute in XML-Prolog.
   */
  public final void writeStartDocument(final String version)
      throws XMLStreamException {
    writeStartDocument();
  }

  /**
   * Calls writeStartDocument.
   * 
   * @param version 
   *                  version-Attribute in XML-Prolog.
   * @param encoding
   *                  encoding-Attribute in XML-Prolog.
   */
  public final void writeStartDocument(
      final String encoding,
      final String version) throws XMLStreamException {
    writeStartDocument();
  }

  /**
   * Writes a start tag to TreeTank. All writeStartElement methods open 
   * a new scope in the internal namespace context. Writing the 
   * corresponding EndElement causes the scope to be closed.
   * 
   * @param localName
   *                  The localName of the element.
   * @throws IllegalStateException
   *                  If localName is null.
   */
  public final void writeStartElement(final String localName)
      throws XMLStreamException {
    if (localName == null) {
      throw new IllegalStateException("Local Name must not be null");
    }

    startTagOpened = true;

    // Insert text node.
    insertPendingText();

    // Insert element node and maintain stack.      
    long key;

    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
      key = mWTX.insertElementAsFirstChild(localName, "");
    } else {
      key = mWTX.insertElementAsRightSibling(localName, "");
    }

    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * Writes a start tag.
   * 
   * @param namespaceURI
   *                  The namespaceURI of the prefix to use, may not be null.
   * @param localName
   *                  The localName of the element.
   * @throws IllegalStateException
   *                  If localName or namespaceURI is null.
   */
  public final void writeStartElement(
      final String namespaceURI,
      final String localName) throws XMLStreamException {
    if (localName == null) {
      throw new IllegalStateException("Local Name must not be null!");
    }

    if (namespaceURI == null) {
      throw new IllegalStateException("NamespaceURI must not be null!");
    }

    startTagOpened = true;

    // Insert text node.
    insertPendingText();

    String prefix = nspContext.getPrefix(namespaceURI);

    // Insert element node and maintain stack.      
    long key;

    if (prefix != null) {
      if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
        key =
            mWTX.insertElementAsFirstChild(
                prefix + ":" + localName,
                namespaceURI);
      } else {
        key =
            mWTX.insertElementAsRightSibling(
                prefix + ":" + localName,
                namespaceURI);
      }
    } else {

      if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
        key = mWTX.insertElementAsFirstChild(localName, namespaceURI);
      } else {
        key = mWTX.insertElementAsRightSibling(localName, namespaceURI);
      }
    }

    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * Writes a start tag.
   * 
   * @param prefix
   *                  The prefix of the tag, may not be null.
   * @param namespaceURI
   *                  The namespaceURI of the prefix to use, may not be null.
   * @param localName
   *                  The localName of the element.
   * @throws IllegalStateException
   *                  If localName, namespaceURI or prefix is null.
   */
  public final void writeStartElement(
      final String prefix,
      final String localName,
      final String namespaceURI) throws XMLStreamException {
    if (localName == null) {
      throw new IllegalStateException("Local Name must not be null!");
    }

    if (namespaceURI == null) {
      throw new IllegalStateException("NamespaceURI must not be null!");
    }

    if (prefix == null) {
      throw new IllegalStateException("Prefix must not be null!");
    }

    // Insert text node.
    insertPendingText();

    startTagOpened = true;

    // Insert element node and maintain stack.      
    long key;

    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
      key =
          mWTX
              .insertElementAsFirstChild(prefix + ":" + localName, namespaceURI);
    } else {
      key =
          mWTX.insertElementAsRightSibling(
              prefix + ":" + localName,
              namespaceURI);
    }

    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
  }
}
