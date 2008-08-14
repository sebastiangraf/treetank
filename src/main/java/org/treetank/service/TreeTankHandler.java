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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

public class TreeTankHandler extends AbstractHandler {

  private static final String FAVICON = "/favicon.ico";

  private static final String CROSSDOMAIN = "/crossdomain.xml";

  private static final String POST = "POST";

  private static final String GET = "GET";

  private final HelperFavicon mHelperFavicon;

  private final HelperCrossdomain mHelperCrossdomain;

  private final HelperGet mHelperGet;

  private final HelperPost mHelperPost;

  private final HelperDelete mHelperDelete;

  public TreeTankHandler(final Map<String, TreeTankWrapper> map) {
    mHelperFavicon = new HelperFavicon();
    mHelperCrossdomain = new HelperCrossdomain();
    mHelperGet = new HelperGet(map);
    mHelperPost = new HelperPost(map);
    mHelperDelete = new HelperDelete(map);
  }

  public void handle(
      String target,
      HttpServletRequest request,
      HttpServletResponse response,
      int dispatch) throws IOException, ServletException {

    try {

      if (request.getRequestURI().equalsIgnoreCase(FAVICON)) {
        mHelperFavicon.handle(request, response);
      } else if (request.getRequestURI().equalsIgnoreCase(CROSSDOMAIN)) {
        mHelperCrossdomain.handle(request, response);
      } else if (request.getMethod().equalsIgnoreCase(GET)) {
        mHelperGet.handle(request, response);
      } else if (request.getMethod().equalsIgnoreCase(POST)) {
        final InputStream in = request.getInputStream();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final byte[] tmp = new byte[256];
        int len = 0;
        while ((len = in.read(tmp)) != -1) {
          bout.write(tmp, 0, len);
        }
        if (bout.size() == 0) {
          mHelperDelete.handle(request, response);
        } else {
          mHelperPost.handle(request, response, bout.toString());
        }
        bout.close();
      } else {
        throw new TreeTankException(501, "Unknown operation.");
      }

    } catch (TreeTankException e) {
      response.sendError(e.getErrorCode(), e.getErrorMessage());
    }

  }

}
