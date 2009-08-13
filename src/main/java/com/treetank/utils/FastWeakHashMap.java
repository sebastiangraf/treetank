/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: FastWeakHashMap.java 4470 2008-09-06 15:24:52Z kramis $
 */

package com.treetank.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1>WeakHashMap</h1>
 * 
 * <p>
 * Based on the SoftHashMap implemented by Dr. Heinz Kabutz.
 * </p>
 * 
 * <p>
 * Hash map based on weak references.
 * </p>
 * 
 * <p>
 * Note that the put and remove methods always return null.
 * </p>
 * 
 * @param <K>
 *            Key object of type K.
 * @param <V>
 *            Value object of type V.
 */
@SuppressWarnings("unchecked")
public final class FastWeakHashMap<K, V> extends AbstractMap<K, V> {

	/** The internal HashMap that will hold the WeakReference. */
	private final Map<K, WeakReference<V>> mInternalMap;

	/** Reference queue for cleared WeakReference objects. */
	private final ReferenceQueue mQueue;

	/**
	 * Default constructor internally using 32 strong references.
	 * 
	 */
	public FastWeakHashMap() {
		mInternalMap = new ConcurrentHashMap();
		mQueue = new ReferenceQueue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V get(final Object key) {
		V value = null;
		final WeakReference<V> weakReference = mInternalMap.get(key);
		if (weakReference != null) {
			// Weak reference was garbage collected.
			value = weakReference.get();
			if (value == null) {
				// Reflect garbage collected weak reference in internal hash
				// map.
				mInternalMap.remove(key);
			}
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V put(final K key, final V value) {
		processQueue();
		mInternalMap.put(key, new WeakValue<V>(value, key, mQueue));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final V remove(final Object key) {
		processQueue();
		mInternalMap.remove(key);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized void clear() {
		processQueue();
		mInternalMap.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		processQueue();
		return mInternalMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove garbage collected weak values with the help of the reference
	 * queue.
	 * 
	 */
	private final void processQueue() {
		WeakValue<V> weakValue;
		while ((weakValue = (WeakValue<V>) mQueue.poll()) != null) {
			mInternalMap.remove(weakValue.key);
		}
	}

	/**
	 * Internal subclass to store keys and values for more convenient lookups.
	 */
	@SuppressWarnings("hiding")
	private final class WeakValue<V> extends WeakReference<V> {
		private final K key;

		/**
		 * Constructor.
		 * 
		 * @param initValue
		 *            Value wrapped as weak reference.
		 * @param initKey
		 *            Key for given value.
		 * @param initReferenceQueue
		 *            Reference queue for cleanup.
		 */
		private WeakValue(final V initValue, final K initKey,
				final ReferenceQueue initReferenceQueue) {
			super(initValue, initReferenceQueue);
			key = initKey;
		}
	}

}
