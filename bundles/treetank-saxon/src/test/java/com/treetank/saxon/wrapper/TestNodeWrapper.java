package com.treetank.saxon.wrapper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NamespaceIterator.NamespaceNodeImpl;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.utils.DocumentCreater;

/**
 * Test implemented methods in NodeWrapper.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class TestNodeWrapper {

	/** Treetank session on Treetank test document. */
	private transient static IDatabase databaseTest;

	/** Document node. */
	private transient NodeWrapper node;

	@Before
	public void beforeMethod() throws TreetankException {
		Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
		Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
		databaseTest = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
		final IWriteTransaction wtx = databaseTest.getSession()
				.beginWriteTransaction();
		DocumentCreater.create(wtx);
		wtx.commit();
		wtx.close();

		final Processor proc = new Processor(false);
		final Configuration config = proc.getUnderlyingConfiguration();

		node = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap();
	}

	@After
	public void afterMethod() throws TreetankException {
		Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
		Database.truncateDatabase(TestHelper.PATHS.PATH2.getFile());
		Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
		Database.forceCloseDatabase(TestHelper.PATHS.PATH2.getFile());
	}

	@Test
	public void testAtomize() throws XPathException {
		final NodeWrapper wrapper = new NodeWrapper(databaseTest, 0);
		final Value value = wrapper.atomize();
		TestCase.assertEquals(true, value instanceof UntypedAtomicValue);
		TestCase.assertEquals("oops1foooops2baroops3", value.getStringValue());
	}

	@Test
	public void testCompareOrder() throws XPathException, TreetankException {
		final Processor proc = new Processor(false);
		final Configuration config = proc.getUnderlyingConfiguration();

		final IDatabase database = Database.openDatabase(TestHelper.PATHS.PATH2
				.getFile());

		// Not the same document.
		NodeWrapper node = (NodeWrapper) new DocumentWrapper(database, config).wrap();
		NodeWrapper other = (NodeWrapper) new DocumentWrapper(databaseTest,
				config)
				.wrap(3);
		TestCase.assertEquals(-2, node.compareOrder(other));

		// Before.
		node = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap();
		other = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap(3);
		TestCase.assertEquals(-1, node.compareOrder(other));

		// After.
		node = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap(3);
		other = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap(0);
		TestCase.assertEquals(1, node.compareOrder(other));

		// Same.
		node = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap(3);
		other = (NodeWrapper) new DocumentWrapper(databaseTest, config).wrap(3);
		TestCase.assertEquals(0, node.compareOrder(other));
	}

	@Test
	public void testGetAttributeValue() {
		final Processor proc = new Processor(false);
		node = (NodeWrapper) new DocumentWrapper(databaseTest, proc
				.getUnderlyingConfiguration()).wrap(1);

		final AxisIterator iterator = node.iterateAxis(Axis.ATTRIBUTE);
		final NodeInfo attribute = (NodeInfo) iterator.next();

		node.getNamePool().allocate(attribute.getPrefix(), attribute.getURI(),
				attribute.getLocalPart());

		// Only supported on element nodes.
//		node = (NodeWrapper) node.getParent();

		TestCase.assertEquals("j", node.getAttributeValue(attribute
				.getFingerprint()));
	}

	@Test
//	@Ignore
	public void testGetBaseURI() throws Exception {
		// Test without xml:base specified.
		TestCase.assertEquals(TestHelper.PATHS.PATH1.getFile()
				.getAbsolutePath(), node.getBaseURI());

		// Test with xml:base specified.
		final File source = new File("src" + File.separator + "test"
				+ File.separator + "resources" + File.separator + "data"
				+ File.separator + "testBaseURI.xml");

		Database.truncateDatabase(new File(TestHelper.PATHS.PATH2.getFile(),
				"baseURI"));
		final IDatabase database = Database.openDatabase(new File(
				TestHelper.PATHS.PATH2.getFile(), "baseURI"));
		final ISession mSession = database.getSession();
		final IWriteTransaction mWTX = mSession.beginWriteTransaction();
		final XMLEventReader reader = XMLShredder.createReader(source);
		final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
		shredder.call();
		mWTX.close();

		final Processor proc = new Processor(false);
		final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(database,
				proc.getUnderlyingConfiguration()).wrap();

		doc.getNamePool().allocate("xml",
				"http://www.w3.org/XML/1998/namespace", "base");
		doc.getNamePool().allocate("", "", "baz");

		final NameTest test = new NameTest(Type.ELEMENT, "", "baz", doc
				.getNamePool());
		final AxisIterator iterator = doc.iterateAxis(Axis.DESCENDANT, test);
		final NodeInfo baz = (NodeInfo) iterator.next();

		TestCase.assertEquals("http://example.org", baz.getBaseURI());
	}

	@Test
	public void testGetDeclaredNamespaces() {
		// Namespace declared.
		final AxisIterator iterator = node.iterateAxis(Axis.CHILD);
		node = (NodeWrapper) iterator.next();
		final int[] namespaces = node.getDeclaredNamespaces(new int[1]);

		node.getNamePool().allocateNamespaceCode("p", "ns");
		final int expected = node.getNamePool().getNamespaceCode("p", "ns");

		TestCase.assertEquals(expected, namespaces[0]);

		// Namespace not declared (on element node) -- returns zero length
		// array.
		final AxisIterator iter = node.iterateAxis(Axis.DESCENDANT);
		node = (NodeWrapper) iter.next();
		node = (NodeWrapper) iter.next();

		final int[] namesp = node.getDeclaredNamespaces(new int[1]);

		TestCase.assertTrue(namesp.length == 0);

		// Namespace nod declared on other nodes -- return null.
		final AxisIterator it = node.iterateAxis(Axis.DESCENDANT);
		node = (NodeWrapper) it.next();

		TestCase.assertNull(node.getDeclaredNamespaces(new int[1]));
	}

	@Test
	public void testGetStringValueCS() {
		// Test on document node.
		TestCase.assertEquals("oops1foooops2baroops3", node.getStringValueCS());

		// Test on element node.
		AxisIterator iterator = node.iterateAxis(Axis.DESCENDANT);
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals("oops1foooops2baroops3", node.getStringValueCS());

		// Test on namespace node.
		iterator = node.iterateAxis(Axis.NAMESPACE);
		NamespaceNodeImpl namespace = (NamespaceNodeImpl) iterator.next();

		/*
		 * Elements have always the default xml:NamespaceConstant.XML namespace,
		 * so we have to search if "ns" is found somewhere in the iterator
		 * (order unpredictable because it's implemented with a HashMap
		 * internally).
		 */
		while (!"ns".equals(namespace.getStringValueCS()) && namespace != null) {
			namespace = (NamespaceNodeImpl) iterator.next();
		}

		TestCase.assertEquals("ns", namespace.getStringValueCS());

		// Test on attribute node.
		final NodeWrapper attrib = (NodeWrapper) node.iterateAxis(
				Axis.ATTRIBUTE).next();
		TestCase.assertEquals("j", attrib.getStringValueCS());

		// Test on text node.
		final NodeWrapper text = (NodeWrapper) node.iterateAxis(Axis.CHILD)
				.next();
		TestCase.assertEquals("oops1", text.getStringValueCS());
	}

	@Test
	public void testGetSiblingPosition() {
		// Test every node in test document.
		final AxisIterator iterator = node.iterateAxis(Axis.DESCENDANT);
		node = (NodeWrapper) iterator.next();
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(0, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(1, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(0, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(1, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(2, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(3, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(0, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(1, node.getSiblingPosition());
		node = (NodeWrapper) iterator.next();
		TestCase.assertEquals(4, node.getSiblingPosition());
	}
}
