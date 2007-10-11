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

import org.treetank.api.IPage;
import org.treetank.api.IReadTransactionState;
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

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws Exception of any kind.
   */
  public PageCache() throws Exception {
    mCache = new SoftHashMap<Long, IPage>();
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

  public final NodePage dereferenceNodePage(
      final IReadTransactionState state,
      final PageReference reference,
      final long nodePageKey) throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return (NodePage) reference.getPage();
    } else {
      // Return committed referenced page.
      NodePage page = (NodePage) mCache.get(reference.getStart());
      if (page == null) {
        final FastByteArrayReader in = state.getPageReader().read(reference);
        page = NodePage.read(in, nodePageKey);
        mCache.put(reference.getStart(), page);
      }
      return page;
    }
  }

  public final NamePage dereferenceNamePage(
      final IReadTransactionState state,
      final PageReference reference) throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return (NamePage) reference.getPage();
    } else {
      // Return committed referenced page.
      NamePage page = (NamePage) mCache.get(reference.getStart());
      if (page == null) {
        final FastByteArrayReader in = state.getPageReader().read(reference);
        page = NamePage.read(in);
        mCache.put(reference.getStart(), page);
      }
      return page;
    }
  }

  public final IndirectPage dereferenceIndirectPage(
      final IReadTransactionState state,
      final PageReference reference) throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return (IndirectPage) reference.getPage();
    } else {
      // Return committed referenced page.
      IndirectPage page = (IndirectPage) mCache.get(reference.getStart());
      if (page == null) {
        final FastByteArrayReader in = state.getPageReader().read(reference);
        page = IndirectPage.read(in);
        mCache.put(reference.getStart(), page);
      }
      return page;
    }
  }

  public final RevisionRootPage dereferenceRevisionRootPage(
      final IReadTransactionState state,
      final PageReference reference,
      final long revisionKey) throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return (RevisionRootPage) reference.getPage();
    } else {
      // Return committed referenced page.
      RevisionRootPage page =
          (RevisionRootPage) mCache.get(reference.getStart());
      if (page == null) {
        final FastByteArrayReader in = state.getPageReader().read(reference);
        page = RevisionRootPage.read(in, revisionKey);
        mCache.put(reference.getStart(), page);
      }
      return page;
    }
  }

  public final UberPage dereferenceUberPage(
      final IReadTransactionState state,
      final PageReference reference) throws Exception {
    if (reference.isInstantiated()) {
      // Return uncommitted referenced page if there is one.
      return (UberPage) reference.getPage();
    } else {
      // Return committed referenced page.
      UberPage page = (UberPage) mCache.get(reference.getStart());
      if (page == null) {
        final FastByteArrayReader in = state.getPageReader().read(reference);
        page = UberPage.read(state, in);
        mCache.put(reference.getStart(), page);
      }
      return page;
    }
  }

}
