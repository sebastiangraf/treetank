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

import org.apache.log4j.Logger;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastObjectStack;
import org.treetank.utils.IConstants;
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

  /** Logger. */
  private static final Logger LOGGER = Logger.getLogger(PageCache.class);

  /** Page cache mapping start address of page to IPage. */
  private final Map<Long, IPage> mCache;

  /** Non-shrinking PageReader pool. */
  private final FastObjectStack mPool;

  /** Path of TreeTank file. */
  private final String mPath;

  /**
   * Constructor.
   * 
   * @param path Path to TreeTank file.
   * @throws Exception
   */
  public PageCache(final String path) {
    mCache = new SoftHashMap<Long, IPage>(IConstants.STRONG_REFERENCE_COUNT);
    mPool = new FastObjectStack();
    mPath = path;
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

      // Logging.
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Miss for start="
            + pageReference.getStart()
            + "; size="
            + pageReference.getSize()
            + "; checksum="
            + pageReference.getChecksum());
      }

      // Get page reader from mPool.
      PageReader reader = null;
      synchronized (mPool) {
        if (mPool.size() > 0) {
          reader = (PageReader) mPool.pop();
        }
      }
      if (reader == null) {
        reader = new PageReader(mPath);
      }

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
      synchronized (mPool) {
        mPool.push(reader);
      }

    } else {
      // Logging.
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Hit for start="
            + pageReference.getStart()
            + "; size="
            + pageReference.getSize()
            + "; checksum="
            + pageReference.getChecksum());
      }
    }
    return page;
  }

}
