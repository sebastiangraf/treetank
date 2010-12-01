/**
 * 
 */
package com.treetank.service.jaxrx.server;

import org.jaxrx.StartJetty;

/**
 * This class starts the in JAX-RX embedded Jetty server.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class StartServer {

    /**
     * The Jetty instance.
     */
    private StartJetty jetty;

    /**
     * I'm a lazy constructor.
     * 
     * @param sPort
     *            port for the REST server.
     * 
     */
    public StartServer(final int sPort) {
        System.setProperty("org.jaxrx.systemPath",
            "com.treetank.service.jaxrx.implementation.TreeTankMediator");
        System.setProperty("org.jaxrx.systemName", "treetank");
        jetty = new StartJetty(sPort);
    }

    /**
     * This method starts the embedded Jetty server.
     * 
     * @param args
     *            Not used parameter.
     */
    public static void main(final String[] args) {
        int port = 8093;
        if (args != null && args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        System.setProperty("org.jaxrx.systemPath",
            "com.treetank.service.jaxrx.implementation.TreeTankMediator");
        System.setProperty("org.jaxrx.systemName", "treetank");
        new StartJetty(port);
    }

    /**
     * This method stops the Jetty server.
     * 
     * @throws Exception
     *             The exception occurred while stopping server.
     */
    public void stopServer() throws Exception {
        // jetty.stop();
    }
}
