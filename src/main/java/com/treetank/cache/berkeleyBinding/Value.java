/*
 * Copyright (c) 2009, Sebastian Graf (Ph.D. Thesis), University of Konstanz
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
 */
package com.treetank.cache.berkeleyBinding;

import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;

/**
 * Value to be stored in the berkeley db. This class makes the correct mapping
 * for the different instances of the {@link AbstractPage}s and references and
 * dereferences them in a correct way.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class Value {

	/**
	 * Kind of {@link IndirectPage}
	 */
	public final static int INDIRECT = 0;

	/**
	 * Kind of {@link NamePage}
	 */
	public final static int NAME = 1;

	/**
	 * Kind of {@link NodePage}
	 */
	public final static int NODE = 2;

	/**
	 * Kind of {@link RevisionRootPage}
	 */
	public final static int REVISION = 3;

	/**
	 * Kind of {@link UberPage}
	 */
	public final static int UBER = 4;

	/**
	 * Current instance to be stored in this container
	 */
	private final AbstractPage page;

	/**
	 * Current kind of the page.
	 */
	private final int kind;

	/**
	 * Simple constructor just getting the page and holding it.
	 * 
	 * @param paramPage
	 *            the {@link AbstractPage} to hold
	 */
	public Value(final AbstractPage paramPage) {
		if (paramPage instanceof IndirectPage) {
			kind = INDIRECT;
		} else if (paramPage instanceof NamePage) {
			kind = NAME;
		} else if (paramPage instanceof NodePage) {
			kind = NODE;
		} else if (paramPage instanceof RevisionRootPage) {
			kind = REVISION;
		} else {
			kind = UBER;
		}

		page = paramPage;
	}

	/**
	 * Getting the page
	 * 
	 * @return the key
	 */
	public AbstractPage getPage() {
		return page;
	}

	/**
	 * Getting the kind.
	 * 
	 * @return the kind
	 */
	public int getKind() {
		return kind;
	}
}
