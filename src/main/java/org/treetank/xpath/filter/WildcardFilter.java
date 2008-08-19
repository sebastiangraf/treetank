/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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

package org.treetank.xpath.filter;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractFilter;

/**
 * <h1>WildcardFilter</h1>
 * <p>
 * Filters ELEMENTS and ATTRIBUTES and supports wildcards either instead of the
 * namespace prefix, or the local name.
 * </p>
 */
public class WildcardFilter extends AbstractFilter implements IFilter {

    /** Defines, if the defined part of the qualified name is the local name. */
    private final boolean mIsName;

    /** Name key of the defined name part. */
    private final int mKnownPartKey;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction to operate on
     * @param knownPart
     *            part of the qualified name that is specified. This can be
     *            either the namespace prefix, or the local name
     * @param isName
     *            defines, if the specified part is the prefix, or the local
     *            name (true, if it is the local name)
     */
    public WildcardFilter(
            final IReadTransaction rtx, final String knownPart,
            final boolean isName) {
        super(rtx);
        mIsName = isName;
        mKnownPartKey = getTransaction().keyForName(knownPart);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean filter() {
        if (getTransaction().isElementKind()) {

            if (mIsName) { // local name is given
                String localname =
                        getTransaction().getName().replaceFirst(".*:", "");
                int localnameKey = getTransaction().keyForName(localname);

                return localnameKey == mKnownPartKey;
            } else {// namespace prefix is given
                int nsCount = getTransaction().getNamespaceCount();
                for (int i = 0; i < nsCount; i++) {
                    int prefixKey = mKnownPartKey;
                    if (getTransaction().getNamespacePrefixKey(i) == prefixKey) {
                        return true;
                    }
                }
            }

        } else if (getTransaction().isAttributeKind()) {
            // supporting attributes here is difficult, because treetank
            // does not provide a way to acces the name and namespace of
            // the current attribute (attribute index is not known here)
            throw new IllegalStateException(
                    "Wildcards are not supported in attribute names yet.");
        }

        return false;

    }
}
