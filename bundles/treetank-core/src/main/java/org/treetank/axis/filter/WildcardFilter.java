/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.axis.filter;

import org.treetank.api.IReadTransaction;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;

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
