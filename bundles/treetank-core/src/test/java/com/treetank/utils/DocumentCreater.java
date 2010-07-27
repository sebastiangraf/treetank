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
 * $Id:TestDocument.java 4373 2008-08-25 07:24:30Z kramis $
 */

package com.treetank.utils;

import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

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
 * <li><code>IConstants.DOCUMENT : doc()</code></li>
 * <li><code>IConstants.FULLTEXT : ft()</code></li>
 * <li><code>IConstants.ELEMENT  : &lt;prefix:localPart&gt;</code></li>
 * <li><code>IConstants.ATTRIBUTE: &#64;prefix:localPart='value'</code></li>
 * <li><code>IConstants.TEXT     : #value</code></li>
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

    /** String representation of ID */
    public static final String ID =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" "
        + "ttid=\"1\" i=\"j\">oops1<b ttid=\"5\">foo<c ttid=\"7\"/></b>oops2<b ttid=\"9\" p:x=\"y\">"
        + "<c ttid=\"11\"/>bar</b>oops3</p:a>";

    /** String representation of rest */
    public static final String REST =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<rest:sequence xmlns:rest=\"REST\"><rest:item>"
        + "<p:a xmlns:p=\"ns\" rest:ttid=\"1\" i=\"j\">oops1<b rest:ttid=\"5\">foo<c rest:ttid=\"7\"/></b>oops2<b rest:ttid=\"9\" p:x=\"y\">"
        + "<c rest:ttid=\"11\"/>bar</b>oops3</p:a>" + "</rest:item></rest:sequence>";

    /** String representation of test document. */
    public static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\">" + "<c/>bar</b>oops3</p:a>";

    /** String representation of test document. */
    public static final String VERSIONEDXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<tt revision=\"0\"><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>"
        + "<tt revision=\"1\"><p:a>OOPS4!</p:a><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>"
        + "<tt revision=\"2\"><p:a>OOPS4!</p:a><p:a>OOPS4!</p:a><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>";

    /** String representation of test document without attributes. */
    public static final String XMLWITHOUTATTRIBUTES =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<p:a>oops1<b>foo<c></c></b>oops2<b>" + "<c></c>bar</b>oops3</p:a>";

    public static final String XML_INDEX =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<t:o><t:oo><t:oop><t:oops><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:TEXT_KIND nodeID=\"4\"/></d:p:a></d:DOCUMENT_ROOT_KIND></t:oops></t:oop></t:oo>"
        + "</t:o><t:f><t:fo><t:foo><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:b nodeID=\"5\"><d:TEXT_KIND nodeID=\"6\"/></d:b></d:p:a></d:DOCUMENT_ROOT_KIND></t:foo>"
        + "</t:fo></t:f><t:b><t:ba><t:bar><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:b nodeID=\"9\"><d:TEXT_KIND nodeID=\"12\"/></d:b></d:p:a></d:DOCUMENT_ROOT_KIND></t:bar>"
        + "</t:ba></t:b>";

    /**
     * Create simple test document containing all supported node kinds.
     * 
     * @param wtx
     *            IWriteTransaction to write to.
     */
    public static void create(final IWriteTransaction wtx) throws TreetankException {
        assertTrue(wtx.moveToDocumentRoot());

        wtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
        wtx.insertAttribute(new QName("i"), "j");
        assertTrue(wtx.moveToParent());
        wtx.insertNamespace("ns", "p");
        assertTrue(wtx.moveToParent());

        wtx.insertTextAsFirstChild("oops1");

        wtx.insertElementAsRightSibling(new QName("b"));

        wtx.insertTextAsFirstChild("foo");
        wtx.insertElementAsRightSibling(new QName("c"));
        assertTrue(wtx.moveToParent());

        wtx.insertTextAsRightSibling("oops2");

        wtx.insertElementAsRightSibling(new QName("b"));
        wtx.insertAttribute(new QName("ns", "x", "p"), "y");
        assertTrue(wtx.moveToParent());

        wtx.insertElementAsFirstChild(new QName("c"));
        wtx.insertTextAsRightSibling("bar");
        assertTrue(wtx.moveToParent());

        wtx.insertTextAsRightSibling("oops3");

        wtx.moveToDocumentRoot();
    }

    /**
     * Create simple revision test in current database
     * 
     * @param wtx
     *            IWriteTransaction to write to.
     */
    public static void createVersioned(final IWriteTransaction wtx) throws TreetankException {
        create(wtx);
        wtx.commit();
        for (int i = 0; i <= 1; i++) {
            wtx.moveToDocumentRoot();
            wtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
            wtx.insertTextAsFirstChild("OOPS4!");
            wtx.commit();
        }

    }

    // /**
    // * Create simple test document containing all supported node kinds except
    // * the attributes.
    // *
    // * @param wtx
    // * IWriteTransaction to write to.
    // */
    // public static void createWithoutAttributes(final IWriteTransaction wtx)
    // throws TreetankException {
    // wtx.moveToDocumentRoot();
    //
    // wtx.insertElementAsFirstChild("p:a", "ns");
    //
    // wtx.insertTextAsFirstChild("oops1");
    //
    // wtx.insertElementAsRightSibling("b", "");
    //
    // wtx.insertTextAsFirstChild("foo");
    // wtx.insertElementAsRightSibling("c", "");
    // wtx.moveToParent();
    //
    // wtx.insertTextAsRightSibling("oops2");
    //
    // wtx.insertElementAsRightSibling("b", "");
    //
    // wtx.insertElementAsFirstChild("c", "");
    // wtx.insertTextAsRightSibling("bar");
    // wtx.moveToParent();
    //
    // wtx.insertTextAsRightSibling("oops3");
    //
    // wtx.moveToDocumentRoot();
    // }

    /**
     * Create simple test document containing all supported node kinds, but
     * ignoring their namespace prefixes.
     * 
     * @param wtx
     *            IWriteTransaction to write to.
     */
    public static void createWithoutNamespace(final IWriteTransaction wtx) throws TreetankException {
        wtx.moveToDocumentRoot();

        wtx.insertElementAsFirstChild(new QName("a"));
        wtx.insertAttribute(new QName("i"), "j");
        wtx.moveToParent();

        wtx.insertTextAsFirstChild("oops1");

        wtx.insertElementAsRightSibling(new QName("b"));

        wtx.insertTextAsFirstChild("foo");
        wtx.insertElementAsRightSibling(new QName("c"));
        wtx.moveToParent();

        wtx.insertTextAsRightSibling("oops2");

        wtx.insertElementAsRightSibling(new QName("b"));
        wtx.insertAttribute(new QName("x"), "y");
        wtx.moveToParent();

        wtx.insertElementAsFirstChild(new QName("c"));
        wtx.insertTextAsRightSibling("bar");
        wtx.moveToParent();

        wtx.insertTextAsRightSibling("oops3");

        wtx.moveToDocumentRoot();
    }
}
