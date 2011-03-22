/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


import org.treetank.TestHelper;
import org.treetank.api.IDatabase;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

import static org.junit.Assert.assertTrue;

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

    /** String representation of revisioned xml file. */
    public static final String REVXML =
        "<article><title>A Test Document</title><para>This is para 1.</para><para>This is para 2<emphasis>"
            + "with emphasis</emphasis>in it.</para><para>This is para 3.</para><para id=\"p4\">This is "
            + "para 4.</para><para id=\"p5\">This is para 5.</para><para>This is para 6."
            + "</para><para>This is para 7.</para><para>This is para 8.</para><para>This is para 9."
            + "</para></article>";

    /** String representation of ID. */
    public static final String ID =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" "
            + "ttid=\"1\" i=\"j\">oops1<b ttid=\"5\">foo<c ttid=\"7\"/></b>oops2<b ttid=\"9\" p:x=\"y\">"
            + "<c ttid=\"11\"/>bar</b>oops3</p:a>";

    /** String representation of rest. */
    public static final String REST =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<rest:sequence xmlns:rest=\"REST\"><rest:item>"
            + "<p:a xmlns:p=\"ns\" rest:ttid=\"1\" i=\"j\">oops1<b rest:ttid=\"5\">foo<c rest:ttid=\"7\"/></b>oops2<b rest:ttid=\"9\" p:x=\"y\">"
            + "<c rest:ttid=\"11\"/>bar</b>oops3</p:a>" + "</rest:item></rest:sequence>";

    /** String representation of test document. */
    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
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

    /** XML for the index structure. */
    public static final String XML_INDEX = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<t:o><t:oo><t:oop><t:oops><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:TEXT_KIND nodeID=\"4\"/></d:p:a></d:DOCUMENT_ROOT_KIND></t:oops></t:oop></t:oo>"
        + "</t:o><t:f><t:fo><t:foo><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:b nodeID=\"5\"><d:TEXT_KIND nodeID=\"6\"/></d:b></d:p:a></d:DOCUMENT_ROOT_KIND></t:foo>"
        + "</t:fo></t:f><t:b><t:ba><t:bar><d:DOCUMENT_ROOT_KIND nodeID=\"0\"><d:p:a nodeID=\"1\">"
        + "<d:b nodeID=\"9\"><d:TEXT_KIND nodeID=\"12\"/></d:b></d:p:a></d:DOCUMENT_ROOT_KIND></t:bar>"
        + "</t:ba></t:b>";

    /**
     * Private Constructor, not used.
     */
    private DocumentCreater() {
    }

    /**
     * Create simple test document containing all supported node kinds.
     * 
     * @param secondWtx
     *            IWriteTransaction to write to.
     * @throws AbsTTException
     *             if any weird happens
     */
    public static void create(final IWriteTransaction secondWtx) throws AbsTTException {
        assertTrue(secondWtx.moveToDocumentRoot());

        secondWtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
        secondWtx.insertAttribute(new QName("i"), "j");
        assertTrue(secondWtx.moveToParent());
        secondWtx.insertNamespace("ns", "p");
        assertTrue(secondWtx.moveToParent());

        secondWtx.insertTextAsFirstChild("oops1");

        secondWtx.insertElementAsRightSibling(new QName("b"));

        secondWtx.insertTextAsFirstChild("foo");
        secondWtx.insertElementAsRightSibling(new QName("c"));
        assertTrue(secondWtx.moveToParent());

        secondWtx.insertTextAsRightSibling("oops2");

        secondWtx.insertElementAsRightSibling(new QName("b"));
        secondWtx.insertAttribute(new QName("ns", "x", "p"), "y");
        assertTrue(secondWtx.moveToParent());

        secondWtx.insertElementAsFirstChild(new QName("c"));
        secondWtx.insertTextAsRightSibling("bar");
        assertTrue(secondWtx.moveToParent());

        secondWtx.insertTextAsRightSibling("oops3");

        secondWtx.moveToDocumentRoot();
    }

    /**
     * Create simple revision test in current database
     * 
     * @param wtx
     *            IWriteTransaction to write to.
     */
    public static void createVersioned(final IWriteTransaction wtx) throws AbsTTException {
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
    public static void createWithoutNamespace(final IWriteTransaction wtx) throws AbsTTException {
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

    /**
     * Create revisioned document.
     * 
     * @param secondWtx
     *            {@link IWriteTransaction} to create the new revision
     * @throws AbsTTException
     *             if shredding fails
     * @throws XMLStreamException
     *             if StAX reader couldn't be created
     * @throws IOException
     *             if reading XML string fails
     */
    public static void createRevisioned() throws AbsTTException, IOException,
        XMLStreamException {
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction firstWtx = database.getSession().beginWriteTransaction();
        final XMLShredder shredder =
            new XMLShredder(firstWtx, XMLShredder.createStringReader(REVXML), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        firstWtx.close();
        final IWriteTransaction secondWtx = database.getSession().beginWriteTransaction();
        secondWtx.moveToFirstChild();
        secondWtx.moveToFirstChild();
        secondWtx.moveToFirstChild();
        secondWtx.setValue("A Contrived Test Document");
        secondWtx.moveToParent();
        secondWtx.moveToRightSibling();
        secondWtx.moveToRightSibling();
        secondWtx.moveToFirstChild();
        secondWtx.moveToRightSibling();
        final long key = secondWtx.getNode().getNodeKey();
        secondWtx.insertAttribute(new QName("role"), "bold");
        secondWtx.moveTo(key);
        secondWtx.moveToRightSibling();
        secondWtx.setValue("changed in it.");
        secondWtx.moveToParent();
        secondWtx.insertElementAsRightSibling(new QName("para"));
        secondWtx.insertTextAsFirstChild("This is a new para 2b.");
        secondWtx.moveToParent();
        secondWtx.moveToRightSibling();
        secondWtx.moveToRightSibling();
        secondWtx.moveToFirstChild();
        secondWtx.setValue("This is a different para 4.");
        secondWtx.moveToParent();
        secondWtx.insertElementAsRightSibling(new QName("para"));
        secondWtx.insertTextAsFirstChild("This is a new para 4b.");
        secondWtx.moveToParent();
        secondWtx.moveToRightSibling();
        secondWtx.moveToRightSibling();
        secondWtx.remove();
        secondWtx.remove();
        secondWtx.commit(); 
        secondWtx.close();
    }
}
