package com.treetank.service.jaxrx.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jaxrx.constants.EURLParameter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.enums.EIdPostType;

/**
 * This class is responsible to test the {@link RIDWorker} class.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class RIDWorkerTest {
    /**
     * The RIDWorker reference.
     */
    private transient static RIDWorker ridWorker;
    /**
     * The TreeTank reference.
     */
    private transient static TreeTank treeTank;
    /**
     * This variable defines the node id from where the resource should be
     * retrieved
     */
    private static transient final long NODEIDGETRESOURCE = 8;
    /**
     * This variable defines the node id that should be modified
     */
    private static transient final long NODEIDTOMODIFY = 11;
    /**
     * This variable defines the node id where a child and sibling element
     * should be appended
     */
    private static transient final long NODEIDTOAPPEND = 14;

    /**
     * The test file that has to be saved on the server.
     */
    private static final transient InputStream INPUTFILE = RIDWorkerTest.class
            .getClass().getResourceAsStream("/factbook.xml");

    /**
     * The name for the input stream file.
     */
    private static final transient String RESOURCENAME = "factyTest";

    /**
     * Instances the Literal true static variable
     */
    private static final transient String LITERALSTRUE = "true";
    /**
     * Instances the Literal false static variable
     */
    private static final transient String LITERALSFALSE = "false";
    /**
     * Instances the Literal property static variable
     */
    private static final transient String LITERALSPROPERTY = "property";
    /**
     * Instances text param query static variable.
     */
    public static final transient String PARAMQUERY = "query";
    /**
     * Instances text param wrap static variable.
     */
    public static final transient String PARAMWRAP = "wrap";
    /**
     * Instances text param output static variable.
     */
    public static final transient String PARAMOUTPUT = "output";

    /**
     * A simple set up.
     * 
     * @throws FileNotFoundException
     */
    @BeforeClass
    // NOPMD because we need one instance of the database (nodeid)
    public static void setUp() throws FileNotFoundException, TreetankException {
        ridWorker = new RIDWorker();
        treeTank = new TreeTank();
        treeTank.shred(INPUTFILE, RESOURCENAME);
    }

    /**
     * A simple tear down.
     */
    // @AfterClass
    // public void tearDown() {
    // treeTank.deleteResource(resourceName);
    // }
    /**
     * This method tests {@link RIDWorker#getAvaliableParams()}
     */
    @Test
    public void testAvailableParams() {
        final Set<EURLParameter> testAvParams = new HashSet<EURLParameter>();
        testAvParams.add(EURLParameter.WRAP);
        testAvParams.add(EURLParameter.OUTPUT);
        testAvParams.add(EURLParameter.QUERY);

        assertEquals("Test Available Params", testAvParams, ridWorker
                .getAvaliableParams());
    }

    /**
     * This method tests {@link RIDWorker#getResource(String, long, Map)}
     */
    @Test
    public void testGetResource() {

        final Map<EURLParameter, String> queryParams = new HashMap<EURLParameter, String>();
        queryParams.put(EURLParameter.OUTPUT, LITERALSTRUE);
        queryParams.put(EURLParameter.QUERY, "//city");
        queryParams.put(EURLParameter.WRAP, LITERALSTRUE);
        queryParams.put(EURLParameter.REVISION, "0");
        assertNotNull("getResource Test 1", ridWorker.getResource(RESOURCENAME,
                NODEIDGETRESOURCE, queryParams));

        queryParams.clear();
        queryParams.put(EURLParameter.OUTPUT, LITERALSFALSE);
        queryParams.put(EURLParameter.QUERY, null);
        queryParams.put(EURLParameter.WRAP, LITERALSFALSE);
        queryParams.put(EURLParameter.REVISION, null);
        assertNotNull("getResource Test 2", ridWorker.getResource(RESOURCENAME,
                NODEIDGETRESOURCE, queryParams));

        queryParams.clear();
        queryParams.put(EURLParameter.OUTPUT, LITERALSFALSE);
        queryParams.put(EURLParameter.QUERY, "//city");
        queryParams.put(EURLParameter.WRAP, LITERALSTRUE);
        queryParams.put(EURLParameter.REVISION, null);
        assertNotNull("getResource Test 3", ridWorker.getResource(RESOURCENAME,
                NODEIDGETRESOURCE, queryParams));

        queryParams.clear();
        queryParams.put(EURLParameter.OUTPUT, LITERALSFALSE);
        queryParams.put(EURLParameter.QUERY, null);
        queryParams.put(EURLParameter.WRAP, LITERALSTRUE);
        queryParams.put(EURLParameter.REVISION, "0");
        assertNotNull("getResource Test 4", ridWorker.getResource(RESOURCENAME,
                NODEIDGETRESOURCE, queryParams));
    }

    /**
     * This method tests {@link RIDWorker#deleteResource(String, long)}
     */
    @Test
    public void testDeleteResource() {
        assertTrue("Test delete resource", ridWorker.deleteResource(
                RESOURCENAME, 5));
    }

    /**
     * This method tests
     * {@link RIDWorker#modifyResource(String, long, InputStream)}
     */
    @Test
    public void testModifyResource() throws TreetankException {
        final InputStream inputStream = new ByteArrayInputStream("<testNode/>"
                .getBytes());
        long lastRevision = treeTank.getLastRevision(RESOURCENAME);
        ridWorker.modifyResource(RESOURCENAME, NODEIDTOMODIFY, inputStream);
        assertEquals("Test modify resource", treeTank
                .getLastRevision(RESOURCENAME), ++lastRevision);

    }

    /**
     * This method tests
     * {@link RIDWorker#postResource(String, long, Object, EIdPostType)}
     * 
     * @throws ParserConfigurationException
     */
    @Test
    public void testPostResource() throws IOException, WebApplicationException,
            TreetankException, ParserConfigurationException {
        Response response;

        // test perform query function
        final Document newQuery = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        final Element queryElement = newQuery.createElement("query");
        final Element textElement = newQuery.createElement("text");
        textElement.setTextContent("//continent");
        queryElement.appendChild(textElement);
        final Element wrapProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(wrapProperty, "wrap", "true");
        final Element outputProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(outputProperty, "output", "true");
        final Element revertProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(revertProperty, "command", "revertto:0");
        final Element revisionProperty = newQuery
                .createElement(LITERALSPROPERTY);
        createNeededAtts(revisionProperty, "revision", "0");
        newQuery.appendChild(queryElement);

        response = ridWorker.postResource(RESOURCENAME, NODEIDGETRESOURCE,
                newQuery, EIdPostType.PERFORMQUERY);

        assertEquals("test webapplication response", 200, response.getStatus());

        // test append new child resource
        InputStream inputStream = new ByteArrayInputStream("<testNodeInput/>"
                .getBytes());
        long lastRevision = treeTank.getLastRevision(RESOURCENAME);

        response = ridWorker.postResource(RESOURCENAME, NODEIDTOAPPEND,
                inputStream, EIdPostType.APPENDCHILD);

        assertEquals("test append new child resource", treeTank
                .getLastRevision(RESOURCENAME), ++lastRevision);
        assertEquals("test webapplication response", 201, response.getStatus());

        inputStream.close();
        inputStream = new ByteArrayInputStream("<testNodeInput/>".getBytes());

        // test append new sibling resource
        lastRevision = treeTank.getLastRevision(RESOURCENAME);
        response = ridWorker.postResource(RESOURCENAME, NODEIDTOAPPEND,
                inputStream, EIdPostType.APPENDSIBLING);
        assertEquals("test append new sibling resource", treeTank
                .getLastRevision(RESOURCENAME), ++lastRevision);
        assertEquals("test webapplication response", 201, response.getStatus());

        inputStream.close();
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
