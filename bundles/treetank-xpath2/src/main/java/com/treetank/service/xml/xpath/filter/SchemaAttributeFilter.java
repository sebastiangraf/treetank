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

package com.treetank.service.xml.xpath.filter;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsFilter;

/**
 * <h1>SchemaAttributeFilter</h1>
 * <p>
 * A SchemaAttributeTest matches an attribute node against a corresponding attribute declaration found in the
 * in-scope attribute declarations.
 * </p>
 * <p>
 * A SchemaAttributeTest matches a candidate attribute node if both of the following conditions are satisfied:
 * </p>
 * <p>
 * <li>1. The name of the candidate node matches the specified AttributeName.</li>
 * <li>2. derives-from(AT, ET) is true, where AT is the type annotation of the candidate node and ET is the
 * schema type declared for attribute AttributeName in the in-scope attribute declarations.</li>
 * </p>
 * <p>
 * If the AttributeName specified in the SchemaAttributeTest is not found in the in-scope attribute
 * declarations, a static error is raised [err:XPST0008].
 * </p>
 */
public class SchemaAttributeFilter extends AbsFilter implements IFilter {

    // /** The specified name for the attribute. */
    // private final String attributeName;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction this filter is bound to.
     */
    public SchemaAttributeFilter(final IReadTransaction rtx) {

        super(rtx);
        // attributeName = declaration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized boolean filter() {

        return false;

        // TODO: The result is only false, because support for schema
        // information is
        // not implemented in treetank yet. As soon as this situation changes it
        // is
        // necessary to change this method according to the following pseudo
        // code:
        //
        // if (attributeName is NOT in in-scope-declaration) {
        // throw new XPathError(ErrorType.XPST0008);
        // } else {
        // Type specifiedType = type of the attribute specified in the
        // declaration
        // return getTransaction().isAttributeKind()
        // && getTransaction().getName().equals(attributeName)
        // && (getTransaction().getValueTypeAsType() == specifiedType
        // getTransaction.getValueTypeAsType().
        // derivesFrom(specifiedDeclaration) );
        // }
        //
        // See W3C's XPath 2.0 specification for more details

    }


}
