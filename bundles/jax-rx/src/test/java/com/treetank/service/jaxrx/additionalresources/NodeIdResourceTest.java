/**
 * 
 */
package com.treetank.service.jaxrx.additionalresources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.treetank.access.Database;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.implementation.TreeTank;
import com.treetank.service.jaxrx.server.StartServer;
import com.treetank.service.jaxrx.util.RESTProps;

/**
 * This class is responsible to test the {@link NodeIdResource} class.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class NodeIdResourceTest {

    /**
     * The name for the input stream file.
     */
    private static final transient String SAVENAME = "factbook978899";

    /**
     * The resource path.
     */
    private final transient static String RESOURCEURL = "http://localhost:8093/treetank/jaxrx/db/"
            + SAVENAME;

    /**
     * The client object to test the node id resource.
     */
    private final transient static Client CLIENT = Client.create();

    /**
     * The Treetank reference.
     */
    private static transient TreeTank treeTank;

    /**
     * The web resource reference.
     */
    private transient static WebResource webResource;

    /**
     * The client response object.
     */
    private transient static ClientResponse clientResponse;

    /**
     * Instances text xml static variable.
     */
    public static final transient String TEXTXML = "text/xml";

    /**
     * Instances client resource static variable.
     */
    public static final transient String CLIENTSOURCE = RESOURCEURL + "/130";

    /**
     * Instances text xml sibling static variable.
     */
    public static final transient String XMLSIBLING = "text/xml+sibling";
    /**
     * Instances text xml child static variable.
     */
    public static final transient String XMLCHILD = "text/xml+child";
    /**
     * Instances text xml query static variable.
     */
    public static final transient String XMLQUERY = "application/query+xml";
    /**
     * Instances literals true static variable.
     */
    public static final transient String LITERALSTRUE = "true";
    /**
     * Instances text literals false variable.
     */
    public static final transient String LITERALSFALSE = "false";
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
     * @throws TreetankException
     */
    @BeforeClass
    public static void beforeClass() throws FileNotFoundException,
            TreetankException {
        StartServer.main(null);

    }

    /**
     * A simple set up.
     * 
     * @throws TreetankException
     * @throws IOException
     */
    @Before
    public void setUp() throws TreetankException, IOException {

        Database.truncateDatabase(new File(RESTProps.STOREDBPATH
                + File.separator + SAVENAME + ".tnk"));
        final InputStream inputFile = NodeIdResourceTest.class.getClass()
                .getResourceAsStream("/factbook.xml");
        treeTank = new TreeTank();
        treeTank.shred(inputFile, SAVENAME);
        inputFile.close();

    }

    /**
     * A simple tear down.
     * 
     * @throws TreetankException
     * @throws InterruptedException
     */
    @After
    public void tearDown() throws TreetankException, InterruptedException {
        Database.forceCloseDatabase(new File(RESTProps.STOREDBPATH
                + File.separator + SAVENAME + ".tnk"));

    }

    /**
     * This method tests
     * {@link NodeIdResource#getResourceByID(String, String, long, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void getResourceById() {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.accept(TEXTXML).get(ClientResponse.class);
        assertEquals("Checks if status code is equal", 200, clientResponse
                .getStatus());
        assertEquals("checks if content type is equal", TEXTXML, clientResponse
                .getType().toString());
        final InputStream responseInput = clientResponse.getEntityInputStream();
        assertNotNull("checks if not null", responseInput);
    }

    /**
     * This method tests
     * {@link NodeIdResource#getResourceByID(String, String, long, javax.ws.rs.core.UriInfo)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void getResourceByIdWrapped() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.queryParam(PARAMWRAP, LITERALSTRUE)
                .accept(TEXTXML).get(ClientResponse.class);
        assertEquals("checks if status code is equal", 200, clientResponse
                .getStatus());
        assertEquals("checks if status code is equal", TEXTXML, clientResponse
                .getType().toString());
        final InputStream responseInput = clientResponse.getEntityInputStream();
        assertNotNull("checks if it is not null", responseInput);
    }

    /**
     * This method tests
     * {@link NodeIdResource#getResourceByID(String, String, long, javax.ws.rs.core.UriInfo)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void getResourceByIdBNodeid() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.queryParam(PARAMOUTPUT, LITERALSTRUE)
                .accept(TEXTXML).get(ClientResponse.class);
        assertEquals("checks if equal", 200, clientResponse.getStatus());
        assertEquals("checks if both are equal", TEXTXML, clientResponse
                .getType().toString());
        final InputStream responseInput = clientResponse.getEntityInputStream();
        assertNotNull("check if not null", responseInput);
    }

    /**
     * This method tests
     * {@link NodeIdResource#getResourceByID(String, String, long, javax.ws.rs.core.UriInfo)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void getResourceByIdWrappedNID() throws InterruptedException {
        webResource = CLIENT.resource(RESOURCEURL + "/1");
        final MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add(PARAMOUTPUT, LITERALSTRUE);
        queryParams.add(PARAMWRAP, LITERALSTRUE);
        queryParams.add(PARAMQUERY, ".//node()");
        clientResponse = webResource.queryParams(queryParams).accept(TEXTXML)
                .get(ClientResponse.class);
        assertEquals("check if both are equal", 200, clientResponse.getStatus());
        assertEquals("test if both are equal", TEXTXML, clientResponse
                .getType().toString());
        final InputStream responseInput = clientResponse.getEntityInputStream();
        assertNotNull("test if not null", responseInput);
    }

    /**
     * Checks if the 404 will be received when a not existing node id will be
     * sent.
     * 
     * @throws InterruptedException
     */
    @Test
    public void getResourceByFalseId() throws InterruptedException {
        webResource = CLIENT.resource(RESOURCEURL + "/300000");
        clientResponse = webResource.accept(TEXTXML).get(ClientResponse.class);
        assertEquals("test if 404 occurred", 404, clientResponse.getStatus());
    }

    /**
     * This method tests {@link NodeIdResourceTest#deleteResourceById()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void deleteResourceById() throws InterruptedException {
        webResource = CLIENT.resource(RESOURCEURL + "/5");
        clientResponse = webResource.accept(TEXTXML).delete(
                ClientResponse.class);
        assertEquals("test if 200 occurred", 200, clientResponse.getStatus());
        webResource = CLIENT.resource(RESOURCEURL + "/300000");
        clientResponse = webResource.accept(TEXTXML).delete(
                ClientResponse.class);
        assertEquals("test if status code 404 occurred", 404, clientResponse
                .getStatus());

    }

    /**
     * This method tests {@link NodeIdResourceTest#modifyResourceById()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void modifyResourceById() throws InterruptedException {
        webResource = CLIENT.resource(RESOURCEURL + "/8");
        clientResponse = webResource.type(TEXTXML).put(
                ClientResponse.class,
                NodeIdResourceTest.class.getClass().getResourceAsStream(
                        "/testput.xml"));
        assertEquals("test if status code 200 occurred or not", 200,
                clientResponse.getStatus());
        webResource = CLIENT.resource(RESOURCEURL + "/300000");
        clientResponse = webResource.put(ClientResponse.class,
                "<continent name=\"Hello\"/>");
        assertEquals("check for status code 404", 404, clientResponse
                .getStatus());
    }

    /**
     * This method tests the
     * {@link NodeIdResource#appendNewResource(String, String, long, InputStream, javax.ws.rs.core.HttpHeaders)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void postQuery() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.type(XMLQUERY).post(
                ClientResponse.class,
                NodeIdResourceTest.class.getClass().getResourceAsStream(
                        "/testquery2.xml"));
        assertEquals("check for 200 status code", 200, clientResponse
                .getStatus());
        clientResponse = webResource.type(XMLQUERY).post(ClientResponse.class,
                "<hello/>");
        assertEquals("check for 500 failure status code", 500, clientResponse
                .getStatus());
    }

    /**
     * This method tests the
     * {@link NodeIdResource#appendNewResource(String, String, long, InputStream, javax.ws.rs.core.HttpHeaders)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void postChild() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.type(XMLCHILD).post(ClientResponse.class,
                "<child name=\"asdf\"/>");
        assertEquals("check for 201 http status code", 201, clientResponse
                .getStatus());
        clientResponse = webResource.type(XMLCHILD).post(ClientResponse.class,
                "<child naa");
        assertEquals("test for 500 http status code", 500, clientResponse
                .getStatus());
    }

    /**
     * This method tests the
     * {@link NodeIdResource#appendNewResource(String, String, long, InputStream, javax.ws.rs.core.HttpHeaders)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void postSibling() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.type(XMLSIBLING).post(
                ClientResponse.class, "<sibling name=\"asdf\"/>");
        assertEquals("test for 201 http response code", 201, clientResponse
                .getStatus());
        clientResponse = webResource.type(XMLSIBLING).post(
                ClientResponse.class, "<sibling naa");
        assertEquals("test for 500 response code", 500, clientResponse
                .getStatus());
    }

    /**
     * This method tests the
     * {@link NodeIdResource#appendNewResource(String, String, long, InputStream, javax.ws.rs.core.HttpHeaders)}
     * 
     * @throws InterruptedException
     */
    @Test
    public void postFailure() throws InterruptedException {
        webResource = CLIENT.resource(CLIENTSOURCE);
        clientResponse = webResource.type("frank/xxxml+sg").post(
                ClientResponse.class, "<sibling name=\"asdf\"/>");
        assertEquals("test for 415 http response code", 415, clientResponse
                .getStatus());
    }

}
