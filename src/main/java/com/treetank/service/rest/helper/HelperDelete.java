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
 * $Id: HelperDelete.java 4463 2008-09-03 09:30:37Z kramis $
 */

package com.treetank.service.rest.helper;

import java.io.OutputStream;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;

import com.treetank.exception.TreetankRestException;
import com.treetank.service.rest.TreeTankWrapper;
import com.treetank.utils.IConstants;

public final class HelperDelete {

    private static final String PATH = "/treetank/data/";

    private static final String CONTENT_TYPE = "application/xml";

    private final Map<String, TreeTankWrapper> mServices;

    public HelperDelete(final Map<String, TreeTankWrapper> map) {
        mServices = map;
    }

    public final void handle(final HttpServletRequest request,
            final HttpServletResponse response) throws TreetankRestException {

        try {

            response.setBufferSize(8192);

            // Initialise request with defaults.
            String serviceString = "default.tnk";
            String idString = "0";
            String queryString = "";

            // Parse request URI.
            final StringTokenizer tokenizer = new StringTokenizer(request
                    .getRequestURI(), "/");
            if (tokenizer.hasMoreTokens()) {
                serviceString = tokenizer.nextToken();
            }
            if (tokenizer.hasMoreTokens()) {
                idString = tokenizer.nextToken();
            }
            queryString = request.getQueryString();
            if (queryString != null) {
                queryString = queryString.replace("%22", "\"").replace("%20",
                        " ");
            }

            // Get service.
            TreeTankWrapper service = null;
            synchronized (mServices) {
                service = mServices.get(serviceString);
                if (service == null) {
                    service = new TreeTankWrapper(PATH + serviceString + ".tnk");
                    mServices.put(serviceString, service);
                }
            }
            final long id = Long.valueOf(idString);

            // Make modifications.
            final long revision = service.delete(id);

            // Write response header.
            response.setContentType(CONTENT_TYPE);
            response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);

            // Write response body.
            final OutputStream out = response.getOutputStream();

            final long start = System.currentTimeMillis();

            out
                    .write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<rest:response xmlns:rest=\"REST\"><rest:sequence rest:revision=\"")
                            .getBytes(IConstants.DEFAULT_ENCODING));
            out.write(Long.toString(revision).getBytes(
                    IConstants.DEFAULT_ENCODING));
            out.write(new String("\"/>").getBytes(IConstants.DEFAULT_ENCODING));

            // Time measurement
            final long stop = System.currentTimeMillis();
            out.write("<rest:time>".getBytes(IConstants.DEFAULT_ENCODING));
            out.write(Long.toString(stop - start).getBytes(
                    IConstants.DEFAULT_ENCODING));
            out.write("[ms]</rest:time></rest:response>"
                    .getBytes(IConstants.DEFAULT_ENCODING));

            response.flushBuffer();

            ((Request) request).setHandled(true);

        } catch (TreetankRestException te) {
            throw te;
        } catch (Exception e) {
            throw new TreetankRestException(500, e.getMessage(), e);
        }

    }

}
