package com.treetank.service.jaxrx.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.util.DOMHelper;

/**
 * This class is responsible to test the implementation class
 * {@link DatabaseRepresentation};
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 * 
 */
public class DatabaseRepresentationTest {

	/**
	 * The name of the resource;
	 */
	private final static transient String RESOURCENAME = "factbookTT";

	/**
	 * Check message for JUnit test: assertTrue
	 */
	private final static transient String ASSTRUE = "check if true";

	/**
	 * Check message for JUnit test: assertEquals
	 */
	private final static transient String ASSEQUALS = "check if equals";

	/**
	 * Treetank reference.
	 */
	private static transient DatabaseRepresentation treetank;

	/**
	 * Instances xml file static variable
	 */
	private static final transient String XMLFILE = "/factbook.xml";

	/**
	 * Instances literal true static variable
	 */
	private static final transient String LITERALTRUE = "yes";

	/**
	 * The name of the result node
	 */
	private static final transient String RESULTNAME = "jaxrx:result";
	/**
	 * The name of the id attribute
	 */
	private static final transient String IDNAME = "rest:ttid";

	/**
	 * The name of the country node
	 */
	private static final transient String NAME = "name";

	/**
	 * This a simple setUp.
	 * 
	 * @throws TreetankException
	 */
	@Before
	public void setUp() throws TreetankException {

		final InputStream input = DatabaseRepresentationTest.class
				.getResourceAsStream(XMLFILE);
		treetank = new DatabaseRepresentation();
		treetank.shred(input, RESOURCENAME);
	}

	/**
	 * This is a simple tear down.
	 * 
	 * @throws TreetankException
	 */
	@After
	public void tearDown() throws TreetankException {
		treetank.deleteResource(RESOURCENAME);
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#createResource(String, java.io.InputStream)}
	 */
	@Test
	public void createResource() {
		final InputStream input = DatabaseRepresentationTest.class
				.getResourceAsStream(XMLFILE);
		treetank.createResource(input, RESOURCENAME);
		assertNotNull("check if resource has been created", treetank
				.getResource(RESOURCENAME, null));
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#createResource(String, java.io.InputStream)}
	 */
	@Test(expected = JaxRxException.class)
	public void createResourceExc() {
		treetank.createResource(null, RESOURCENAME);
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#getResource(String, java.util.Map)}
	 * 
	 * @throws TreetankException
	 * @throws IOException
	 * @throws WebApplicationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void getResource() throws TreetankException,
			WebApplicationException, IOException, ParserConfigurationException,
			SAXException {
		final Map<QueryParameter, String> queryParams = new HashMap<QueryParameter, String>();
		Document doc;
		Node node;
		Node resultNode;
		Attr attribute;
		StreamingOutput sOutput = treetank.getResource(RESOURCENAME,
				queryParams);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		doc = DOMHelper.buildDocument(output);
		node = doc.getElementsByTagName("mondial").item(0);
		attribute = (Attr) node.getAttributes().getNamedItem(IDNAME);
		resultNode = doc.getElementsByTagName(RESULTNAME).item(0);
		assertNotNull("mondial does exist", node);
		assertNull("test if result node exists - null", resultNode);
		assertNull("test if id element exists - null", attribute);
		output.close();

		queryParams.put(QueryParameter.WRAP, LITERALTRUE);
		sOutput = treetank.getResource(RESOURCENAME, queryParams);
		output = new ByteArrayOutputStream();
		sOutput.write(output);
		doc = DOMHelper.buildDocument(output);
		node = doc.getElementsByTagName("country").item(0);
		attribute = (Attr) node.getAttributes().getNamedItem(IDNAME);
		resultNode = doc.getElementsByTagName(RESULTNAME).item(0);
		assertNotNull("test if country exists", node);
		assertNotNull("test if result node exists", resultNode);
		assertNull("test if id element exists", attribute);
		output.close();

		queryParams.put(QueryParameter.OUTPUT, LITERALTRUE);
		sOutput = treetank.getResource(RESOURCENAME, queryParams);
		output = new ByteArrayOutputStream();
		sOutput.write(output);
		doc = DOMHelper.buildDocument(output);
		node = doc.getElementsByTagName(NAME).item(0);
		attribute = (Attr) node.getAttributes().getNamedItem(IDNAME);
		resultNode = doc.getElementsByTagName(RESULTNAME).item(0);
		assertNotNull("test if country exists2", node);
		assertNotNull("test if result node exists2", resultNode);
		assertNotNull("test if id element exists2", attribute);
		output.close();

		sOutput = treetank.getResource(RESOURCENAME, queryParams);
		output = new ByteArrayOutputStream();
		sOutput.write(output);
		doc = DOMHelper.buildDocument(output);
		node = doc.getElementsByTagName("city").item(0);
		attribute = (Attr) node.getAttributes().getNamedItem(IDNAME);
		resultNode = doc.getElementsByTagName(RESULTNAME).item(0);
		assertNotNull("test if city exists2", node);
		assertNotNull("test if result node exists2", resultNode);
		assertNotNull("test if id element exists2", attribute);
		output.close();

	}

	/**
	 * This method tests {@link DatabaseRepresentation#getResourcesNames()}
	 * 
	 * @throws TreetankException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void getResourcesNames() throws TreetankException,
			ParserConfigurationException, SAXException, IOException {

		final StreamingOutput sOutput = treetank.getResourcesNames();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		final Document doc = DOMHelper.buildDocument(output);
		final Node node = doc.getElementsByTagName("resource").item(0);
		assertNotNull("Check if a resource exists", node);
		Attr attribute = (Attr) node.getAttributes().getNamedItem(
				"lastRevision");
		assertNotNull("Check if lastRevision exists", attribute);
		attribute = (Attr) node.getAttributes().getNamedItem("name");
		assertNotNull("Check if name attribute exists", attribute);
		assertEquals("Check if name is the expected one", "/" + RESOURCENAME,
				attribute.getTextContent());
		output.close();
	}

	/**
	 * This method tests {@link DatabaseRepresentation#add(InputStream, String)}
	 * 
	 * @throws IOException
	 * @throws WebApplicationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws TreetankException
	 * 
	 */
	@Test
	public void addResource() throws WebApplicationException, IOException,
			ParserConfigurationException, SAXException, TreetankException {
		final InputStream input = DatabaseRepresentationTest.class
				.getResourceAsStream("/books.xml");
		treetank.add(input, RESOURCENAME);
		final Map<QueryParameter, String> params = new HashMap<QueryParameter, String>();
		params.put(QueryParameter.WRAP, LITERALTRUE);
		final StreamingOutput sOutput = treetank.performQueryOnResource(
				RESOURCENAME + ".col", ".", params);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		final Document doc = DOMHelper.buildDocument(output);
		Node node = doc.getElementsByTagName("books").item(0);
		assertNotNull("check if books has been added to factbook", node);
		node = doc.getElementsByTagName("mondial").item(0);
		assertNotNull("check if mondial still exists", node);
		output.close();
		treetank.deleteResource(RESOURCENAME + ".col");
		setUp();

	}

	/**
	 * This method tests {@link DatabaseRepresentation#deleteResource(String)}
	 * 
	 * @throws TreetankException
	 * @throws IOException
	 * @throws WebApplicationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void deleteResource() throws TreetankException,
			WebApplicationException, IOException, ParserConfigurationException,
			SAXException {
		final InputStream input = DatabaseRepresentationTest.class
				.getResourceAsStream(XMLFILE);
		treetank.shred(input, RESOURCENAME + "99");
		treetank.deleteResource(RESOURCENAME + "99");
		final StreamingOutput sOutput = treetank.getResourcesNames();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		final Document doc = DOMHelper.buildDocument(output);
		final NodeList nodes = doc.getElementsByTagName("resource");
		String searchName = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			final Attr attribute = (Attr) nodes.item(i).getAttributes()
					.getNamedItem(NAME);
			if (attribute.getTextContent().equals(RESOURCENAME + "99")) {
				searchName = attribute.getTextContent();
				break;
			}
		}
		assertNull("Check if the resource has been deleted", searchName);
		output.close();
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#shred(java.io.InputStream, String)}
	 * 
	 * @throws TreetankException
	 */
	@Test
	public void shred() throws TreetankException {
		final InputStream input = DatabaseRepresentationTest.class
				.getResourceAsStream(XMLFILE);
		assertTrue(ASSTRUE, treetank.shred(input, RESOURCENAME + "88"));
		treetank.deleteResource(RESOURCENAME + "88");

	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#performQueryOnResource(String, String, Map)}
	 * 
	 * @throws IOException
	 * @throws WebApplicationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void performQueryOnResource() throws WebApplicationException,
			IOException, ParserConfigurationException, SAXException {

		final Map<QueryParameter, String> params = new HashMap<QueryParameter, String>();
		params.put(QueryParameter.OUTPUT, LITERALTRUE);
		params.put(QueryParameter.WRAP, LITERALTRUE);
		final StreamingOutput sOutput = treetank.performQueryOnResource(
				RESOURCENAME, "//continent", params);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		final Document doc = DOMHelper.buildDocument(output);
		Node node = doc.getElementsByTagName("continent").item(0);
		assertNotNull("check if continent exists", node);
		node = doc.getElementsByTagName("country").item(0);
		assertNull("check for null country object", node);
		output.close();
	}

	/**
	 * This method tests {@link DatabaseRepresentation#getLastRevision(String)}
	 * 
	 * @throws TreetankException
	 */
	@Test
	public void getLastRevision() throws TreetankException {
		assertEquals(ASSEQUALS, 0, treetank.getLastRevision(RESOURCENAME));
		final NodeIdRepresentation rid = new NodeIdRepresentation();
		rid.deleteResource(RESOURCENAME, 8);
		assertEquals(ASSEQUALS, 1, treetank.getLastRevision(RESOURCENAME));
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#getModificHistory(String, String, boolean, java.io.OutputStream)}
	 * 
	 * @throws TreetankException
	 * @throws WebApplicationException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void getModificHistory() throws WebApplicationException,
			TreetankException, SAXException, IOException,
			ParserConfigurationException {
		final NodeIdRepresentation rid = new NodeIdRepresentation();
		rid.deleteResource(RESOURCENAME, 8);
		final OutputStream output = new ByteArrayOutputStream();
		treetank.getModificHistory(RESOURCENAME, "0-1", false, output, true);
		final InputStream inpSt = new ByteArrayInputStream(
				((ByteArrayOutputStream) output).toByteArray());
		final Document doc = xmlDocument(inpSt);
		final NodeList nodes = doc.getElementsByTagName("continent");
		final int changeditems = nodes.getLength();
		assertEquals(ASSEQUALS, 1, changeditems);
	}

	/**
	 * This method tests
	 * {@link DatabaseRepresentation#revertToRevision(String, long)}
	 * 
	 * @throws TreetankException
	 * @throws IOException
	 * @throws WebApplicationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws InterruptedException
	 */
	@Test
	public void revertToRevision() throws TreetankException,
			WebApplicationException, IOException, ParserConfigurationException,
			SAXException, InterruptedException {
		final NodeIdRepresentation rid = new NodeIdRepresentation();
		rid.deleteResource(RESOURCENAME, 8);
		rid.deleteResource(RESOURCENAME, 11);
		rid.deleteResource(RESOURCENAME, 14);
		assertEquals(ASSEQUALS, 3, treetank.getLastRevision(RESOURCENAME));
		treetank.revertToRevision(RESOURCENAME, 0);
		final StreamingOutput sOutput = rid.getResource(RESOURCENAME, 14,
				new HashMap<QueryParameter, String>());
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		sOutput.write(output);
		final Document doc = DOMHelper.buildDocument(output);
		final Node node = doc.getElementsByTagName("continent").item(0);
		final Attr attribute = (Attr) node.getAttributes().getNamedItem(NAME);
		final String africaString = attribute.getTextContent();
		assertNotNull("check if africa (14) exists in the latest version",
				africaString);
		assertEquals(ASSEQUALS, 4, treetank.getLastRevision(RESOURCENAME));
		output.close();
	}

	/**
	 * This method creates of an input stream an XML document.
	 * 
	 * @param input
	 *            The input stream.
	 * @return The packed XML document.
	 * @throws SAXException
	 *             Exception occurred.
	 * @throws IOException
	 *             Exception occurred.
	 * @throws ParserConfigurationException
	 *             Exception occurred.
	 */
	private Document xmlDocument(final InputStream input) throws SAXException,
			IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				input);
	}

}
