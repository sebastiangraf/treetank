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
package com.treetank.cache;

import java.io.File;

import com.treetank.session.SessionConfiguration;

/**
 * Abstract class for holding all persistence caches. Each instance of this
 * class stores the data in a place related to the {@link SessionConfiguration}
 * at a different subfolder.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractPersistenceCache implements ICache {

	/**
	 * Place to store the data
	 */
	protected final File persistentCachePlace;

	/**
	 * Counter to give every instance a different place
	 */
	private static int counter = 0;

	/**
	 * Constructor with the place to store the data
	 * 
	 * @param paramConfig
	 *            {@link SessionConfiguration} which holds the place to store
	 *            the data.
	 */
	protected AbstractPersistenceCache(final SessionConfiguration paramConfig) {
		persistentCachePlace = new File(paramConfig.getAbsolutePath()
				+ File.separator + "transactionLog" + File.separator + counter);
		persistentCachePlace.mkdirs();
		counter++;
	}

	/**
	 * Clearing the data which means that the log is deleted after usage.
	 */
	public void clear() {
		for (final File files : persistentCachePlace.listFiles()) {
			files.delete();
		}
		persistentCachePlace.delete();
	}

}
