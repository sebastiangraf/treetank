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

package com.treetank.axis.filter;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

/**
 * <h1>WildcardFilter</h1>
 * <p>
 * Filters ELEMENTS and ATTRIBUTES and supports wildcards either instead of the namespace prefix, or the local
 * name.
 * </p>
 */
public class WildcardFilter extends AbsFilter {

    /** Defines, if the defined part of the qualified name is the local name. */
    private final boolean mIsName;

    /** Name key of the defined name part. */
    private final int mKnownPartKey;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction to operate on
     * @param mKnownPart
     *            part of the qualified name that is specified. This can be
     *            either the namespace prefix, or the local name
     * @param mIsName
     *            defines, if the specified part is the prefix, or the local
     *            name (true, if it is the local name)
     */
    public WildcardFilter(final IReadTransaction rtx, final String mKnownPart, final boolean mIsName) {
        super(rtx);
        this.mIsName = mIsName;
        mKnownPartKey = getTransaction().keyForName(mKnownPart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean filter() {
        if (getTransaction().getNode().getKind() == ENodes.ELEMENT_KIND) {

            if (mIsName) { // local name is given
                final String localname =
                    getTransaction().nameForKey(getTransaction().getNode().getNameKey()).replaceFirst(".*:",
                        "");
                final int localnameKey = getTransaction().keyForName(localname);

                return localnameKey == mKnownPartKey;
            } else { // namespace prefix is given
                final int nsCount = ((ElementNode)getTransaction().getNode()).getNamespaceCount();
                for (int i = 0; i < nsCount; i++) {
                    getTransaction().moveToNamespace(i);
                    final int prefixKey = mKnownPartKey;
                    if (getTransaction().getNode().getNameKey() == prefixKey) {
                        getTransaction().moveToParent();
                        return true;
                    }
                    getTransaction().moveToParent();
                }
            }

        } else if (getTransaction().getNode().getKind() == ENodes.ATTRIBUTE_KIND) {
            // supporting attributes here is difficult, because treetank
            // does not provide a way to acces the name and namespace of
            // the current attribute (attribute index is not known here)
            throw new IllegalStateException("Wildcards are not supported in attribute names yet.");
        }

        return false;

    }
}
