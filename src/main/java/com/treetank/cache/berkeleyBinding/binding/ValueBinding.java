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
import com.treetank.cache.berkeleyBinding.Value;
import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;

/**
 * * Binding for storing {@link Value} in the berkeley db. Since the
 * {@link Value} on a concrete {@link AbstractPage} this class has to
 * dereference related to the concrete instance of the {@link AbstractPage}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ValueBinding extends TupleBinding<Value> {

	/**
	 * Binding to reference/dereference {@link IndirectPage}s
	 */
	private static final IndirectPageBinding indirectBinding = new IndirectPageBinding();
	
	/**
	 * Binding to reference/dereference {@link NamePage}s
	 */
	private static final NamePageBinding nameBinding = new NamePageBinding();
	
	
	/**
	 * Binding to reference/dereference {@link NodePage}s
	 */
	private static final NodePageBinding nodeBinding = new NodePageBinding();
	
	/**
	 * Binding to reference/dereference {@link RevisionRootPage}s
	 */
	private static final RevisionRootPageBinding revisionBinding = new RevisionRootPageBinding();
	
	/**
	 * Binding to reference/dereference {@link UberPage}s
	 */
	private static final UberPageBinding uberBinding = new UberPageBinding();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value entryToObject(TupleInput input) {
		final int kind = input.readInt();
		AbstractPage page;
		switch (kind) {
		case Value.INDIRECT:
			page = new IndirectPage(input);
			break;
		case Value.NAME:
			page = new NamePage(input);
			break;
		case Value.NODE:
			page = new NodePage(input);
			break;
		case Value.REVISION:
			page = new RevisionRootPage(input);
			break;
		case Value.UBER:
			page = new UberPage(input);
			break;
		default:
			throw new RuntimeException();
		}
		return new Value(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectToEntry(Value arg0, TupleOutput arg1) {
		final int kind = arg0.getKind();
		arg1.writeInt(kind);

		final AbstractPage page = arg0.getPage();
		switch (kind) {
		case Value.INDIRECT:
			indirectBinding.objectToEntry((IndirectPage) page, arg1);
			break;
		case Value.NAME:
			nameBinding.objectToEntry((NamePage) page, arg1);
			break;
		case Value.NODE:
			nodeBinding.objectToEntry((NodePage) page, arg1);
			break;
		case Value.REVISION:
			revisionBinding.objectToEntry((RevisionRootPage) page, arg1);
			break;
		case Value.UBER:
			uberBinding.objectToEntry((UberPage) page, arg1);
			break;
		default:
			throw new RuntimeException();
		}

	}

}
