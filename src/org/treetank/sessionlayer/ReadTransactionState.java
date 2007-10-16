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
 * $Id:SessionConfiguration.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.util.Map;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IPage;
import org.treetank.api.IReadTransactionState;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.Node;
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
public class ReadTransactionState implements IReadTransactionState {

  /** Shared page cache mapping start address of page to IPage. */
  private final Map<Long, IPage> mPageCache;

  /** Page reader exclusively assigned to this transaction. */
  private final PageReader mPageReader;

  /** Revision root page as root of this transaction. */
  private RevisionRootPage mRevisionRootPage;

  /** Cached least recently touched node page. */
  private NodePage mNodePage;

  /** Cached name page of this revision. */
  private NamePage mNamePage;

  /** Offsets of indirect tree to locate node page. */
  private int[] mIndirectOffsets;

  /** Pages of indirect tree to locate node page. */
  private IndirectPage[] mIndirectPages;

  /**
   * Standard constructor.
   * 
   * @param pageCache Shared page cache.
   * @param pageReader Exclusive page reader.
   * @param revisionRootPage Root of revision.
   */
  public ReadTransactionState(
      final Map<Long, IPage> pageCache,
      final PageReader pageReader,
      final RevisionRootPage revisionRootPage) {
    mPageCache = pageCache;
    mPageReader = pageReader;
    mRevisionRootPage = revisionRootPage;
    mNodePage = null;
    mNamePage = null;
    mIndirectOffsets = new int[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
    mIndirectPages =
        new IndirectPage[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
    for (int i = 0; i < mIndirectOffsets.length; i++) {
      mIndirectOffsets[i] = -1;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final RevisionRootPage getRevisionRootPage() {
    return mRevisionRootPage;
  }

  /**
   * {@inheritDoc}
   */
  public final INode getNode(final long nodeKey) throws Exception {

    // Calculate coordinates for given nodeKey.
    final long nodePageKey = Node.nodePageKey(nodeKey);
    final int nodePageOffset = Node.nodePageOffset(nodeKey);

    // Fetch node page if required.
    if (mNodePage == null || mNodePage.getNodePageKey() != nodePageKey) {
      // Indirect reference.
      PageReference reference = mRevisionRootPage.getIndirectPageReference();

      // Remaining levels.
      int levelSteps = 0;
      long levelKey = nodePageKey;
      for (int i = 0; i < mIndirectOffsets.length; i++) {

        // Calculate offset of current level.
        levelSteps =
            (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
        levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

        // Fetch page from current level.
        if (levelSteps != mIndirectOffsets[i]) {
          mIndirectOffsets[i] = levelSteps;
          mIndirectPages[i] = dereferenceIndirectPage(reference);
        }
        reference = mIndirectPages[i].getPageReference(levelSteps);
      }

      mNodePage = dereferenceNodePage(reference, nodePageKey);
    }

    // Fetch node from node page.
    return mNodePage.getNode(nodePageOffset);
  }

  /**
   * {@inheritDoc}
   */
  public final String getName(final int nameKey) throws Exception {
    if (mNamePage == null) {
      mNamePage = dereferenceNamePage(mRevisionRootPage.getNamePageReference());
    }
    return mNamePage.getName(nameKey);
  }

  /**
   * {@inheritDoc}
   */
  protected final NodePage dereferenceNodePage(
      final PageReference reference,
      final long nodePageKey) throws Exception {

    // Get uncommitted referenced page if there is one.
    NodePage page = (NodePage) reference.getPage();

    // Get committed referenced page from cache if there is one.
    if (page == null) {
      page = (NodePage) mPageCache.get(reference.getStart());
    }

    // Get committed referenced page from storage.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = NodePage.read(in, nodePageKey);
      mPageCache.put(reference.getStart(), page);
    }

    return page;

  }

  /**
   * {@inheritDoc}
   */
  protected final NamePage dereferenceNamePage(final PageReference reference)
      throws Exception {

    // Get uncommitted referenced page if there is one.
    NamePage page = (NamePage) reference.getPage();

    // Get committed referenced page from cache if there is one.
    if (page == null) {
      page = (NamePage) mPageCache.get(reference.getStart());
    }

    // Get committed referenced page from storage.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = NamePage.read(in);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * {@inheritDoc}
   */
  public final IndirectPage dereferenceIndirectPage(
      final PageReference reference) throws Exception {

    // Get uncommitted referenced page if there is one.
    IndirectPage page = (IndirectPage) reference.getPage();

    // Get committed referenced page from cache if there is one.
    if (page == null) {
      page = (IndirectPage) mPageCache.get(reference.getStart());
    }

    // Get committed referenced page from storage.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = IndirectPage.read(in);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * {@inheritDoc}
   */
  public final RevisionRootPage dereferenceRevisionRootPage(
      final PageReference reference,
      final long revisionKey) throws Exception {

    // Get uncommitted referenced page if there is one.
    RevisionRootPage page = (RevisionRootPage) reference.getPage();

    // Get committed referenced page from cache if there is one.
    if (page == null) {
      page = (RevisionRootPage) mPageCache.get(reference.getStart());
    }

    // Get committed referenced page from storage.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = RevisionRootPage.read(in, revisionKey);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

  /**
   * @return The page cache.
   */
  protected final Map<Long, IPage> getPageCache() {
    return mPageCache;
  }

  /**
   * @param nodePage The node page to set.
   */
  protected final void setNodePage(final NodePage nodePage) {
    mNodePage = nodePage;
  }

  /**
   * @return The node page.
   */
  protected final NodePage getNodePage() {
    return mNodePage;
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
   * @return The indirect offsets.
   */
  protected final int getIndirectOffset(final int index) {
    return mIndirectOffsets[index];
  }

  /**
   * @param indirectOffsets The indirect offsets to set.
   */
  protected final void setIndirectOffset(final int index, final int value) {
    mIndirectOffsets[index] = value;
  }

  /**
   * @return The indirect pages.
   */
  protected final IndirectPage getIndirectPage(final int index) {
    return mIndirectPages[index];
  }

  /**
   * @param indirectPages The indirect pages to set.
   */
  protected final void setIndirectPage(final int index, final IndirectPage page) {
    mIndirectPages[index] = page;
  }

}
