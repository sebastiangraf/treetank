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

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;
import org.treetank.utils.StaticTree;

final public class UberPage extends AbstractPage implements IPage {

  private long mMaxRevisionKey;

  private final PageReference[] mRevisionRootPageReferences;

  private final PageReference[] mIndirectRevisionRootPageReferences;

  private RevisionRootPage mCurrentRevisionRootPage;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private UberPage(final PageCache pageCache) {
    super(pageCache);
    mRevisionRootPageReferences =
        new PageReference[IConstants.UBP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT];
    mIndirectRevisionRootPageReferences =
        new PageReference[IConstants.UBP_MAX_REVISION_ROOT_PAGE_INDIRECTION_LEVEL];
  }

  /**
   * Create new uncommitted in-memory uber page.
   * 
   * @param pageCache
   * @return
   * @throws Exception
   */
  public static final UberPage create(final PageCache pageCache)
      throws Exception {

    final UberPage uberPage = new UberPage(pageCache);

    // Make sure that all references are instantiated.
    uberPage.mMaxRevisionKey = IConstants.UBP_INIT_ROOT_REVISION_KEY;
    createPageReferences(uberPage.mRevisionRootPageReferences);

    // Indirect pages (shallow init).
    createPageReferences(uberPage.mIndirectRevisionRootPageReferences);

    // Make sure that the first empty revision root page already exists.
    uberPage.mCurrentRevisionRootPage =
        RevisionRootPage.create(pageCache, IConstants.UBP_ROOT_REVISION_KEY);

    return uberPage;

  }

  /**
   * Read committed uber page from disk.
   * 
   * @param pageCache
   * @param in
   * @throws Exception
   */
  public static final UberPage read(
      final PageCache pageCache,
      final FastByteArrayReader in) throws Exception {

    final UberPage uberPage = new UberPage(pageCache);

    // Deserialize uber page.
    uberPage.mMaxRevisionKey = in.readLong();
    readPageReferences(uberPage.mRevisionRootPageReferences, in);

    // Indirect pages (shallow load without indirect page instances).
    readPageReferences(uberPage.mIndirectRevisionRootPageReferences, in);

    // Make sure latest revision root page is active.
    uberPage.mCurrentRevisionRootPage =
        uberPage.getRevisionRootPage(uberPage.mMaxRevisionKey);

    return uberPage;
  }

  /**
   * COW committed uber page to modify it.
   * 
   * @param committedUberPage
   * @return
   */
  public static final UberPage clone(final UberPage committedUberPage) {

    final UberPage uberPage = new UberPage(committedUberPage.mPageCache);

    // COW uber page.
    uberPage.mMaxRevisionKey = committedUberPage.mMaxRevisionKey;
    clonePageReferences(
        uberPage.mRevisionRootPageReferences,
        committedUberPage.mRevisionRootPageReferences);

    // Indirect pages (shallow COW without page instances).
    clonePageReferences(
        uberPage.mIndirectRevisionRootPageReferences,
        committedUberPage.mIndirectRevisionRootPageReferences);

    uberPage.mCurrentRevisionRootPage =
        committedUberPage.mCurrentRevisionRootPage;

    return uberPage;
  }

  public final long getMaxRevisionKey() {
    return mMaxRevisionKey;
  }

  public final RevisionRootPage getRevisionRootPage(final long revisionKey)
      throws Exception {

    // Calculate number of levels and offsets of these levels.
    final int[] offsets = StaticTree.calcRevisionRootPageOffsets(revisionKey);

    if (offsets.length == 1) {
      // Immediate reference.
      return (RevisionRootPage) dereference(
          mRevisionRootPageReferences[offsets[0]],
          IConstants.REVISION_ROOT_PAGE);
    } else {
      // Indirect reference.
      PageReference reference = mIndirectRevisionRootPageReferences[offsets[0]];
      IPage page = null;

      // Remaining levels.
      for (int i = 1; i < offsets.length; i++) {
        page = dereference(reference, IConstants.INDIRECT_PAGE);
        reference = ((IndirectPage) page).getPageReference(offsets[i]);
      }
      return (RevisionRootPage) dereference(
          reference,
          IConstants.REVISION_ROOT_PAGE);
    }
  }

  public final RevisionRootPage prepareRevisionRootPage() throws Exception {

    // Calculate number of levels and offsets of these levels.
    final int[] offsets = StaticTree.calcRevisionRootPageOffsets(mMaxRevisionKey + 1);

    // Which page reference to COW on immediate level 0?
    mCurrentRevisionRootPage =
        RevisionRootPage.clone(mMaxRevisionKey + 1, mCurrentRevisionRootPage);
    if (offsets.length == 1) {
      // Immediate reference.
      mRevisionRootPageReferences[(int) mMaxRevisionKey + 1]
          .setPage(mCurrentRevisionRootPage);
    } else {
      // Indirect reference.
      PageReference reference = mIndirectRevisionRootPageReferences[offsets[0]];
      IPage page = null;

      //    Remaining levels.
      for (int i = 1; i < offsets.length; i++) {
        page = prepareIndirectPage(reference);
        reference = ((IndirectPage) page).getPageReference(offsets[i]);
      }
      reference.setPage(mCurrentRevisionRootPage);
    }
    return mCurrentRevisionRootPage;

  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageWriter pageWriter) throws Exception {
    commit(pageWriter, mRevisionRootPageReferences);
    commit(pageWriter, mIndirectRevisionRootPageReferences);
    mMaxRevisionKey += 1;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    out.writeLong(mMaxRevisionKey);
    serialize(out, mRevisionRootPageReferences);
    serialize(out, mIndirectRevisionRootPageReferences);
  }

}
