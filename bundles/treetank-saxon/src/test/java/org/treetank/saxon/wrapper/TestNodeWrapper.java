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

package org.treetank.saxon.wrapper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.NamespaceIterator.NamespaceNodeImpl;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import org.treetank.TestHelper;
import org.treetank.access.Database;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxisTest;
import org.treetank.axis.AbsAxisTest.Holder;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test implemented methods in NodeWrapper.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class TestNodeWrapper {

    /** Treetank session on Treetank test document. */
    private transient Holder mHolder;

    /** Document node. */
    private transient NodeWrapper node;

    @Before
    public void beforeMethod() throws AbsTTException {
        Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
        Database.closeDatabase(TestHelper.PATHS.PATH1.getFile());
        TestHelper.createTestDocument();
        mHolder = AbsAxisTest.generateHolder();

        final Processor proc = new Processor(false);
        final Configuration config = proc.getUnderlyingConfiguration();

        node = new DocumentWrapper(mHolder.session, config).getNodeWrapper();
    }

    @After
    public void afterMethod() throws AbsTTException {
        mHolder.session.close();
        mHolder.rtx.close();
        Database.closeDatabase(TestHelper.PATHS.PATH1.getFile());
        Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
    }

    @Test
    public void testAtomize() throws Exception {
        final Value value = node.atomize();
        assertEquals(true, value instanceof UntypedAtomicValue);
        assertEquals("oops1foooops2baroops3", value.getStringValue());
    }

    @Test
    public void testCompareOrder() throws XPathException, AbsTTException {
        final Processor proc = new Processor(false);
        final Configuration config = proc.getUnderlyingConfiguration();

        final IDatabase database = Database.openDatabase(TestHelper.PATHS.PATH2.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());

        // Not the same document.
        NodeInfo node = new DocumentWrapper(session, config);
        NodeInfo other = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 3);
        assertEquals(-2, node.compareOrder(other));

        // Before.
        node = new DocumentWrapper(mHolder.session, config);
        other = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 3);
        assertEquals(-1, node.compareOrder(other));

        // After.
        node = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 3);
        other = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 0);
        assertEquals(1, node.compareOrder(other));

        // Same.
        node = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 3);
        other = new NodeWrapper(new DocumentWrapper(mHolder.session, config), 3);
        assertEquals(0, node.compareOrder(other));

        session.close();
        Database.closeDatabase(TestHelper.PATHS.PATH2.getFile());
    }

    @Test
    public void testGetAttributeValue() throws AbsTTException {
        final Processor proc = new Processor(false);
        node = new NodeWrapper(new DocumentWrapper(mHolder.session, proc.getUnderlyingConfiguration()), 1);

        final AxisIterator iterator = node.iterateAxis(Axis.ATTRIBUTE);
        final NodeInfo attribute = (NodeInfo)iterator.next();

        node.getNamePool().allocate(attribute.getPrefix(), attribute.getURI(), attribute.getLocalPart());

        // Only supported on element nodes.
        // node = (NodeWrapper) node.getParent();

        assertEquals("j", node.getAttributeValue(attribute.getFingerprint()));
    }

    @Test
    // @Ignore
        public
        void testGetBaseURI() throws Exception {
        // Test with xml:base specified.
        final File source =
            new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "data"
                + File.separator + "testBaseURI.xml");

        Database.truncateDatabase(new File(TestHelper.PATHS.PATH2.getFile(), "baseURI"));
        final IDatabase database =
            Database.openDatabase(new File(TestHelper.PATHS.PATH2.getFile(), "baseURI"));
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLEventReader reader = XMLShredder.createReader(source);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();

        final Processor proc = new Processor(false);
        final NodeInfo doc = new DocumentWrapper(session, proc.getUnderlyingConfiguration());

        doc.getNamePool().allocate("xml", "http://www.w3.org/XML/1998/namespace", "base");
        doc.getNamePool().allocate("", "", "baz");

        final NameTest test = new NameTest(Type.ELEMENT, "", "baz", doc.getNamePool());
        final AxisIterator iterator = doc.iterateAxis(Axis.DESCENDANT, test);
        final NodeInfo baz = (NodeInfo)iterator.next();

        assertEquals("http://example.org", baz.getBaseURI());
        session.close();
        Database.closeDatabase(TestHelper.PATHS.PATH2.getFile());

    }

    @Test
    public void testGetDeclaredNamespaces() {
        // Namespace declared.
        final AxisIterator iterator = node.iterateAxis(Axis.CHILD);
        node = (NodeWrapper)iterator.next();
        final int[] namespaces = node.getDeclaredNamespaces(new int[1]);

        node.getNamePool().allocateNamespaceCode("p", "ns");
        final int expected = node.getNamePool().getNamespaceCode("p", "ns");

        assertEquals(expected, namespaces[0]);

        // Namespace not declared (on element node) -- returns zero length
        // array.
        final AxisIterator iter = node.iterateAxis(Axis.DESCENDANT);
        node = (NodeWrapper)iter.next();
        node = (NodeWrapper)iter.next();

        final int[] namesp = node.getDeclaredNamespaces(new int[1]);

        assertTrue(namesp.length == 0);

        // Namespace nod declared on other nodes -- return null.
        final AxisIterator it = node.iterateAxis(Axis.DESCENDANT);
        node = (NodeWrapper)it.next();

        assertNull(node.getDeclaredNamespaces(new int[1]));
    }

    @Test
    public void testGetStringValueCS() {
        // Test on document node.
        assertEquals("oops1foooops2baroops3", node.getStringValueCS());

        // Test on element node.
        AxisIterator iterator = node.iterateAxis(Axis.DESCENDANT);
        node = (NodeWrapper)iterator.next();
        assertEquals("oops1foooops2baroops3", node.getStringValueCS());

        // Test on namespace node.
        iterator = node.iterateAxis(Axis.NAMESPACE);
        NamespaceNodeImpl namespace = (NamespaceNodeImpl)iterator.next();

        /*
         * Elements have always the default xml:NamespaceConstant.XML namespace,
         * so we have to search if "ns" is found somewhere in the iterator
         * (order unpredictable because it's implemented with a HashMap
         * internally).
         */
        while (!"ns".equals(namespace.getStringValueCS()) && namespace != null) {
            namespace = (NamespaceNodeImpl)iterator.next();
        }

        if (namespace == null) {
            fail("namespace is null!");
        } else {
            assertEquals("ns", namespace.getStringValueCS());
        }

        // Test on attribute node.
        final NodeWrapper attrib = (NodeWrapper)node.iterateAxis(Axis.ATTRIBUTE).next();
        assertEquals("j", attrib.getStringValueCS());

        // Test on text node.
        final NodeWrapper text = (NodeWrapper)node.iterateAxis(Axis.CHILD).next();
        assertEquals("oops1", text.getStringValueCS());
    }

    @Test
    public void testGetSiblingPosition() {
        // Test every node in test document.
        final AxisIterator iterator = node.iterateAxis(Axis.DESCENDANT);
        node = (NodeWrapper)iterator.next();
        node = (NodeWrapper)iterator.next();
        assertEquals(0, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(1, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(0, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(1, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(2, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(3, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(0, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(1, node.getSiblingPosition());
        node = (NodeWrapper)iterator.next();
        assertEquals(4, node.getSiblingPosition());
    }
}
