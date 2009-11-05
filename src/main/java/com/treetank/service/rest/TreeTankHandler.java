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
 * $Id: TreeTankHandler.java 4471 2008-09-06 18:52:17Z kramis $
 */

package com.treetank.service.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.treetank.api.ISession;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankRestException;
import com.treetank.service.rest.helper.HelperDelete;
import com.treetank.service.rest.helper.HelperGet;
import com.treetank.service.rest.helper.HelperPost;
import com.treetank.service.rest.helper.HelperPut;
import com.treetank.session.Session;
import com.treetank.utils.IConstants;

public class TreeTankHandler extends AbstractHandler {

    private HelperGet mHelperGet;

    private HelperPost mHelperPost;

    private HelperPut mHelperPut;

    private HelperDelete mHelperDelete;

    private final static ConcurrentHashMap<File, TreeTankHandler> instancesPerFile = new ConcurrentHashMap<File, TreeTankHandler>();

    private final ISession session;

    private final File path;

    private TreeTankHandler(final File paramPath) throws TreetankIOException {
        session = Session.beginSession(paramPath);
        path = paramPath;
    }

    /**
     * Public singleton getter. This method works multithreaded for all
     * accessing threads.
     * 
     * @param file
     *            the session to be bound. If not existing, a new session will
     *            be opened.
     * @return an instance of this class
     * @throws TreetankIOException
     *             if the opening fails
     */
    public static TreeTankHandler getHandler(final File file)
            throws TreetankIOException {
        return instancesPerFile.putIfAbsent(file, new TreeTankHandler(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final String target,
            final HttpServletRequest paramRequest,
            final HttpServletResponse response, final int dispatch)
            throws IOException, ServletException {
        final HandledRequest request = new HandledRequest(
                (Request) paramRequest);
        try {

            if (request.getRequestURI().equalsIgnoreCase(
                    RESTConstants.FAVICONPATH.getStringContent())) {
                request.setHandled(true);
            } else if (request.getRequestURI().equalsIgnoreCase(
                    RESTConstants.CROSSDOMAINPATH.getStringContent())) {
                handleCrossDomain(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.JAVASCRIPT.getStringContent())) {
                handleFile(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.FLEX.getStringContent())) {
                handleFile(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.STYLE.getStringContent())) {
                handleFile(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.GIF.getStringContent())) {
                handleFile(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.JPEG.getStringContent())) {
                handleFile(request, response);
            } else if (request.getRequestURI().endsWith(
                    RESTConstants.PNG.getStringContent())) {
                handleFile(request, response);
            } else if (request.getMethod().equalsIgnoreCase(
                    RESTConstants.GET.getStringContent())) {
                mHelperGet.handle(request, response);
            } else if (request.getMethod().equalsIgnoreCase(
                    RESTConstants.POST.getStringContent())) {
                if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.DELETE.getStringContent())) {
                    mHelperDelete.handle(request, response);
                } else if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.POST.getStringContent())) {
                    mHelperPost.handle(request, response);
                } else if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.PUT.getStringContent())) {
                    mHelperPut.handle(request, response);
                } else {
                    throw new TreetankRestException(501, "Unknown operation.");
                }
            } else {
                throw new TreetankRestException(501, "Unknown operation.");
            }

        } catch (final TreetankRestException exc) {
            exc.printStackTrace();
            response.sendError(exc.getErrorCode(), exc.getErrorMessage());
        }
    }

    /**
     * Helping just for cross domain requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handleCrossDomain(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {
        try {
            response.setContentType(RESTConstants.CONTENT_TYPE
                    .getStringContent());
            response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);
            response.getOutputStream().write(
                    RESTConstants.CROSSDOMAIN.getStringContent().getBytes());
            request.setHandled(true);
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }
    }

    /**
     * Helping just for file requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handleFile(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {
        response.setContentType(RESTConstants.CONTENT_TYPE.getStringContent());
        response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);
        response.setBufferSize(RESTConstants.BUFFER_SIZE.getIntContent());
        try {
            final File file = new File(path.getAbsoluteFile()
                    + request.getRequestURI());
            final FileInputStream fin = new FileInputStream(file);
            final byte[] tmp = new byte[RESTConstants.BUFFER_SIZE
                    .getIntContent() / 2];
            int length = 0;
            while ((length = fin.read(tmp)) != -1) {
                response.getOutputStream().write(tmp, 0, length);
            }
            fin.close();

            response.flushBuffer();

            request.setHandled(true);
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }
    }

}
