/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
import java.util.concurrent.LinkedBlockingQueue;

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.nodelayer.SessionConfiguration;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.SoftHashMap;

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

  /** Non-shrinking PageReader pool. */
  private final LinkedBlockingQueue<PageReader> mPool;

  /** Session configuration. */
  private final SessionConfiguration mSessionConfiguration;

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws Exception of any kind.
   */
  public PageCache(final SessionConfiguration sessionConfiguration)
      throws Exception {
    mCache = new SoftHashMap<Long, IPage>();
    mPool = new LinkedBlockingQueue<PageReader>(32);
    mSessionConfiguration = sessionConfiguration;

    for (int i = 0; i < 32; i++) {
      mPool.put(new PageReader(mSessionConfiguration));
    }
  }

  /**
   * Add a new page to the cache.
   * 
   * @param pageReference Page reference pointing to added page.
   */
  public final void put(final PageReference pageReference) {
    mCache.put(pageReference.getStart(), pageReference.getPage());
    pageReference.setPage(null);
  }

  /**
   * Get a page from the cache. In case the page was not in the cache, it is
   * silently fetched using the provided page reference.
   * 
   * @param pageReference Page reference pointing to added page.
   * @param pageKind Kind of page to instantiate the right IPage instance.
   * @return Page from cache (both for hits and "misses").
   * @throws Exception if the page could not be read.
   */
  public final IPage get(final PageReference pageReference, final int pageKind)
      throws Exception {
    IPage page = (IPage) mCache.get(pageReference.getStart());
    if (page == null) {

      // Get page reader from mPool.
      PageReader reader = mPool.take();

      // Deserialize page.
      final FastByteArrayReader in = reader.read(pageReference);
      switch (pageKind) {
      case IConstants.REVISION_ROOT_PAGE:
        page = RevisionRootPage.read(this, in);
        break;
      case IConstants.NAME_PAGE:
        page = NamePage.read(this, in);
        break;
      case IConstants.NODE_PAGE:
        page = NodePage.read(in);
        break;
      case IConstants.UBER_PAGE:
        page = UberPage.read(this, in);
        break;
      case IConstants.INDIRECT_PAGE:
        page = IndirectPage.read(this, in);
        break;
      default:
        throw new IllegalStateException("Unknown page kind.");
      }
      mCache.put(pageReference.getStart(), page);

      // Give page reader back to mPool.
      mPool.put(reader);

    }
    return page;
  }

  /**
   * Safely dereference page.
   * 
   * @param reference Reference to dereference.
   * @param kind Kind of dereferenced page.
   * @return Dereferenced page.
   * @throws Exception of any kind.
   */
  public final IPage dereference(final PageReference reference, final int kind)
      throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return reference.getPage();
    } else {
      // Return committed referenced page.
      return get(reference, kind);
    }
  }

}
