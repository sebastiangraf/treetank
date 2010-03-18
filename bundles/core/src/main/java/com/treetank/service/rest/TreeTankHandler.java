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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankRestException;
import com.treetank.utils.IConstants;

public class TreeTankHandler extends AbstractHandler {

    private static final ConcurrentHashMap<File, TreeTankHandler> instancesPerFile = new ConcurrentHashMap<File, TreeTankHandler>();

    private final ConcurrentHashMap<File, TreeTankWrapper> sessions;

    private final File path;

    private TreeTankHandler(final File paramPath) throws TreetankIOException {
        path = paramPath;
        sessions = new ConcurrentHashMap<File, TreeTankWrapper>();
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
            handleResponseHeader(response);
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
                handleGet(request, response);
            } else if (request.getMethod().equalsIgnoreCase(
                    RESTConstants.POST.getStringContent())) {
                if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.DELETE.getStringContent())) {
                    handleDelete(request, response);
                } else if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.POST.getStringContent())) {
                    handlePost(request, response);
                } else if (request.getQueryString().equalsIgnoreCase(
                        RESTConstants.PUT.getStringContent())) {
                    handlePut(request, response);
                } else {
                    throw new TreetankRestException(501, "Unknown operation.");
                }
            } else {
                throw new TreetankRestException(501, "Unknown operation.");
            }
            request.setHandled(true);
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
            response.getOutputStream().write(
                    RESTConstants.CROSSDOMAIN.getStringContent().getBytes());

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
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }
    }

    /**
     * Helping just for delete requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handleDelete(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {

        // Parse request URI.
        final StringTokenizer tokenizer = new StringTokenizer(request
                .getRequestURI(), "/");
        if (tokenizer.countTokens() != 2) {
            throw new TreetankRestException(
                    500,
                    new StringBuilder(
                            "DELETE should consist out of 2 params: Ressource and ID, Following Tokens were found: ")
                            .append(tokenizer.toString()).toString());
        }

        final File resource = getRessource(tokenizer.nextToken());
        final long key = Long.parseLong(tokenizer.nextToken());

        final TreeTankWrapper session = sessions.putIfAbsent(resource,
                new TreeTankWrapper(resource));
        final long revision = session.delete(key);

        OutputStream out;
        try {
            out = response.getOutputStream();

            // Write response body.
            handleResponseBodyFirstPart(out, revision);
            handleResponseBodyLastPart(out);
            out.flush();
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

    }

    /**
     * Helping just for get requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handlePut(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {
        // Parse request URI.
        final StringTokenizer tokenizer = new StringTokenizer(request
                .getRequestURI(), "/");

        // Initialise request with defaults.
        long key = 0;

        if (tokenizer.countTokens() < 1) {
            throw new TreetankRestException(500, new StringBuilder(
                    "Put should consist at least out of 1 params.").toString());
        }
        final File resource = getRessource(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            key = Long.parseLong(tokenizer.nextToken());
        }
        try {
            final TreeTankWrapper session = sessions.putIfAbsent(resource,
                    new TreeTankWrapper(resource));
            final String content = getContent(request);
            final long revision = session.put(key, content);
            final OutputStream out = response.getOutputStream();
            handleResponseBodyFirstPart(out, revision);
            session.get(out, revision, key);
            handleResponseBodyLastPart(out);
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

    }

    /**
     * Helping just for get requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handlePost(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {

        // Parse request URI.
        final StringTokenizer tokenizer = new StringTokenizer(request
                .getRequestURI(), "/");

        // Initialise request with defaults.
        long key = 0;

        if (tokenizer.countTokens() < 1) {
            throw new TreetankRestException(500, new StringBuilder(
                    "Post should consist at least out of 1 params.").toString());
        }
        final File resource = getRessource(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            key = Long.parseLong(tokenizer.nextToken());
        }
        try {
            final TreeTankWrapper session = sessions.putIfAbsent(resource,
                    new TreeTankWrapper(resource));
            final String content = getContent(request);
            final long revision = session.post(key, content);
            final OutputStream out = response.getOutputStream();
            handleResponseBodyFirstPart(out, revision);
            session.get(out, revision, key);
            handleResponseBodyLastPart(out);
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

    }

    /**
     * Helping just for get requests
     * 
     * @param request
     *            to handle
     * @param response
     *            to give back
     * @throws TreetankRestException
     *             if any weird happen
     */
    private void handleGet(final HandledRequest request,
            final HttpServletResponse response) throws TreetankRestException {

        // Parse request URI.
        final StringTokenizer tokenizer = new StringTokenizer(request
                .getRequestURI(), "/");

        // Initialise request with defaults.
        String revisionString = RESTConstants.LAST_REVISION.getStringContent();
        long key = 0;
        String queryString = "";

        if (tokenizer.countTokens() < 1) {
            throw new TreetankRestException(500, new StringBuilder(
                    "Get should consist at least out of 1 params.").toString());
        }
        final File resource = getRessource(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            revisionString = tokenizer.nextToken();
            if (revisionString.length() > 2) {
                revisionString = revisionString.substring(1, revisionString
                        .length() - 1);
            }
        }
        if (tokenizer.hasMoreTokens()) {
            key = Long.parseLong(tokenizer.nextToken());
        }
        queryString = request.getQueryString();
        if (queryString != null) {
            queryString = queryString.replace("%22", "\"").replace("%20", " ");
        }
        final TreeTankWrapper session = sessions.putIfAbsent(resource,
                new TreeTankWrapper(resource));

        long revision = 0;
        if (revisionString.equalsIgnoreCase(RESTConstants.LAST_REVISION
                .getStringContent())) {
            revision = session.getLastRevision();
        } else {
            final long lastRevision = session.getLastRevision();
            revision = Long.valueOf(revisionString);
            if (lastRevision < revision) {
                throw new TreetankRestException(404, new StringBuilder(
                        "Revision=").append(revision).append(" not found.")
                        .toString());
            }
        }

        OutputStream out;
        try {
            out = response.getOutputStream();

            // Write response body.
            handleResponseBodyFirstPart(out, revision);

            // Handle.
            if (queryString == null) {
                session.get(out, revision, key);
            } else {
                session.get(out, revision, key, queryString);
            }
            handleResponseBodyLastPart(out);
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

    }

    private void handleResponseBodyFirstPart(final OutputStream out,
            final long revision) throws TreetankRestException {
        try {
            out
                    .write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<rest:response xmlns:rest=\"REST\"><rest:sequence rest:revision=\"")
                            .getBytes(IConstants.DEFAULT_ENCODING));
            out.write(Long.toString(revision).getBytes(
                    IConstants.DEFAULT_ENCODING));
            out.write(new String("\"/>").getBytes(IConstants.DEFAULT_ENCODING));
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }
    }

    private void handleResponseBodyLastPart(final OutputStream out)
            throws TreetankRestException {
        try {
            out.write(new String("</rest:response>")
                    .getBytes(IConstants.DEFAULT_ENCODING));
            out.flush();
        } catch (final IOException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }
    }

    private void handleResponseHeader(final HttpServletResponse response) {
        response.setBufferSize(RESTConstants.BUFFER_SIZE.getIntContent());
        response.setContentType(RESTConstants.CONTENT_TYPE.getStringContent());
        response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);
    }

    private File getRessource(final String ressource) {
        return new File(new StringBuilder(path.getAbsolutePath()).append(
                File.separator).append(ressource).toString());
    }

    private String getContent(final HandledRequest request) throws IOException {
        // Get request body.
        final InputStream in = request.getInputStream();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final byte[] tmp = new byte[256];
        int len = 0;
        while ((len = in.read(tmp)) != -1) {
            bout.write(tmp, 0, len);
        }
        final String requestBody = bout.toString();
        bout.close();
        return requestBody;

    }

}
