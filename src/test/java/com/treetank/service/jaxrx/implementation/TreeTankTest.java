package com.treetank.service.jaxrx.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jaxrx.constants.EURLParameter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.treetank.exception.TreetankException;

/**
 * This class is responsible to test the implementation class {@link TreeTank};
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 * 
 */
public class TreeTankTest {

    /**
     * The name of the resource;
     */
    private static transient String resourceName = "factbookTT";

    // /**
    // * The resource path.
    // */
    // private final transient static String resourceURL =
    // "http://localhost:8093/treetank/jaxrx/db/"
    // + resourceName;
    //
    // /**
    // * The client object to test the node id resource.
    // */
    // private final transient static Client client = Client.create();
    //
    // /**
    // * The web resource reference.
    // */
    // private transient static WebResource webResource;
    //
    // /**
    // * The client response object.
    // */
    // private transient static ClientResponse clientResponse;

    /**
     * Check message for JUnit test: assertTrue
     */
    private final static transient String ASSTRUE = "check if true";
    /**
     * Check message for JUnit test: assertFalse
     */
    private final static transient String ASSFALSE = "check if false";
    /**
     * Check message for JUnit test: assertEquals
     */
    private final static transient String ASSEQUALS = "check if equals";

    /**
     * Treetank reference.
     */
    private static transient TreeTank treetank;

    /**
     * Instances xml file static variable
     */
    private static final transient String XMLFILE = "/factbook.xml";

    /**
     * Instances property static variable
     */
    private static final transient String PROPERTYVAR = "property";

    /**
     * Instances literal true static variable
     */
    private static final transient String LITERALTRUE = "true";

    /**
     * Instances literal true static variable
     */
    private static final transient String LITERALFALSE = "false";

    /**
     * This a simple setUp.
     * 
     * @throws TreetankException
     */
    @BeforeClass
    // NOPMD because we need one instance of the database (nodeid)
    public static void setUp() throws TreetankException {

        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        deleteDirectory(new File(System.getProperty("user.home")
                + File.separatorChar + "xml-databases"));
        treetank = new TreeTank();
        treetank.shred(input, resourceName);
    }

    /**
     * This method tests
     * {@link TreeTank#createResource(String, java.io.InputStream)}
     */
    @Test
    public void createResource() {
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        assertTrue(ASSTRUE, treetank.createResource(resourceName + "2", input));
    }

    /**
     * This method tests
     * {@link TreeTank#createResource(String, java.io.InputStream)}
     */
    @Test(expected = WebApplicationException.class)
    public void createResourceExc() {
        assertFalse(ASSFALSE, treetank.createResource(resourceName + "3", null));
    }

    /**
     * This method tests {@link TreeTank#getAvailableParams()}
     */
    @Test
    public void getAvailableParams() {
        final Set<EURLParameter> theParams = treetank.getAvailableParams();
        assertTrue(ASSTRUE, theParams.contains(EURLParameter.COMMAND));
        assertTrue(ASSTRUE, theParams.contains(EURLParameter.WRAP));
        assertTrue(ASSTRUE, theParams.contains(EURLParameter.QUERY));
        assertTrue(ASSTRUE, theParams.contains(EURLParameter.REVISION));
        assertTrue(ASSTRUE, theParams.contains(EURLParameter.OUTPUT));
    }

    /**
     * This method tests {@link TreeTank#getResource(String, java.util.Map)}
     * 
     * @throws TreetankException
     */
    @Test
    public void getResource() throws TreetankException {

        // webResource = client.resource(resourceURL);
        // clientResponse = webResource.get(ClientResponse.class);
        // assertEquals("test for 200 http response code", 200, clientResponse
        // .getStatus());
        //
        // MultivaluedMap<String, String> queryParams = new
        // MultivaluedMapImpl();
        // queryParams.add("output", "true");
        // queryParams.add("wrap", "true");
        // queryParams.add("query", ".//node()");
        //
        // webResource = client.resource(resourceURL);
        // clientResponse = webResource.queryParams(queryParams).get(
        // ClientResponse.class);
        // assertEquals("test for 200 http response code", 200, clientResponse
        // .getStatus());
        //
        // queryParams = new MultivaluedMapImpl();
        // queryParams.add("output", "true");
        // queryParams.add("wrap", "false");
        // queryParams.add("revision", "0");
        //
        // webResource = client.resource(resourceURL);
        // clientResponse = webResource.queryParams(queryParams).get(
        // ClientResponse.class);
        // assertEquals("test for 200 http response code", 200, clientResponse
        // .getStatus());

        Map<EURLParameter, String> paramMap;
        final String name = resourceName;
        StreamingOutput sOutput = treetank.getResource(name, null);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap.put(EURLParameter.QUERY, "//continent");
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.OUTPUT, LITERALTRUE);
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.OUTPUT, LITERALFALSE);
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.WRAP, LITERALFALSE);
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.WRAP, LITERALTRUE);
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.REVISION, "0");
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        final RIDWorker rid = new RIDWorker();
        rid.deleteResource(name, 8);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.COMMAND, "revertto:0");
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
        paramMap = new HashMap<EURLParameter, String>();
        paramMap.put(EURLParameter.OUTPUT, LITERALTRUE);
        paramMap.put(EURLParameter.QUERY, "//continent");
        paramMap.put(EURLParameter.WRAP, LITERALTRUE);
        sOutput = treetank.getResource(name, paramMap);
        assertNotNull(ASSTRUE, sOutput);
    }

    /**
     * This method tests {@link TreeTank#getResourcesNames()}
     * 
     * @throws TreetankException
     */
    @Test
    public void getResourcesNames() throws TreetankException {
        final StreamingOutput sOutput = treetank.getResourcesNames();
        assertNotNull(ASSTRUE, sOutput);
    }

    /**
     * This method tests {@link TreeTank#postResource(String, Object, boolean)}
     * 
     * @throws TreetankException
     * @throws ParserConfigurationException
     */
    @Test
    public void postResource() throws TreetankException,
            ParserConfigurationException {
        final String name = resourceName + "55";
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        treetank.shred(input, resourceName + "55");
        final RIDWorker rid = new RIDWorker();
        rid.deleteResource(name, 8);

        // first query via post

        // webResource = client.resource(resourceURL);
        // clientResponse = webResource.type("application/query+xml").post(
        // ClientResponse.class,
        // NodeIdResourceTest.class.getClass().getResourceAsStream(
        // "/testquery2.xml"));
        // assertEquals("test for 200 http response code", 200, clientResponse
        // .getStatus());

        Document newQuery = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        final Element queryElement = newQuery.createElement("query");
        final Element textElement = newQuery.createElement("text");
        textElement.setTextContent("//continent");
        queryElement.appendChild(textElement);
        final Element propertiesElement = newQuery.createElement("properties");
        final Element wrapProperty = newQuery.createElement(PROPERTYVAR);
        createNeededAtts(wrapProperty, "wrap", LITERALTRUE);
        final Element outputProperty = newQuery.createElement(PROPERTYVAR);
        createNeededAtts(outputProperty, "output", LITERALTRUE);
        final Element revertProperty = newQuery.createElement(PROPERTYVAR);
        createNeededAtts(revertProperty, "command", "revertto:0");
        final Element revisionProperty = newQuery.createElement(PROPERTYVAR);
        createNeededAtts(revisionProperty, "revision", "0");
        newQuery.appendChild(queryElement);
        assertNotNull(ASSTRUE, treetank.postResource(name, newQuery, true));
        propertiesElement.appendChild(wrapProperty);
        queryElement.appendChild(propertiesElement);
        assertNotNull(ASSTRUE, treetank.postResource(name, newQuery, true));
        propertiesElement.appendChild(outputProperty);
        assertNotNull(ASSTRUE, treetank.postResource(name, newQuery, true));
        newQuery = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();
        propertiesElement.appendChild(revisionProperty);
        assertNotNull(ASSTRUE, treetank.postResource(name, newQuery, true));
        propertiesElement.appendChild(revertProperty);
        assertNotNull(ASSTRUE, treetank.postResource(name, newQuery, true));
        final InputStream someInput = new ByteArrayInputStream(
                "<hello><a name='as'/></hello>".getBytes());
        // add to collection via post
        assertNotNull(ASSTRUE, treetank.postResource(name, someInput, false));

    }

    /**
     * This method tests {@link TreeTank#deleteResource(String)}
     * 
     * @throws TreetankException
     */
    @Test
    public void deleteResource() throws TreetankException {
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        treetank.shred(input, resourceName + "99");
        assertTrue(ASSTRUE, treetank.deleteResource(resourceName + "99"));
    }

    /**
     * This method tests {@link TreeTank#shred(java.io.InputStream, String)}
     * 
     * @throws TreetankException
     */
    @Test
    public void shred() throws TreetankException {
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        assertTrue(ASSTRUE, treetank.shred(input, resourceName + "88"));

    }

    /**
     * This method tests {@link TreeTank#getLastRevision(String)}
     * 
     * @throws TreetankException
     */
    @Test
    public void getLastRevision() throws TreetankException {
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        treetank.shred(input, resourceName + "77");
        final String name = resourceName + "77";
        assertEquals(ASSEQUALS, 0, treetank.getLastRevision(name));
        final RIDWorker rid = new RIDWorker();
        rid.deleteResource(name, 8);
        assertEquals(ASSEQUALS, 1, treetank.getLastRevision(name));
    }

    /**
     * This method tests
     * {@link TreeTank#getModificHistory(String, String, boolean, java.io.OutputStream)}
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
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        treetank.shred(input, resourceName + "66");
        final String name = resourceName + "66";
        final RIDWorker rid = new RIDWorker();
        rid.deleteResource(name, 8);
        final OutputStream output = new ByteArrayOutputStream();
        treetank.getModificHistory(name, "0-1", false, output);
        final InputStream inpSt = new ByteArrayInputStream(
                ((ByteArrayOutputStream) output).toByteArray());
        final Document doc = xmlDocument(inpSt);
        final NodeList nodes = doc.getElementsByTagName("continent");
        final int changeditems = nodes.getLength();
        assertEquals(ASSEQUALS, 1, changeditems);
    }

    /**
     * This method tests {@link TreeTank#revertToRevision(String, long)}
     * 
     * @throws TreetankException
     */
    @Test
    public void revertToRevision() throws TreetankException {
        final String name = resourceName + "55";
        final InputStream input = TreeTankTest.class
                .getResourceAsStream(XMLFILE);
        treetank.shred(input, name);
        final RIDWorker rid = new RIDWorker();
        rid.deleteResource(name, 8);
        rid.deleteResource(name, 11);
        rid.deleteResource(name, 14);
        assertEquals(ASSEQUALS, 3, treetank.getLastRevision(name));
    }

    /**
     * This method deletes a not empty directory.
     * 
     * @param path
     *            The director that has to be deleted.
     * @return <code>true</code> if the deletion process has been successful.
     *         <code>false</code> otherwise.
     */
    static public boolean deleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
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

    /**
     * This method creates the both attributes within the property element.
     * 
     * @param element
     *            The property element.
     * @param name
     *            The name of the property.
     * @param value
     *            The value of the property.
     */
    private void createNeededAtts(final Element element, final String name,
            final String value) {
        element.setAttribute("name", name);
        element.setAttribute("value", value);
    }

}
