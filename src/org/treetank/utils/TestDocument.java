/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.utils;

import org.treetank.api.IWriteTransaction;

/**
 * <h1>TestDocument</h1>
 * 
 * <p>
 * This class creates an XML document that contains all features seen in
 * the Extensible Markup Language (XML) 1.1 (Second Edition) as well as the
 * Namespaces in XML 1.1 (Second Edition).
 * </p>
 * 
 * <p>
 * The following figure describes the created test document (see
 * <code>xml/test.xml</code>). The nodes are described as follows:
 * 
 * <ul>
 * <li><code>IConstants.DOCUMENT : doc()</code></li>
 * <li><code>IConstants.FULLTEXT : ft()</code></li>
 * <li><code>IConstants.ELEMENT  : &lt;prefix:localPart&gt;</code></li>
 * <li><code>IConstants.ATTRIBUTE: &#64;prefix:localPart='value'</code></li>
 * <li><code>IConstants.TEXT     : #value</code></li>
 * </ul>
 *
 * <pre>
 * 0 doc()
 * 1 ft()
 * |-  2 &lt;p:a §p:ns &#64;i='j'&gt;
 *     |-  3 #oops1
 *     |-  4 &lt;b&gt;
 *     |   |-  5 #foo
 *     |   |-  6 &lt;c&gt;
 *     |-  7 #oops2
 *     |-  8 &lt;b &#64;p:x='y'&gt;
 *     |   |-  9 &lt;c&gt;
 *     |   |- 10 #bar
 *     |- 11 #oops3
 * </pre>
 * 
 * </p>
 */
public final class TestDocument {

  /** String representation of test document. */
  public static final String XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
          + "<p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\">"
          + "<c/>bar</b>oops3</p:a>";

  public static final String XMLWITHOUTATTRIBUTES =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
          + "<p:a>oops1<b>foo<c></c></b>oops2<b>"
          + "<c></c>bar</b>oops3</p:a>";

  /**
   * Hidden constructor.
   *
   */
  private TestDocument() {
    // Hidden.
  }

  /**
   * Create simple test document containing all supported node kinds.
   * 
   * @param wtx IWriteTransaction to write to.
   */
  public static void create(final IWriteTransaction wtx) {

    wtx.insertElementAsFirstChild("a", "ns", "p");
    wtx.insertAttribute("i", "", "", UTF.convert("j"));

    wtx.insertTextAsFirstChild(UTF.convert("oops1"));

    wtx.insertElementAsRightSibling("b", "", "");

    wtx.insertTextAsFirstChild(UTF.convert("foo"));
    wtx.insertElementAsRightSibling("c", "", "");
    wtx.moveToParent();

    wtx.insertTextAsRightSibling(UTF.convert("oops2"));

    wtx.insertElementAsRightSibling("b", "", "");
    wtx.insertAttribute("x", "ns", "p", UTF.convert("y"));

    wtx.insertElementAsFirstChild("c", "", "");
    wtx.insertTextAsRightSibling(UTF.convert("bar"));
    wtx.moveToParent();

    wtx.insertTextAsRightSibling(UTF.convert("oops3"));

  }

  /**
   * Create simple test document containing all supported node kinds except
   * the attributes.
   * 
   * @param wtx IWriteTransaction to write to.
   */
  public static void createWithoutAttributes(final IWriteTransaction wtx) {
    wtx.insertElementAsFirstChild("a", "ns", "p");

    wtx.insertTextAsFirstChild(UTF.convert("oops1"));

    wtx.insertElementAsRightSibling("b", "", "");

    wtx.insertTextAsFirstChild(UTF.convert("foo"));
    wtx.insertElementAsRightSibling("c", "", "");
    wtx.moveToParent();

    wtx.insertTextAsRightSibling(UTF.convert("oops2"));

    wtx.insertElementAsRightSibling("b", "", "");

    wtx.insertElementAsFirstChild("c", "", "");
    wtx.insertTextAsRightSibling(UTF.convert("bar"));
    wtx.moveToParent();

    wtx.insertTextAsRightSibling(UTF.convert("oops3"));

  }

}
