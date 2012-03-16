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

package org.treetank.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.treetank.api.INodeWriteTransaction;
import org.treetank.exception.AbsTTException;

/**
 * <h1>TestDocument</h1>
 * 
 * <p>
 * This class creates an XML document that contains all features seen in the Extensible Markup Language (XML)
 * 1.1 (Second Edition) as well as the Namespaces in XML 1.1 (Second Edition).
 * </p>
 * 
 * <p>
 * The following figure describes the created test document (see <code>xml/test.xml</code>). The nodes are
 * described as follows:
 * 
 * <ul>
 * <li><code>ENode.ROOT_KIND     : doc()</code></li>
 * <li><code>ENode.ELEMENT_KIND  : &lt;prefix:localPart&gt;</code></li>
 * <li><code>ENode.NAMESPACE_KIND: §prefix:namespaceURI</code></li>
 * <li><code>ENode.ATTRIBUTE_KIND: &#64;prefix:localPart='value'</code></li>
 * <li><code>ENode.TEXT_KIND     : #value</code></li>
 * </ul>
 * 
 * <pre>
 * 0 doc()
 * |-  1 &lt;p:a §p:ns @i='j'&gt;
 *     |-  4 #oops1
 *     |-  5 &lt;b&gt;
 *     |   |-  6 #foo
 *     |   |-  7 &lt;c&gt;
 *     |-  8 #oops2
 *     |-  9 &lt;b @p:x='y'&gt;
 *     |   |- 11 &lt;c&gt;
 *     |   |- 12 #bar
 *     |- 13 #oops3
 * </pre>
 * 
 * </p>
 */
public final class DocumentCreater {

    /** String representation of test document. */
    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\">" + "<c/>bar</b>oops3</p:a>";

    /**
     * Private Constructor, not used.
     */
    private DocumentCreater() {
        throw new AssertionError("Not permitted to call constructor!");
    }

    /**
     * Create simple test document containing all supported node kinds.
     * 
     * @param paramWtx
     *            {@link INodeWriteTransaction} to write to
     * @throws AbsTTException
     *             if anything weird happens
     */
    public static void create(final INodeWriteTransaction paramWtx) throws AbsTTException {
        assertNotNull(paramWtx);
        assertTrue(paramWtx.moveToDocumentRoot());

        paramWtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
        paramWtx.insertAttribute(new QName("i"), "j");
        assertTrue(paramWtx.moveToParent());
        paramWtx.insertNamespace(new QName("ns", "xmlns", "p"));
        assertTrue(paramWtx.moveToParent());

        paramWtx.insertTextAsFirstChild("oops1");

        paramWtx.insertElementAsRightSibling(new QName("b"));

        paramWtx.insertTextAsFirstChild("foo");
        paramWtx.insertElementAsRightSibling(new QName("c"));
        assertTrue(paramWtx.moveToParent());

        paramWtx.insertTextAsRightSibling("oops2");

        paramWtx.insertElementAsRightSibling(new QName("b"));
        paramWtx.insertAttribute(new QName("ns", "x", "p"), "y");
        assertTrue(paramWtx.moveToParent());

        paramWtx.insertElementAsFirstChild(new QName("c"));
        paramWtx.insertTextAsRightSibling("bar");
        assertTrue(paramWtx.moveToParent());

        paramWtx.insertTextAsRightSibling("oops3");

        paramWtx.moveToDocumentRoot();
    }

}
