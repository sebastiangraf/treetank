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
package com.treetank.cache.berkeleyBinding.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.page.IndirectPage;

/**
 * Binding for storing {@link IndirectPage} in the berkeley db.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IndirectPageBinding extends TupleBinding<IndirectPage> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndirectPage entryToObject(TupleInput arg0) {
		return new IndirectPage(arg0);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectToEntry(IndirectPage arg0, TupleOutput arg1) {
		arg0.serialize(arg1);
	}
}