/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: TreeTankService.java 4473 2008-09-06 20:02:05Z kramis $
 */

package com.treetank.service.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;

public class TreeTankService {

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            final Map<String, TreeTankWrapper> map = new ConcurrentHashMap<String, TreeTankWrapper>();

            final Server server = new Server();
            final Connector connector = new SocketConnector();
            // final Connector connector = new SslSocketConnector();
            final Handler handler = new TreeTankHandler(map);

            connector.setPort(8182);
            //
            // ((SslSocketConnector) connector).setKeystore("keystore");
            // ((SslSocketConnector) connector).setPassword("keystore");
            // ((SslSocketConnector) connector).setKeyPassword("keystore");

            server.setConnectors(new Connector[] { connector });
            server.setHandler(handler);
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}
