/**
 * 
 */
package com.treetank.service.jaxrx.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class is responsible to test the {@link StartServer} class.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class StartServerTest {

    /**
     * This method tests the {@link StartServer#main(String[])} method.
     */
    @Test
    public void startServer() {
        StartServer.main(null);
        final Client client = Client.create();
        final WebResource resource = client.resource("http://localhost:8093");
        final ClientResponse anotherResponse = resource.accept("text/xml").get(ClientResponse.class);
        final int status = anotherResponse.getStatus();
        assertEquals("checks if status meets status 200", 200, status);

    }

}
