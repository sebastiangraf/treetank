/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.sessionlayer;

import java.util.Map;

import org.treetank.api.IConstants;
import org.treetank.nodelayer.AbstractNode;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.FastByteArrayReader;

/**
 * <h1>ReadTransactionState</h1>
 * 
 * <p>
 * State of a reading transaction. The only thing shared amongst transactions
 * is the page cache. Everything else is exclusive to this transaction. It
 * is required that only a single thread has access to this transaction.
 * </p>
 * 
 * <p>
 * A path-like cache boosts sequential operations.
 * </p>
 */
public class ReadTransactionState {

  /** Session configuration. */
  private SessionConfiguration mSessionConfiguration;

  /** Shared page cache mapping start address of page to IPage. */
  private Map<Long, AbstractPage> mPageCache;

  /** Page reader exclusively assigned to this transaction. */
  private PageReader mPageReader;

  /** Uber page this transaction is bound to. */
  private UberPage mUberPage;

  /** Revision root page as root of this transaction. */
  private RevisionRootPage mRevisionRootPage;

  /** Cached least recently touched node page. */
  private NodePage mNodePage;

  /** Cached name page of this revision. */
  private NamePage mNamePage;

  /**
   * Standard constructor.
   * 
   * @param sessionConfiguration Configuration of session.
   * @param pageCache Shared page cache.
   * @param uberPage Uber page to start reading with.
   * @param revisionKey Key of revision to read from uber page.
   */
  protected ReadTransactionState(
      final SessionConfiguration sessionConfiguration,
      final Map<Long, AbstractPage> pageCache,
      final UberPage uberPage,
      final long revisionKey) {
    mSessionConfiguration = sessionConfiguration;
    mPageCache = pageCache;
    mPageReader = new PageReader(sessionConfiguration);
    mUberPage = uberPage;
    mRevisionRootPage = getRevisionRootPage(revisionKey);
    mNodePage = null;
    mNamePage = null;
  }

  /**
   * {@inheritDoc}
   */
  protected final RevisionRootPage getRevisionRootPage() {
    return mRevisionRootPage;
  }

  /**
   * {@inheritDoc}
   */
  protected final AbstractNode getNode(final long nodeKey) {

    // Calculate page and node part for given nodeKey.
    final long nodePageKey = nodePageKey(nodeKey);
    final int nodePageOffset = nodePageOffset(nodeKey);

    // Fetch node page if it is not yet in the state cache.
    if (mNodePage == null || mNodePage.getNodePageKey() != nodePageKey) {
      mNodePage =
          dereferenceNodePage(dereferenceLeafOfTree(mRevisionRootPage
              .getIndirectPageReference(), nodePageKey), nodePageKey);
    }

    // Fetch node from node page.
    return mNodePage.getNode(nodePageOffset);
  }

  /**
   * {@inheritDoc}
   */
  protected final String getName(final int nameKey) {
    if (mNamePage == null) {
      mNamePage = dereferenceNamePage(mRevisionRootPage.getNamePageReference());
    }
    return mNamePage.getName(nameKey);
  }

  /**
   * {@inheritDoc}
   */
  protected void close() {
    mPageReader.close();

    // Immediately release all references.
    mSessionConfiguration = null;
    mPageCache = null;
    mPageReader = null;
    mUberPage = null;
    mRevisionRootPage = null;
    mNodePage = null;
    mNamePage = null;
  }

  /**
   * Get revision root page belonging to revision key.
   * 
   * @param revisionKey Key of revision to find revision root page for.
   * @return Revision root page of this revision key.
   */
  protected final RevisionRootPage getRevisionRootPage(final long revisionKey) {

    // Get revision root page which is the leaf of the indirect tree.
    return dereferenceRevisionRootPage(dereferenceLeafOfTree(mUberPage
        .getIndirectPageReference(), revisionKey), revisionKey);

  }

  /**
   * @return The session configuration.
   */
  protected final SessionConfiguration getSessionConfiguration() {
    return mSessionConfiguration;
  }

  /**
   * @return The page cache.
   */
  protected final Map<Long, AbstractPage> getPageCache() {
    return mPageCache;
  }

  /**
   * @param nodePage The node page to set.
   */
  protected final void setNodePage(final NodePage nodePage) {
    mNodePage = nodePage;
  }

  /**
   * @param namePage The name page to set.
   */
  protected final void setNamePage(final NamePage namePage) {
    mNamePage = namePage;
  }

  /**
   * @return The name page.
   */
  protected final NamePage getNamePage() {
    return mNamePage;
  }

  /**
   * @return The uber page.
   */
  protected final UberPage getUberPage() {
    return mUberPage;
  }

  /**
   * @param revisionRootPage The revision root page to set.
   */
  protected final void setRevisionRootPage(
      final RevisionRootPage revisionRootPage) {
    mRevisionRootPage = revisionRootPage;
  }

  /**
   * Calculate node page key from a given node key.
   * 
   * @param nodeKey Node key to find node page key for.
   * @return Node page key.
   */
  protected final long nodePageKey(final long nodeKey) {
    return nodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT;
  }

  /**
   * Calculate node page offset for a given node key.
   * 
   * @param nodeKey Node key to find offset for.
   * @return Offset into node page.
   */
  protected final int nodePageOffset(final long nodeKey) {
    return (int) (nodeKey - ((nodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT) << IConstants.NDP_NODE_COUNT_EXPONENT));
  }

  /**
   * Dereference node page reference.
   * 
   * @param reference Reference to dereference.
   * @param nodePageKey Key of node page.
   * @return Dereferenced page.
   */
  protected final NodePage dereferenceNodePage(
      final PageReference<NodePage> reference,
      final long nodePageKey) {

    // Get page that was dereferenced or prepared earlier.
    NodePage page = reference.getPage();

    // If there is no page, get it from the cache.
    if (page == null) {
      page = (NodePage) mPageCache.get(reference.getStart());
    }

    // If there is no page, get it from the storage and cache it.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = new NodePage(in, nodePageKey);
      mPageCache.put(reference.getStart(), page);
    }

    return page;

  }

  /**
   * Dereference name page reference.
   * 
   * @param reference Reference to dereference.
   * @return Dereferenced page.
   */
  protected final NamePage dereferenceNamePage(
      final PageReference<NamePage> reference) {

    // Get page that was dereferenced or prepared earlier.
    NamePage page = reference.getPage();

    // If there is no page, get it from the cache.
    if (page == null) {
      page = (NamePage) mPageCache.get(reference.getStart());
    }

    // If there is no page, get it from the storage and cache it.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = new NamePage(in);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * Dereference indirect page reference.
   * 
   * @param reference Reference to dereference.
   * @return Dereferenced page.
   */
  protected final IndirectPage dereferenceIndirectPage(
      final PageReference<IndirectPage> reference) {

    // Get page that was dereferenced or prepared earlier.
    IndirectPage page = reference.getPage();

    // If there is no page, get it from the cache.
    if (page == null) {
      page = (IndirectPage) mPageCache.get(reference.getStart());
    }

    // If there is no page, get it from the storage and cache it.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = new IndirectPage(in);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * Dereference revision root page reference.
   * 
   * @param reference Reference to dereference.
   * @param revisionKey Key of revision.
   * @return Dereferenced page.
   */
  protected final RevisionRootPage dereferenceRevisionRootPage(
      final PageReference<RevisionRootPage> reference,
      final long revisionKey) {

    // Get page that was dereferenced or prepared earlier.
    RevisionRootPage page = reference.getPage();

    // If there is no page, get it from the cache.
    if (page == null) {
      page = (RevisionRootPage) mPageCache.get(reference.getStart());
    }

    // If there is no page, get it from the storage and cache it.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = new RevisionRootPage(in, revisionKey);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * Find reference pointing to leaf page of an indirect tree.
   * 
   * @param startReference Start reference pointing to the indirect tree.
   * @param key Key to look up in the indirect tree.
   * @return Reference denoted by key pointing to the leaf page.
   */
  protected final PageReference dereferenceLeafOfTree(
      final PageReference<IndirectPage> startReference,
      final long key) {

    // Initial state pointing to the indirect page of level 0.
    PageReference reference = startReference;
    int offset = 0;
    long levelKey = key;

    // Iterate through all levels.
    for (int level = 0, height =
        IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
      offset =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
      levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
      reference = dereferenceIndirectPage(reference).getReference(offset);
    }

    // Return reference to leaf of indirect tree.
    return reference;
  }

}
