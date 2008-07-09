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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * NamespaceContext Implementation.
 * 
 * @author Johannes Lichtenberger &lt;johannes.lichtenberger@uni-konstanz.de&gt;
 */
public class NamespaceContextImpl implements NamespaceContext {

  // HashMap with a prefix to namespace mapping.
  private final HashMap<String, String> mMap;

  /**
   * Constructor which initializes the HashMap.
   */
  public NamespaceContextImpl() {
    mMap = new HashMap<String, String>();
    mMap.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
    mMap.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    mMap.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
  }

  /**
   * Sets a namespace.
   * 
   * @param prefix
   *               Prefix bound to Namespace URI in current context.
   * @param namespaceURI
   *               URI of the Namespace.
   * @throw IllegalArgumentException
   *               If prefix or namespaceURI is null.
   */
  public void setNamespace(final String prefix, final String namespaceURI) {

    if (prefix == null || namespaceURI == null) {
      throw new IllegalArgumentException(
          "Arguments prefix and namespaceURI must not be null.");
    }

    mMap.put(prefix, namespaceURI);
  }

  /**
   * <p>Get Namespace URI bound to a prefix in the current scope.</p>
   * 
   * <p>When requesting a Namespace URI by prefix, the following
   * table describes the returned Namespace URI value for all
   * possible prefix values:</p>
   * 
   * <table border="2" rules="all" cellpadding="4"> 
   *   <thead>
   *     <tr>
   *       <td align="center" colspan="2">
   *         <code>getNamespaceURI(prefix)</code>
   *         return value for specified prefixes
   *       </td>
   *     </tr>
   *     <tr>
   *       <td>prefix parameter</td>
   *       <td>Namespace URI return value</td>
   *     </tr>   
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td><code>DEFAULT_NS_PREFIX</code> ("")</td>
   *       <td>default Namespace URI in the current scope or
   *       <code><a href="http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/XMLConstants.html#NULL_NS_URI">
   *       <code>XMLConstants.NULL_NS_URI("")</code></a></code>
   *       when there is no default Namespace URI in the current scope</td>
   *     </tr>
   *     <tr>
   *       <td>bound prefix</td>
   *       <td>Namespace URI bound to prefix in current scope</td>
   *     </tr>
   *     <tr>
   *       <td>unbound prefix</td>
   *       <td><code><a href="http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/XMLConstants.html#NULL_NS_URI">
   *       <code>XMLConstants.NULL_NS_URI("")</code></a></code></td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
   *       <td><code>XMLConstants.XML_NS_URI</code>
   *       ("http://www.w3.org/XML/1998/namespace")</td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
   *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
   *       ("http://www.w3.org/2000/xmlns/")</td>
   *     </tr>
   *     <tr>
   *       <td><code>null</code></td>
   *       <td><code>IllegalArgumentException</code> is thrown</td>
   *     </tr>
   *   </tbody>
   * </table>
   * 
   * @param prefix
   *               Prefix to look up.
   * @return Namespace 
   *               URI bound to prefix in the current scope.
   * @throw IllegalArgumentException
   *               If prefix is null.
   */
  public final String getNamespaceURI(final String prefix) {

    // Case 7.
    if (prefix == null) {
      throw new IllegalArgumentException("Argument prefix must not be null.");
    }

    // Case 4.
    if (!mMap.containsKey(prefix)) {
      return XMLConstants.NULL_NS_URI;
    }

    // Case 1, 2 (prefix = ""), 3, 5, 6
    return mMap.get(prefix);
  }

  /**
   * <p>Get prefix bound to Namespace URI in the current scope.</p>
   * 
   * <p>To get all prefixes bound to a Namespace URI in the current 
   * scope, use <a href="#getPrefixes(java.lang.String)">
   * <code>getPrefixes(final String namespaceURI)</code></a>.</p>
   * 
   * <p>When requesting a prefix by Namespace URI, the following
   * table describes the returned prefix value for all Namespace URI
   * values:</p>
   * 
   * <table border="2" rules="all" cellpadding="4">
   *   <thead>
   *     <tr>
   *       <td align="center" colspan="2">
   *       <code>getPrefix(namespaceURI)</code> return value for
   *       specified Namespace URIs
   *       </td>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>Namespace URI parameter</td>
   *       <td>prefix value returned</td>
   *     </tr>
   *     <tr>
   *       <td>&lt;default Namespace URI&gt;</td>
   *       <td><code>XMLConstants.DEFAULT_NS_PREFIX</code> ("")</td>
   *     </tr>
   *     <tr>
   *       <td>bound Namespace URI</td>
   *       <td>prefix bound to Namespace URI in the current scope,
   *       if multiple prefixes are bound to the Namespace URI in
   *       the current scope, a single arbitrary prefix, whose
   *       choice is implementation dependent, is returned</td>
   *     </tr>
   *     <tr>
   *       <td>unbound Namespace URI</td>
   *       <td><code>null</code></td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XML_NS_URI</code>
   *       ("http://www.w3.org/XML/1998/namespace")</td>
   *       <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
   *       ("http://www.w3.org/2000/xmlns/")</td>
   *       <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
   *     </tr>
   *     <tr>
   *       <td><code>null</code></td>
   *       <td><code>IllegalArgumentException</code> is thrown</td>
   *     </tr>
   *   </tbody>
   * </table>
   * 
   * @param namespaceURI
   *                     URI of Namespace to lookup.
   * @return Prefix 
   *                     Bound to Namespace URI in current context.
   * @throw IllegalArgumentException
   *                     If namespaceURI is null.   
   */
  public final String getPrefix(final String namespaceURI) {

    // Check case 7.
    if (namespaceURI == null) {
      throw new IllegalArgumentException(
          "Argument namespaceURI must not be null.");
    }

    // Check case 1, 3, 5 and 6.
    for (final String prefix : mMap.keySet()) {
      if (mMap.get(prefix).equals(namespaceURI)) {
        return prefix;
      }
    }

    // Check case 4.
    return null;
  }

  /**
   * <p>Get all prefixes bound to a Namespace URI in the current scope.</p>
   * 
   * <p>An Iterator over String elements is returned</p>
   * 
   * <p>When requesting prefixes by Namespace URI, the following table 
   * describes the returned prefixes value for all Namespace URI values:</p>
   * 
   * <table border="2" rules="all" cellpadding="4">
   *   <thead>
   *     <tr>
   *       <td align="center" colspan="2"><code>
   *       getPrefixes(namespaceURI)</code> return value for specified 
   *       Namespace URIs</td>
   *     </tr>
   *     <tr>
   *       <td>Namespace URI parameter</td>
   *       <td>prefixes value returned</td>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>bound Namespace URI, including the &lt;default Namespace 
   *       URI&gt;</td>
   *       <td><code>Iterator</code> over prefixes bound to Namespace URI in
   *       the current scope in an arbitrary, <strong>implementation 
   *       dependent</strong>, order</td>
   *     </tr>
   *     <tr>
   *       <td>unbound Namespace URI</td>
   *       <td>empty <code>Iterator</code></td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XML_NS_URI</code>
   *       ("http://www.w3.org/XML/1998/namespace")</td>
   *       <td><code>Iterator</code> with one element set to
   *       <code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
   *     </tr>
   *     <tr>
   *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
   *       ("http://www.w3.org/2000/xmlns/")</td>
   *       <td><code>Iterator</code> with one element set to
   *       <code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
   *     </tr>
   *     <tr>
   *       <td><code>null</code></td>
   *       <td><code>IllegalArgumentException</code> is thrown</td>
   *     </tr>
   *   </tbody>
   * </table>
   * 
   * @param namespaceURI
   *                     URI of Namespace to lookup.
   * @return Iterator for all prefixes bound to the Namespace URI 
   *         in the current scope.
   */
  public Iterator<String> getPrefixes(final String namespaceURI) {

    // Case 6.
    if (namespaceURI == null) {
      throw new IllegalArgumentException(
          "Argument namespaceURI must not be null.");
    }

    final List<String> prefixes = new ArrayList<String>();

    // Case 1, 2, 3, 4, 5.
    for (final String prefix : mMap.keySet()) {
      if (mMap.get(prefix).equals(namespaceURI)) {
        prefixes.add(prefix);
      }
    }

    return prefixes.iterator();
  }

}
