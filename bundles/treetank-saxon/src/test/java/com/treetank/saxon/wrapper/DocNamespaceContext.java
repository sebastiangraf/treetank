package com.treetank.saxon.wrapper;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * <h1>Namespace Context</h1>
 * 
 * <p>Namespace Context to test the NodeInfo implementation (<code>NodeWrapper</code>). 
 * It's written for the test document which is written via <code></code>.</p>
 * 
 * @author johannes
 */
public final class DocNamespaceContext implements NamespaceContext {

  /**
   * Get the Namespace URI.
   * 
   * @param prefix
   *          Prefix of the current node.
   * @return Return the Namespace URI.
   * @throws IllegalArgumentException if prefix is null.
   */
  public String getNamespaceURI(final String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException("Prefix may not be null!");
    } else if (prefix == XMLConstants.DEFAULT_NS_PREFIX) {
      return XMLConstants.NULL_NS_URI;
    } else if (prefix == XMLConstants.XML_NS_PREFIX) {
      return XMLConstants.XML_NS_URI;
    } else if (prefix == XMLConstants.XMLNS_ATTRIBUTE) {
      return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    } else if ("p".equals(prefix)) {
      return "ns";
    } else {
      return XMLConstants.NULL_NS_URI;
    }
  }

  /**
   * Get the prefix of the namespace.
   * 
   * @param namespace
   *          Namespace of the current node.
   * @return "p" if the Namespace equals "ns", otherwise returns null.
   */
  public String getPrefix(final String namespace) {
    if (namespace == null) {
      throw new IllegalArgumentException("Namespace may not be null!");
    } else if (XMLConstants.XML_NS_URI.equals(namespace)) {
      return XMLConstants.XML_NS_PREFIX;
    } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
      return XMLConstants.XMLNS_ATTRIBUTE;
    } else if (namespace.equals("ns")) {
      return "p";
    } else {
      return null;
    }
  }

  /**
   * Not needed/supported (only one prefix exists for a NS_URI).
   */
  public Iterator<String> getPrefixes(final String namespace) {
    throw new UnsupportedOperationException(
        "Currently not needed by the test document!");
  }
}