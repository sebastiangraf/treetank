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

import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;

/**
 * <h1>SingleType</h1>
 * <p>
 * A single type defines a type the a single item can have. It consists of an atomic type and a optional
 * interrogation that, when present indicates that the item can also be the empty sequence.
 * </p>
 */
public class SingleType {

    private Type mAtomicType;

    private final boolean mhasInterogation;

    /**
     * Constructor.
     * 
     * @param atomic
     *            string representation of the atomic value
     * @param mIntero
     *            true, if interrogation sign is present
     */
    public SingleType(final String atomic, final boolean mIntero) {

        // get atomic type
        mAtomicType = null; // TODO. = null is not good style
        for (Type type : Type.values()) {
            if (type.getStringRepr().equals(atomic)) {
                mAtomicType = type;
                break;
            }
        }

        if (mAtomicType == null) {
            throw new XPathError(ErrorType.XPST0051);
        }

        mhasInterogation = mIntero;
    }

    /**
     * Gets the atomic type.
     * 
     * @return atomic type.
     */
    public Type getAtomic() {

        return mAtomicType;
    }

    /**
     * Specifies, whether interrogation sign is present and therefore the empty
     * sequence is valid too.
     * 
     * @return true, if interrogation sign is present.
     */
    public boolean hasInterogation() {

        return mhasInterogation;
    }

}
