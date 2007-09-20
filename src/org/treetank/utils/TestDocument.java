/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
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
 * <li><code>IConstants.DOCUMENT : doc('value')</code></li>
 * <li><code>IConstants.ELEMENT  : &lt;prefix:localPart&gt;</code></li>
 * <li><code>IConstants.ATTRIBUTE: &#64;prefix:localPart='value'</code></li>
 * <li><code>IConstants.TEXT     : #value</code></li>
 * </ul>
 *
 * <pre>
 * 0 doc('doc')
 * |-  1 &lt;p:a §p:ns &#64;i='j'&gt;
 *     |-  2 #oops1
 *     |-  3 &lt;b&gt;
 *     |   |-  4 #foo
 *     |   |-  5 &lt;c&gt;
 *     |-  6 #oops2
 *     |-  7 &lt;b &#64;p:x='y'&gt;
 *     |   |-  8 &lt;c&gt;
 *     |   |-  9 #bar
 *     |- 10 #oops3
 * </pre>
 * 
 * </p>
 */
public final class TestDocument {

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
   * @param trx IWriteTransaction to write to.
   * @throws Exception of any kind.
   */
  public static void create(final IWriteTransaction trx) throws Exception {

    trx.insertRoot("doc");

    trx.insertFirstChild(IConstants.ELEMENT, "a", "ns", "p", UTF.EMPTY);
    trx.insertAttribute("i", "", "", UTF.convert("j"));

    trx.insertFirstChild(IConstants.TEXT, "", "", "", UTF.convert("oops1"));

    trx.insertRightSibling(IConstants.ELEMENT, "b", "", "", UTF.EMPTY);

    trx.insertFirstChild(IConstants.TEXT, "", "", "", UTF.convert("foo"));
    trx.insertRightSibling(IConstants.ELEMENT, "c", "", "", UTF.EMPTY);
    trx.moveToParent();

    trx.insertRightSibling(IConstants.TEXT, "", "", "", UTF.convert("oops2"));

    trx.insertRightSibling(IConstants.ELEMENT, "b", "", "", UTF.EMPTY);
    trx.insertAttribute("x", "ns", "p", UTF.convert("y"));

    trx.insertFirstChild(IConstants.ELEMENT, "c", "", "", UTF.EMPTY);
    trx.insertRightSibling(IConstants.TEXT, "", "", "", UTF.convert("bar"));
    trx.moveToParent();

    trx.insertRightSibling(IConstants.TEXT, "", "", "", UTF.convert("oops3"));

  }

}
