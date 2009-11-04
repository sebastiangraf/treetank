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
 * $Id: HelperCrossdomain.java 4322 2008-08-14 09:56:29Z kramis $
 */

package com.treetank.service.rest.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.treetank.exception.TreetankRestException;
import com.treetank.service.rest.HandledHttpServletRequest;
import com.treetank.utils.IConstants;

/**
 * Helper for the Crossdomain-Request.
 * 
 * @author Georgios Giannakaras, University of Konstanz
 * 
 */
public final class HelperCrossdomain {

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
    public void handle(final HttpServletRequest request,
            final HttpServletResponse response) throws TreetankRestException {
        try {
            response.setContentType(RESTConstants.CONTENT_TYPE.getContent());
            response.setCharacterEncoding(IConstants.DEFAULT_ENCODING);
            response.getOutputStream().write(
                    RESTConstants.CROSSDOMAIN.getContent().getBytes());
            ((HandledHttpServletRequest) request).setHandled(true);
        } catch (final IOException e) {
            throw new TreetankRestException(500, e.getMessage(), e);
        }
    }

}
