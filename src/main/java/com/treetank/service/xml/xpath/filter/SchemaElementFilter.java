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
 * $Id: SchemaElementFilter.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractFilter;

/**
 * <h1>SchemaElementFilter</h1>
 * <p>
 * A SchemaElementTest matches an element node against a corresponding element
 * declaration found in the in-scope element declarations.
 * </p>
 * <p>
 * A SchemaElementTest matches a candidate element node if both of the following
 * conditions are satisfied:
 * </p>
 * <p>
 * <li>1. The name of the candidate node matches the specified ElementName or
 * matches the name of an element in a substitution group headed by an element
 * named ElementName.</li>
 * <li>derives-from(AT, ET) is true, where AT is the type annotation of the
 * candidate node and ET is the schema type declared for element ElementName in
 * the in-scope element declarations.</li>
 * <li>3. If the element declaration for ElementName in the in-scope element
 * declarations is not nillable, then the nilled property of the candidate node
 * is false.</li>
 * </p>
 * <p>
 * If the ElementName specified in the SchemaElementTest is not found in the
 * in-scope Element declarations, a static error is raised [err:XPST0008].
 * </p>
 */
public class SchemaElementFilter extends AbstractFilter implements IFilter {

    /** The specified name for the element. */
    private final String elementName;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction this filter is bound to.
     * @param declaration
     *            Element declaration in-scope Element declaration to match the
     *            current node against.
     */
    public SchemaElementFilter(final IReadTransaction rtx,
            final String declaration) {

        super(rtx);
        elementName = declaration;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean filter() {

        return false;

        // TODO: The result is only false, because support for schema
        // information is
        // not implemented in treetank yet. As soon as this situation changes it
        // is
        // necessary to change this method according to the following pseudo
        // code:
        //
        // if (ElementName is NOT in in-scope-declaration) {
        // throw new XPathError(ErrorType.XPST0008);
        // } else {
        // Type specifiedType = type of the Element specified in the declaration
        // return getTransaction().isElementKind()
        // && (getTransaction().getName().equals(ElementName)
        // || substitution group matches elementName)
        // && (getTransaction().getValueTypeAsType() == specifiedType
        // || getTransaction.getValueTypeAsType().
        // derivesFrom(specifiedDeclaration)
        // && if (element declaration is not nillable) nilled property of
        // candidate node has to be false);

        // }
        //
        // See W3C's XPath 2.0 specification for more details

    }

}
