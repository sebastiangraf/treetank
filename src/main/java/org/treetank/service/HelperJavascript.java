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

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;

public final class HelperJavascript {

  private static final String PATH = "/treetank/service/";

  private static final String CONTENT_TYPE = "text/ecmascript";

  private static final String ENCODING = "UTF-8";

  public final void handle(
      final HttpServletRequest request,
      final HttpServletResponse response) throws TreeTankException {
    try {
      response.setContentType(CONTENT_TYPE);
      response.setCharacterEncoding(ENCODING);

      final File file = new File(PATH + request.getRequestURI());
      final FileInputStream fin = new FileInputStream(file);
      final byte[] tmp = new byte[4096];
      int length = 0;
      while ((length = fin.read(tmp)) != -1) {
        response.getOutputStream().write(tmp, 0, length);
      }
      fin.close();

      ((Request) request).setHandled(true);
    } catch (Exception e) {
      e.printStackTrace();
      throw new TreeTankException(500, e.getMessage(), e);
    }
  }

}
