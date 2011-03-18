/**
 * 
 */
package org.treetank.service.jaxrx.server;

import org.jaxrx.JettyServer;

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
    private JettyServer jetty;

    /**
     * I'm a lazy constructor.
     * 
     * @param sPort
     *            port for the REST server.
     * @throws Exception
     *             Exception occurred
     * 
     */
    public StartServer(final int sPort) throws Exception {
        System.setProperty("org.jaxrx.systemPath",
            "org.treetank.service.jaxrx.implementation.TreeTankMediator");
        System.setProperty("org.jaxrx.systemName", "treetank");
        jetty = new JettyServer(sPort);
    }

    /**
     * This method starts the embedded Jetty server.
     * 
     * @param args
     *            Not used parameter.
     * @throws Exception
     *             Exception occurred.
     */
    public static void main(final String[] args) throws Exception {
        int port = 8093;
        if (args != null && args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        System.setProperty("org.jaxrx.systemPath",
            "org.treetank.service.jaxrx.implementation.TreeTankMediator");
        System.setProperty("org.jaxrx.systemName", "treetank");
        new JettyServer(port);
    }

    /**
     * This method stops the Jetty server.
     * 
     * @throws Exception
     *             The exception occurred while stopping server.
     */
    public void stopServer() throws Exception {
        jetty.stop();
    }
}
