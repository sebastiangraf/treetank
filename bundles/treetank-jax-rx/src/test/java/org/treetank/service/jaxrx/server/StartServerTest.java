/**
 * 
 */
package org.treetank.service.jaxrx.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.treetank.service.jaxrx.server.StartServer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class is responsible to test the {@link StartServer} class.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class StartServerTest {

    /**
     * This method tests the {@link StartServer#main(String[])} method.
     * 
     * @throws Exception
     *             Exception occurred.
     */
    @Test
    public void startServer() throws Exception {
        StartServer.main(new String[]{"8093"});
        final Client client = Client.create();
        final WebResource resource = client.resource("http://localhost:8093");
        final ClientResponse anotherResponse = resource.accept("text/xml").get(ClientResponse.class);
        final int status = anotherResponse.getStatus();
        assertEquals("checks if status meets status 200", 200, status);

    }

}
