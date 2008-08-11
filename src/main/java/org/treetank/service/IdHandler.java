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
 * $Id$
 */

package org.treetank.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class IdHandler extends AbstractHandler {

  private final Map<String, TreeTankWrapper> mServices;

  public IdHandler(final Map<String, TreeTankWrapper> map) {
    mServices = map;
  }

  private final void error(
      HttpServletResponse response,
      final Exception exception) {
    error(response, 500, exception.getLocalizedMessage());
  }

  private final void error(
      final HttpServletResponse response,
      final int httpStatusCode,
      final String httpStatusMessage) {
    try {
      response.sendError(httpStatusCode, httpStatusMessage);
    } catch (Exception e) {
      e.printStackTrace();
    }
    throw new IllegalStateException(httpStatusMessage);
  }

  public void handle(
      String target,
      HttpServletRequest request,
      HttpServletResponse response,
      int dispatch) throws IOException, ServletException {

    // filter fancy stuff.
    if (request.getRequestURI().equalsIgnoreCase("/favicon.ico")) {
      error(response, 404, "Resource '/favicon.ico' not found.");
    }

    // Initialise request with defaults.
    String serviceString = "default.tnk";
    String revisionString = "()";
    String idString = "0";
    String queryString = "";

    // Parse request URI.
    final StringTokenizer tokenizer =
        new StringTokenizer(request.getRequestURI(), "/");
    if (tokenizer.hasMoreTokens()) {
      serviceString = tokenizer.nextToken();
    }
    if (tokenizer.hasMoreTokens()) {
      revisionString = tokenizer.nextToken();
      if (revisionString.length() > 2) {
        revisionString =
            revisionString.substring(1, revisionString.length() - 1);
      }
    }
    if (tokenizer.hasMoreTokens()) {
      idString = tokenizer.nextToken();
    }
    queryString = request.getQueryString();
    if (queryString != null) {
      queryString = queryString.replace("%22", "\"").replace("%20", " ");
    }

    // Get service.
    TreeTankWrapper service = null;
    synchronized (mServices) {
      service = mServices.get(serviceString);
      if (service == null) {
        service = new TreeTankWrapper(serviceString + ".tnk");
        mServices.put(serviceString, service);
      }
    }

    // Write response header.
    response.setContentType("application/xml");
    response.setCharacterEncoding("UTF-8");

    final OutputStream out = response.getOutputStream();

    try {
      long revision = -1L;
      if (revisionString.equalsIgnoreCase("()")) {
        revision = service.getLastRevisionNumber();
      } else {
        revision = Long.valueOf(revisionString);
      }

      long id = Long.valueOf(idString);

      if (service.isValid(revision, id)) {
        out
            .write(new String(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<rest:response xmlns:rest=\"REST\"><rest:sequence rest:revision=\"")
                .getBytes("UTF-8"));
        out.write(Long.toString(revision).getBytes("UTF-8"));
        out.write(new String("\">").getBytes("UTF-8"));

        final long start = System.currentTimeMillis();

        // Handle id.
        if (request.getMethod().equalsIgnoreCase("get")) {
          // --- GET ---------------------------------------------------------------
          if (queryString == null) {
            service.get(out, revision, id);
          } else {
            service.get(out, revision, id, queryString);
          }
        } else {
          error(response, 501, "Method not implemented.");
        }

        final long stop = System.currentTimeMillis();

        out.write(new String("</rest:sequence><rest:time>").getBytes("UTF-8"));
        out.write(new String(Long.toString(stop - start)).getBytes("UTF-8"));
        out.write(new String("[ms]</rest:time></rest:response>")
            .getBytes("UTF-8"));

      } else {
        error(response, 404, "Revision or id not found.");
      }
    } catch (Exception e) {
      error(response, e);
    }

    ((Request) request).setHandled(true);
  }
}