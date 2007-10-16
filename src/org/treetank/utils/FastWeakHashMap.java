/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * @param <K> Key object of type K.
 * @param <V> Value object of type V.
 */
public final class FastWeakHashMap<K, V> extends AbstractMap<K, V> {

  /** The internal HashMap that will hold the WeakReference. */
  private final Map<K, SoftReference<V>> mInternalMap;

  /** Reference queue for cleared WeakReference objects. */
  private final ReferenceQueue mQueue;

  /**
   * Default constructor internally using 32 strong references.
   *
   */
  public FastWeakHashMap() {
    mInternalMap = new HashMap<K, SoftReference<V>>();
    mQueue = new ReferenceQueue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final V get(final Object key) {
    V value = null;
    final SoftReference<V> weakReference = mInternalMap.get(key);
    if (weakReference != null) {
      // Weak reference was garbage collected.
      value = weakReference.get();
      if (value == null) {
        // Reflect garbage collected weak reference in internal hash map.
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
    mInternalMap.put(key, new SoftValue<V>(value, key, mQueue));
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
   * Remove garbage collected weak values with the help of the reference queue.
   *
   */
  private final void processQueue() {
    SoftValue<V> weakValue;
    while ((weakValue = (SoftValue) mQueue.poll()) != null) {
      mInternalMap.remove(weakValue.key);
    }
  }

  /**
   * Internal subclass to store keys and values for more convenient lookups.
   */
  private final class SoftValue<V> extends SoftReference<V> {
    private final K key;

    /**
     * Constructor.
     * 
     * @param initValue Value wrapped as weak reference.
     * @param initKey Key for given value.
     * @param initReferenceQueue Reference queue for cleanup.
     */
    private SoftValue(
        final V initValue,
        final K initKey,
        final ReferenceQueue initReferenceQueue) {
      super(initValue, initReferenceQueue);
      key = initKey;
    }
  }

}
