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
 * $Id: ItemFilter.java 4246 2008-07-08 08:54:09Z scherer $
 */
package com.treetank.service.xml.xpath.filter;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractFilter;

/**
 * <h1>ItemFilter</h1>
 * 
 * <p>
 * Match any item type (nodes and atomic values).
 * </p>
 */
public class ItemFilter extends AbstractFilter implements IFilter {

	/**
	 * Default constructor.
	 * 
	 * @param rtx
	 *            Transaction this filter is bound to.
	 */
	public ItemFilter(final IReadTransaction rtx) {
		super(rtx);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean filter() {
		// everything that is hold by an transaction is either a node or an
		// atomic value, so this yields true for all item kinds
		return true;
	}

}