/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.exception;

/**
 * Exception to hold all relevant failures upcoming from Treetank.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class TreetankException extends Exception {

    /** General ID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to encapsulate parsing.
     * 
     * @param mExc
     *            to encapsulate
     */
    public TreetankException(final Exception mExc) {
        super(mExc);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            , convinience for super-constructor
     */
    private TreetankException(final StringBuilder message) {
        super(message.toString());
    }

    /**
     * Constructor.
     * 
     * @param message
     *            message as string, they are concatenated with spaces in
     *            between
     */
    public TreetankException(final String... message) {
        this(concat(message));
    }

    /**
     * Util method to provide StringBuilder functionality.
     * 
     * @param message
     *            to be concatenated
     * @return the StringBuilder for the combined string
     */
    private static StringBuilder concat(final String... message) {
        final StringBuilder builder = new StringBuilder();
        for (final String mess : message) {
            builder.append(mess);
            builder.append(" ");
        }
        return builder;
    }

}
