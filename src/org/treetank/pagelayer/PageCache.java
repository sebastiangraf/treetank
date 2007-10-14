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

package org.treetank.pagelayer;

import java.util.Map;

import org.treetank.api.IPage;
import org.treetank.api.IReadTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.WeakHashMap;

/**
 * <h1>PageCache</h1>
 * 
 * <p>PageCache maintains a soft-reference-based cache to the pages. It maps
 * the start address, i.e. the address of the first byte of the page to the
 * deserialized IPage instance.
 * </p>
 */
public final class PageCache {

  /** Page cache mapping start address of page to IPage. */
  private final Map<Long, IPage> mCache;

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws Exception of any kind.
   */
  public PageCache() throws Exception {
    mCache = new WeakHashMap<Long, IPage>();
  }

  /**
   * Add a new page to the cache.
   * 
   * @param pageReference Page reference pointing to added page.
   */
  public final void put(final long pageStart, IPage page) {
    mCache.put(pageStart, page);
  }

  public final IPage get(final PageReference pageReference) {
    if (pageReference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return pageReference.getPage();
    } else {
      // Return committed referenced page.
      return mCache.get(pageReference.getStart());
    }
  }

}
