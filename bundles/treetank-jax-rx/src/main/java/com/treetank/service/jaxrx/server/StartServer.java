/**
 * 
 */
package com.treetank.service.jaxrx.server;

import org.jaxrx.StartJettyServer;

/**
 * This class starts the in JAX-RX embedded Jetty server.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class StartServer {

    /**
     * I'm a lazy constructor.
     */
    private StartServer() {

    }

    /**
     * This method starts the embedded Jetty server.
     * 
     * @param args
     *            Not used parameter.
     */
    public static void main(final String[] args) {
        System.setProperty("org.jaxrx.implementation",
                "com.treetank.service.jaxrx.implementation");
        System.setProperty("org.jaxrx.systemName", "treetank");
        System.setProperty("org.jaxrx.serverport", "8093");
        System.setProperty("org.jaxrx.additionalResources",
                "com.treetank.service.jaxrx.additionalresources");
        StartJettyServer.main(null);
    }
}
