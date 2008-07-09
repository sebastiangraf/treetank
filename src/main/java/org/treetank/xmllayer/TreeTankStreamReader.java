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

import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.ChildAxis;
import org.treetank.axislayer.FilterAxis;
import org.treetank.axislayer.TextFilter;
import org.treetank.utils.FastStack;

/**
 * <h1>TreeTankStreamReader</h1>
 * 
 * <p>
 * XMLStreamReader implementation streaming the descendants-or-self nodes of the
 * currently selected node.
 * 
 * Must be used within a single thread.
 * </p>
 */
public final class TreeTankStreamReader implements XMLStreamReader {

  private static final String VERSION = "1.0";

  /** Transaction to read from (is the same as the mAxis). */
  private final IReadTransaction mRTX;

  /** Stack for reading end element. */
  private final FastStack<Long> mStack;

  /** Namespace context. */
  private final NamespaceContextImpl nspContext;

  /** All StartTags closed? */
  private boolean closeElements = false;

  /** Is it the first element, which should be closed? */
  private boolean firstElement = false;

  /** Stack for remembering next nodeKey in document order. */
  private FastStack<Long> mRightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /** Key of last found node. */
  private long mKey;

  /** Make sure next() can only be called after hasNext(). */
  private boolean mNext;

  /** Key of node where axis started. */
  private long mStartKey;

  /** Map TreeTank events/node kinds to xml events. */
  private HashMap<Integer, Integer> mapEvents;

  /** Current EventType. */
  private int eventType;

  /** Run next() once after endElements has been set to true. */
  private boolean firstRun = true;

  /** Determines if it's an empty element. */
  private boolean isEmptyElement = false;

  /** Key from next node after elements are closed. */
  private long key = 0L;

  /**
   * Initialize XMLStreamReader implementation with transaction. The cursor
   * points to the node the XMLStreamReader starts to read.
   * 
   * @param rtx
   *          Transaction with cursor pointing to start node.
   */
  public TreeTankStreamReader(final IReadTransaction rtx) {
    mRTX = rtx;
    mNextKey = mRTX.getNodeKey();
    mStartKey = mRTX.getNodeKey();
    mRightSiblingKeyStack = new FastStack<Long>();
    mStack = new FastStack<Long>();
    nspContext = new NamespaceContextImpl();

    mapEvents = new HashMap<Integer, Integer>();
    mapEvents.put(IReadTransaction.ELEMENT_KIND, XMLEvent.START_ELEMENT);
    mapEvents.put(IReadTransaction.ATTRIBUTE_KIND, XMLEvent.ATTRIBUTE);
    mapEvents.put(IReadTransaction.TEXT_KIND, XMLEvent.CHARACTERS);
    mapEvents.put(IReadTransaction.NAMESPACE_KIND, XMLEvent.NAMESPACE);
    mapEvents.put(IReadTransaction.DOCUMENT_ROOT_KIND, XMLEvent.START_DOCUMENT);
    mapEvents.put(
        IReadTransaction.PROCESSING_INSTRUCTION_KIND,
        XMLEvent.PROCESSING_INSTRUCTION);

    eventType = mapEvents.get(mRTX.getKind());
  }

  /**
   * {@inheritDoc}
   */
  public final void close() throws XMLStreamException {
    // Nothing to do here.
  }

  /**
   * Returns the count of attributes on this ELEMENT. This method is only valid
   * on an ELEMENT. This count excludes namespace definitions. Attribute indices
   * are zero-based.
   * 
   * @return Returns the number of attributes.
   * @throw IllegalStateException If this is not an ELEMENT.
   */
  // Checked.
  public final int getAttributeCount() {

    if (getEventType() != XMLEvent.START_ELEMENT) {
      throw new IllegalStateException("getAttributeCount(): Wrong event type: "
          + getEventType());
    }

    return mRTX.getAttributeCount();
  }

  /**
   * Returns the localName of the attribute at the provided index.
   * 
   * @param index
   *          The position of the attribute.
   * @return The localName of the attribute or null if the attribute isn't
   *         specified.
   * @throw IllegalStateException If this is not an ELEMENT or ATTRIBUTE.
   */
  // Checked.
  public final String getAttributeLocalName(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeLocalName(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      String localName = mRTX.getAttributeName(index);

      final int offset = localName.indexOf(":");

      if (offset > 0) {
        localName = localName.substring(offset + 1);
      }

      return localName;
    }

    return null;
  }

  /**
   * Returns the qname of the attribute at the provided index.
   * 
   * @param index
   *          The position of the element.
   * @return The QName of the attribute or null if the attribute isn't
   *         specified.
   */
  // Checked.
  public final QName getAttributeName(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeName(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      return new QName(mRTX.getAttributeName(index));
    }

    return null;
  }

  /**
   * Returns the namespace of the attribute at the provided index.
   * 
   * @param index
   *          The position of the attribute.
   * @return The namespace URI (can be null).
   * @throw IllegalStateException If this is not an ELEMENT or ATTRIBUTE.
   */
  // Checked.
  public final String getAttributeNamespace(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeValue(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      return mRTX.getNamespaceURI(index);
    }

    return null;
  }

  /**
   * Returns the prefix of this attribute at the provided index.
   * 
   * @param index
   *          The position of the attribute.
   * @return The prefix of the attribute or null if it isn't specified.
   */
  // Checked.
  public final String getAttributePrefix(final int index) {

    if (eventType != XMLEvent.START_ELEMENT
        && eventType != XMLEvent.END_ELEMENT
        && eventType != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributePrefix(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      String localName = mRTX.getAttributeName(index);

      String prefix = "";

      final int offset = localName.indexOf(":");

      if (offset != -1) {
        prefix = localName.substring(0, offset);
      }

      if (!prefix.equalsIgnoreCase("")) {
        return prefix;
      }
    }

    return null;
  }

  /**
   * Returns the XML type of the attribute at the provided index.
   * 
   * @param index
   *          The position of the attribute.
   * @return The XML type of the attribute or null if it isn't specified.
   * @throw IllegalStateException If this is not an ELEMENT or ATTRIBUTE.
   */
  public final String getAttributeType(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeValue(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      return String.valueOf(mRTX.getAttributeTypeKey(index));
    }

    return null;
  }

  /**
   * Returns the value of the attribute at the index.
   * 
   * @param index
   *          The position of the attribute.
   * @return The attribute value or null if it isn't specified.
   * @throw IllegalStateException If this is not an ELEMENT or ATTRIBUTE.
   */
  // Checked.
  public final String getAttributeValue(final int index) {
    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeValue(final int index): Wrong event type: "
              + getEventType());
    }

    if (isAttributeSpecified(index)) {
      return mRTX.getAttributeValue(index);
    }

    return null;
  }

  /**
   * Returns the normalized attribute value of the attribute with the namespace
   * and localName. If the namespaceURI is null the namespace is not checked for
   * equality
   * 
   * @param namespaceURI
   *          The namespace of the attribute.
   * @param localName
   *          The local name of the attribute, cannot be null.
   * @throw IllegalStateException If this is not an ELEMENT or ATTRIBUTE.
   * @return The attribute value or null.
   */
  public final String getAttributeValue(
      final String namespaceURI,
      final String localName) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException(
          "getAttributeValue(final String namespaceURI, "
              + "final String localName): "
              + "Wrong event type: "
              + getEventType());
    }

    for (int i = 0, l = mRTX.getAttributeCount(); i < l; i++) {
      mRTX.moveToAttribute(i);

      String prefix = nspContext.getPrefix(namespaceURI);

      if ((namespaceURI == null && mRTX.getAttributeName(i).equalsIgnoreCase(
          localName))
          || mRTX.getAttributeName(i).equalsIgnoreCase(prefix)) {
        return mRTX.getValue();
      }
    }

    return null;
  }

  /**
   * Returns the character encoding declared on the xml declaration. Returns
   * null if none was declared.
   * 
   * @return The encoding declared in the document or null.
   */
  // Checked.
  public final String getCharacterEncodingScheme() {
    return "UTF-8";
  }

  /**
   * Reads the content of a text-only element, an exception is thrown if this is
   * not a text-only element. Regardless of value of
   * javax.xml.stream.isCoalescing this method always returns coalesced content.
   * 
   * @throws IllegalStateException
   *           If the current event is not an ELEMENT or if a non text element
   *           is encountered.
   * @return Text between start and end-Tag.
   */
  // Checked.
  public final String getElementText() {

    if (getEventType() == XMLEvent.START_ELEMENT) {
      StringBuffer content = new StringBuffer();

      for (final long key : new FilterAxis(new ChildAxis(mRTX), new TextFilter(
          mRTX))) {
        content.append(mRTX.getValue());
      }

      return content.toString();
    }

    throw new IllegalStateException(
        "Parser must be on an element to read next text" + getLocation());
  }

  /**
   * Return input encoding.
   * 
   * @return UTF-8.
   */
  // Checked.
  public final String getEncoding() {

    return "UTF-8";
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final int getEventType() {

    return eventType;
  }

  /**
   * Returns the (local) name of the current event. For an ELEMENT returns the
   * (local) name of the current element. The current event must be an ELEMENT.
   * 
   * @return The localName.
   * @throw IllegalStateException The current event must be an ELEMENT.
   */
  // Checked.
  public final String getLocalName() {

    if (getEventType() == XMLEvent.START_ELEMENT
        && getEventType() == XMLEvent.END_ELEMENT) {
      throw new IllegalStateException("getLocalName(): Wrong eventType: "
          + getEventType());
    }

    String localName = mRTX.getName();

    final int offset = localName.indexOf(":");

    if (offset > 0) {
      localName = localName.substring(offset + 1);
    }

    return localName;
  }

  /**
   * {@inheritDoc}
   */
  public final Location getLocation() {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank!");
  }

  /**
   * Returns a QName for the current ELEMENT event.
   * 
   * @return The QName for the current ELEMENT event.
   * @throw IllegalStateException If this is not an ELEMENT.
   */
  // Checked.
  public final QName getName() {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException("Can not be a qname!");
    }

    return new QName(mRTX.getName());
  }

  /**
   * {@inheritDoc}
   */
  public final NamespaceContext getNamespaceContext() {

    return nspContext;
  }

  /**
   * Returns the count of namespaces declared on this ELEMENT. This method is
   * only valid on an ELEMENT or NAMESPACE.
   * 
   * @return Returns the count of namespaces declared on this specific element.
   * @throw IllegalStateException If this is not an ELEMENT or NAMESPACE.
   */
  // Checked.
  public final int getNamespaceCount() {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.NAMESPACE) {
      throw new IllegalStateException(
          "getNamespaceCount() shouldn't be invoked at: " + getEventType());
    }

    return mRTX.getNamespaceCount();
  }

  /**
   * Returns the prefix for the namespace declared at the index.
   * 
   * @param index
   *          The position of the namespace declaration.
   * @return Returns the namespace prefix.
   * @throw IllegalStateException If this is not an ELEMENT or NAMESPACE.
   */
  // Checked.
  public final String getNamespacePrefix(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.NAMESPACE) {
      throw new IllegalStateException(
          "getNamespacePrefix(final int index) shouldn't be invoked at: "
              + getEventType());
    }

    return mRTX.getNamespacePrefix(index);
  }

  /**
   * If the current event is an ELEMENT this method returns the URI of the
   * prefix or the default namespace. Returns null if the event does not have a
   * prefix.
   * 
   * @return The URI bound to this elements prefix, the default namespace, or
   *         null.
   * @throw IllegalStateException If this is not an ELEMENT.
   */
  // Checked.
  public final String getNamespaceURI() {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT) {
      throw new IllegalStateException("getNamespaceURI() shouldn't be invoked "
          + "at: "
          + getEventType());
    }

    return mRTX.getURI();
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final String getNamespaceURI(final String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException(
          "getNamespaceURI(final String prefix): Argument prefix must "
              + "not be null.");
    }

    return nspContext.getNamespaceURI(prefix);
  }

  /**
   * Returns the uri for the namespace declared at the index.
   * 
   * @param index
   *          The position of the namespace declaration.
   * @return Returns the namespace uri.
   * @throw IllegalStateException If this is not an ELEMENT or NAMESPACE.
   */
  // Checked.
  public final String getNamespaceURI(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.END_ELEMENT
        && getEventType() != XMLEvent.NAMESPACE) {
      throw new IllegalStateException(
          "getNamespaceURI(final int index) shouldn't be invoked at: "
              + getEventType());
    }

    return mRTX.getNamespaceURI(index);
  }

  /**
   * {@inheritDoc}
   */
  public final String getPIData() {
    if (getEventType() != XMLEvent.PROCESSING_INSTRUCTION) {
      return null;
    }

    return mRTX.getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPITarget() {
    if (getEventType() != XMLEvent.PROCESSING_INSTRUCTION) {
      return null;
    }

    return mRTX.getName();
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final String getPrefix() {
    String name = mRTX.getName();
    String prefix = null;

    final int offset = name.indexOf(":");

    if (offset > 0) {
      prefix = name.substring(0, offset);
    }

    return prefix;
  }

  /**
   * {@inheritDoc}
   */
  public final Object getProperty(final String name) {
    throw new UnsupportedOperationException(
        "Currently not supported by TreeTank.");
  }

  /**
   * Returns the current value of the parse event as a string, this returns the
   * string value of a TEXT event or returns the value of a COMMENT.
   * 
   * @return The current text.
   * @throw IllegalStateException 
   *            If this is not a TEXT or COMMENT node.
   */
  // Checked.
  public final String getText() {

    if (getEventType() != XMLEvent.CHARACTERS
        && getEventType() != XMLEvent.COMMENT) {
      throw new IllegalStateException("getText() Wrong event type: "
          + getEventType());
    }

    return mRTX.getValue();
  }

  /**
   * Returns an array which contains the characters from this event. This array
   * should be treated as read-only and transient. I.e. the array will contain
   * the text characters until the TreeTankStreamReader moves on to the next
   * event. Attempts to hold onto the character array beyond that time or modify
   * the contents of the array are breaches of the contract for this interface.
   * 
   * @return The current text or an empty array.
   * @throw IllegalStateException 
   *            If this state is not a valid text state.
   */
  // Checked.
  public final char[] getTextCharacters() {

    return mRTX.getValue().toCharArray();
  }

  /**
   * <p>
   * Gets the the text associated with a TEXT event. Text starting at
   * "sourceStart" is copied into "target" starting at "targetStart". Up to
   * "length" characters are copied. The number of characters actually copied is
   * returned. The "sourceStart" argument must be greater or equal to 0 and less
   * than or equal to the number of characters associated with the event.
   * Usually, one requests text starting at a "sourceStart" of 0. If the number
   * of characters actually copied is less than the "length", then there is no
   * more text. Otherwise, subsequent calls need to be made until all text has
   * been retrieved. For example:
   * 
   * <code>int length = 1024; char[] myBuffer = new char[length]; 
   * for ( int sourceStart = 0 ; ; sourceStart += length ) { 
   * int nCopied = stream.getTextCharacters(sourceStart, myBuffer, 0, length); 
   * if (nCopied < length) break; }</code>
   * 
   * XMLStreamException may be thrown if there are any XML errors in the
   * underlying source. The "targetStart" argument must be greater than or equal
   * to 0 and less than the length of "target", Length must be greater than 0
   * and "targetStart + length" must be less than or equal to length of
   * "target".
   * </p>
   * 
   * @param sourceStart
   *          The index of the first character in the source array to copy.
   * @param target
   *          The destination array.
   * @param targetStart
   *          The start offset in the target array.
   * @param length
   *          The number of characters to copy.
   * @return The number of characters actually copied.
   * @throw IllegalStateException If targetStart < 0 or > than the length of
   *        target.
   * @throw IllegalStateException If length < 0 or targetStart + length > length
   *        of target.
   * @throw NullpointerException If target is null.
   */
  // Checked.
  public final int getTextCharacters(
      final int sourceStart,
      final char[] target,
      final int targetStart,
      final int length) {

    // Checked.
    if (target == null) {
      throw new NullPointerException("Target may not be null!");
    }

    if (targetStart < 0
        || targetStart > target.length
        || length < 0
        || targetStart + length > target.length) {
      throw new IllegalStateException();
    }

    int copiedLength = 0;

    // Available text (may be shorter than the length parameter).
    int available = getTextLength() - sourceStart;

    // Checked.
    if (available < 0) {
      throw new IndexOutOfBoundsException("sourceStart is greater than"
          + " number of characters associated with this event");
    }

    // If there are less than length characters.
    if (available < length) {
      copiedLength = available;
    } else {
      copiedLength = length;
    }

    // Checked.
    System.arraycopy(
        getTextCharacters(),
        getTextStart() + sourceStart,
        target,
        targetStart,
        copiedLength);

    return copiedLength;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final int getTextLength() {

    return mRTX.getValue().length();
  }

  /**
   * {@inheritDoc}
   */
  // Checked (not checked if there are whitespaces to skip).
  public final int getTextStart() {
    if (getEventType() != XMLEvent.CHARACTERS) {
      throw new IllegalStateException("getTextStart(): Isn't a text node.");
    }

    char[] ch = mRTX.getValue().toCharArray();

    for (int i = 0; i < ch.length; i++) {
      // There is no special xml definition of whitespaces.
      if (!(ch[i] == '\n' || ch[i] == '\r' || ch[i] == '\t' || ch[i] == ' ')) {
        return i;
      }
    }

    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isWhiteSpace() {

    if (getEventType() == XMLEvent.CHARACTERS
        || getEventType() == XMLEvent.COMMENT) {
      char[] ch = mRTX.getValue().toCharArray();

      int start = getTextStart();
      int end = getTextLength();

      for (int i = start; i < end; i++) {
        // There is no special xml declaration of whitespaces.
        if (!(ch[i] == '\n' || ch[i] == '\r' || ch[i] == '\t' || ch[i] == ' ')) {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final String getVersion() {

    return VERSION;
  }

  /**
   * Returns true if the current event has a name (is an ELEMENT) returns false
   * otherwise.
   * 
   * @return True if it's an ELEMENT, false otherwise.
   */
  // Checked (not checked: XMLEvent.END_ELEMENT).
  public final boolean hasName() {

    if (getEventType() == XMLEvent.START_ELEMENT
        || getEventType() == XMLEvent.END_ELEMENT
        || getEventType() == XMLEvent.ATTRIBUTE) {
      return true;
    }

    return false;
  }

  /**
   * Return true if the current event has text, false otherwise. COMMENT and
   * TEXT events have text.
   * 
   * @return True if it's a COMMENT or TEXT event, false otherwise.
   */
  // Checked (COMMENT should also be checked!).
  public final boolean hasText() {

    if (getEventType() == XMLEvent.CHARACTERS
        || getEventType() == XMLEvent.COMMENT) {

      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final boolean isAttributeSpecified(final int index) {

    if (getEventType() != XMLEvent.START_ELEMENT
        && getEventType() != XMLEvent.ATTRIBUTE) {
      throw new IllegalStateException("isAttributeSpecified(): "
          + getEventType()
          + " is not among the allowed types!");
    }

    if (mRTX.getAttributeCount() > index) {
      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isCharacters() {

    return hasText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isStandalone() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isStartElement() {

    if (getEventType() == XMLEvent.START_ELEMENT) {
      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isEndElement() {

    if (getEventType() == XMLEvent.END_ELEMENT) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if there are more parsing events and false if there are no
   * more events.
   * 
   * @return True if there are more events, false otherwise.
   */
  // Checked.
  public final boolean hasNext() {
    resetToLastKey();

    if (getFirstRun()) {
      // Fail if there is no node anymore.
      if (mNextKey == IReadTransaction.NULL_NODE_KEY) {
        fail();
      }

      mRTX.moveTo(mNextKey);

      // Fail if the subtree is finished.
      if (mRTX.getLeftSiblingKey() == mStartKey) {
        fail();
      }

      // Always follow first child if there is one.
      if (mRTX.hasFirstChild()) {
        mNextKey = mRTX.getFirstChildKey();
        if (mRTX.hasRightSibling()) {
          mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
        }
        return true;
      }

      // Then follow right sibling if there is one.
      if (mRTX.hasRightSibling()) {
        mNextKey = mRTX.getRightSiblingKey();
        return true;
      }

      // Then follow right sibling on stack.
      if (mRightSiblingKeyStack.size() > 0) {
        mNextKey = mRightSiblingKeyStack.pop();
        return true;
      }

      // Then end.
      mNextKey = IReadTransaction.NULL_NODE_KEY;
    }

    if (mStack.empty()) {
      return false;
    }

    return true;
  }

  /**
   * Determines if next() has to return true or false.
   * 
   * @return False if element stack is empty, true otherwise.
   */
  // Checked.
  private final boolean fail() {
    if (mStack.empty()) {
      resetToStartKey();
      return false;
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final int next() {
    // Elements to close.
    if (getCloseElements()) {
      if (getFirstElement()) {
        key = nextKey();
        setFirstRun(false);
      } else {
        // If it's not the first time move to key.
        mRTX.moveTo(key);
      }

      if (!mStack.empty() && mRTX.getLeftSiblingKey() == mStack.peek()) {
        mRTX.moveTo(mStack.pop());
        setCloseElements(false);
      } else if (!mStack.empty()) {
        // Also works at the end of TreeTank.
        mRTX.moveTo(mStack.pop());
      }

      if (getFirstElement()) {
        setFirstElement(false);
      }

      eventType = XMLEvent.END_ELEMENT;

      return eventType;
      // Empty element to close.
    } else if (getIsEmptyElement()) {
      nextKey();

      mRTX.moveTo(mStack.pop());

      if (!mRTX.hasRightSibling()) {
        setValues();
      }

      setIsEmptyElement(false);
      setFirstRun(true);

      eventType = XMLEvent.END_ELEMENT;

      return eventType;
    } else {
      // Process nodes, maybe end of TreeTank reached.
      nextKey();

      // Push to the stack if it's an element with children.
      if (mRTX.isElementKind() && mRTX.hasFirstChild()) {
        mStack.push(mRTX.getNodeKey());

        // If it's an empty element.
      } else if (mRTX.isElementKind()) {
        mStack.push(mRTX.getNodeKey());
        setIsEmptyElement(true);

        // Shouldn't move to mNextKey.
        setFirstRun(false);

        eventType = mapEvents.get(mRTX.getKind());

        return eventType;
      }

      // Remember to emit all pending end elements from stack if required.
      if (!mRTX.hasFirstChild() && !mRTX.hasRightSibling()) {
        setValues();
      }

      setFirstRun(true);
      eventType = mapEvents.get(mRTX.getKind());

      return eventType;
    }
  }

  // Checked.
  private final Long nextKey() {
    if (!mNext) {
      throw new IllegalStateException(
          "nextKey() must be called exactely once after hasNext()"
              + " evaluated to true.");
    }
    mKey = mRTX.getNodeKey();
    mNext = false;
    return mKey;
  }

  /**
   * Make sure the transaction points to the node after the last hasNext(). This
   * must be called first in hasNext().
   * 
   * @return Key of node where transaction was after the last call of hasNext().
   */
  // Checked.
  private final long resetToLastKey() {
    // No check because of IAxis Convention 4.
    mRTX.moveTo(mKey);
    mNext = true;
    return mKey;
  }

  /**
   * Make sure the transaction points to the node it started with. This must
   * be called just before hasNext() == false.
   * 
   * @return Key of node where transaction was before the first call of
   *         hasNext().
   */
  // Checked.
  protected final long resetToStartKey() {
    // No check beacause of IAxis Convention 4.
    mRTX.moveTo(mStartKey);
    mNext = false;
    return mStartKey;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final int nextTag() {
    int event = 0;
    
    for (int i = 0; i != 2; i++) {
      if (hasNext()) {
        event = next();
      }
    }

    while ((event != XMLEvent.START_ELEMENT && event != XMLEvent.END_ELEMENT && hasNext())) {
      event = next();
    }

    if (event != XMLEvent.START_ELEMENT) {
      throw new IllegalStateException("Expected element" + getLocation());
    }

    return event;
  }

  /**
   * {@inheritDoc}
   */
  // Checked.
  public final void require(
      final int type,
      final String namespaceURI,
      final String localName) {

    if (type != getEventType()) {
      throw new IllegalStateException("Event type "
          + type
          + " specified did "
          + "not match with current parser event "
          + getEventType());
    }

    if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
      throw new IllegalStateException("Namespace URI "
          + namespaceURI
          + " specified did not match "
          + "with current namespace URI");
    }

    if (localName != null && !localName.equals(getLocalName())) {
      throw new IllegalStateException("LocalName "
          + localName
          + " specified did not match with "
          + "current local name");
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean standaloneSet() {
    return true;
  }

  // ------------- Private getter and setter methods -------------
  private final void setCloseElements(final boolean state) {
    closeElements = state;
  }

  private final boolean getCloseElements() {
    return closeElements;
  }

  private final void setFirstElement(final boolean state) {
    firstElement = state;
  }

  private final boolean getFirstElement() {
    return firstElement;
  }

  private final void setIsEmptyElement(final boolean state) {
    isEmptyElement = state;
  }

  private final boolean getIsEmptyElement() {
    return isEmptyElement;
  }

  private final void setFirstRun(final boolean state) {
    firstRun = state;
  }

  private final boolean getFirstRun() {
    return firstRun;
  }

  private final void setValues() {
    setCloseElements(true);
    setFirstElement(true);
  }
}
