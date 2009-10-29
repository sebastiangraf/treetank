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
 * $Id: TreeTankException.java 4322 2008-08-14 09:56:29Z kramis $
 */

package com.treetank.exception;

import javax.servlet.ServletException;

/**
 * Exception for the Rest-Interface to communicate with the server. Handling
 * normal http-errors.
 * 
 * @author Georgios Giannakarras, University of Konstanz
 * 
 */
public final class TreetankRestException extends ServletException {
    private static final long serialVersionUID = 1L;

    /** HTTP-Error code to encapsulate */
    private final int mErrorCode;

    /** Error message for more specification */
    private final String mErrorMessage;

    /**
     * Constructor, just getting
     * 
     * @param error
     */
    public TreetankRestException(final TreetankException error) {
        super(error);
        mErrorCode = 500;
        mErrorMessage = error.toString();
    }

    public TreetankRestException(final int errorCode, final String errorMessage) {
        super();
        mErrorCode = errorCode;
        mErrorMessage = errorMessage;
    }

    public TreetankRestException(final int errorCode,
            final String errorMessage, final Exception e) {
        super(e);
        mErrorCode = errorCode;
        mErrorMessage = errorMessage;
    }

    public final int getErrorCode() {
        return mErrorCode;
    }

    public final String getErrorMessage() {
        return mErrorMessage;
    }

}
