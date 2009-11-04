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

package com.treetank.service.rest.helper;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;

import com.treetank.exception.TreetankRestException;
import com.treetank.utils.IConstants;

public final class HelperFile {

    private static final String PATH = "/treetank/data/";

    private final String mContentType;

    public HelperFile(final String contentType) {
        mContentType = contentType;
    }

    public void handle(final HttpServletRequest request,
            final HttpServletResponse response) throws TreetankRestException {
        try {
            response.setContentType(mContentType);
            response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);

            response.setBufferSize(8192);

            final File file = new File(PATH + request.getRequestURI());
            final FileInputStream fin = new FileInputStream(file);
            final byte[] tmp = new byte[4096];
            int length = 0;
            while ((length = fin.read(tmp)) != -1) {
                response.getOutputStream().write(tmp, 0, length);
            }
            fin.close();

            response.flushBuffer();

            ((Request) request).setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TreetankRestException(500, e.getMessage(), e);
        }
    }

}
