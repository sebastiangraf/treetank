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

package com.treetank.service.xml.xpath;

/**
 * <h1>XPathToken</h1>
 * <p>
 * Categorized block of text.
 * </p>
 */
public class XPathToken implements XPathConstants {

    /**
     * The content of the token, a text sequence that represents a text, a
     * number, a special character etc.
     */
    private final String mContent;

    /** Specifies the type that the content of the token has. */
    private final Token mType;

    /**
     * Constructor initializing internal state.
     * 
     * @param mStr
     *            the content of the token
     * @param mType
     *            the type of the token
     */
    public XPathToken(final String mStr, final Token mType) {

        this.mContent = mStr;
        this.mType = mType;
    }

    /**
     * Gets the content of the token.
     * 
     * @return the content
     */
    public String getContent() {

        return mContent;
    }

    /**
     * Gets the type of the token.
     * 
     * @return the type
     */
    public Token getType() {

        return mType;
    }
}
