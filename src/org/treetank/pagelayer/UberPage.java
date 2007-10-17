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

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.api.IWriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

final public class UberPage extends AbstractPage implements IPage {

  private long mRevisionCount;

  private PageReference mIndirectPageReference;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private UberPage() {
    mIndirectPageReference = null;
  }

  /**
   * Create new uncommitted in-memory uber page.
   * 
   * @param pageCache
   * @return
   * @throws Exception
   */
  public static final UberPage create() throws Exception {

    // --- Create uber page ----------------------------------------------------

    final UberPage uberPage = new UberPage();

    // Make sure that all references are instantiated.
    uberPage.mRevisionCount = IConstants.UBP_INIT_ROOT_REVISION_KEY;

    // Indirect pages (shallow init).
    uberPage.mIndirectPageReference = createPageReference();

    // --- Create revision tree ------------------------------------------------

    // Initialize revision tree to guarantee that there is a revision root page.
    IndirectPage page = null;
    PageReference reference = uberPage.mIndirectPageReference;

    // Remaining levels.
    for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
      page = IndirectPage.create();
      reference.setPage(page);
      reference = page.getPageReference(0);
    }

    RevisionRootPage rrp = RevisionRootPage.create(uberPage.mRevisionCount);
    reference.setPage(rrp);

    // --- Create node tree ----------------------------------------------------

    // Initialize revision tree to guarantee that there is a revision root page.
    page = null;
    reference = rrp.getIndirectPageReference();

    // Remaining levels.
    for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
      page = IndirectPage.create();
      reference.setPage(page);
      reference = page.getPageReference(0);
    }

    NodePage ndp = NodePage.create(IConstants.ROOT_KEY);
    reference.setPage(ndp);

    ndp.setNode(0, new Node(IConstants.ROOT_KEY));
    
    rrp.incrementNodeCountAndMaxNodeKey();

    return uberPage;

  }

  /**
   * Read committed uber page from disk.
   * 
   * @param pageCache
   * @param in
   * @throws Exception
   */
  public static final UberPage read(final FastByteArrayReader in)
      throws Exception {

    final UberPage uberPage = new UberPage();

    // Deserialize uber page.
    uberPage.mRevisionCount = in.readVarLong();

    // Indirect pages (shallow load without indirect page instances).
    uberPage.mIndirectPageReference = readPageReference(in);

    return uberPage;
  }

  /**
   * COW committed uber page to modify it.
   * 
   * @param committedUberPage
   * @return
   */
  public static final UberPage clone(final UberPage committedUberPage) {

    final UberPage uberPage = new UberPage();

    // COW uber page.
    uberPage.mRevisionCount = committedUberPage.mRevisionCount;

    // Indirect pages (shallow COW without page instances).
    uberPage.mIndirectPageReference =
        clonePageReference(committedUberPage.mIndirectPageReference);

    return uberPage;
  }

  public final PageReference getIndirectPageReference() {
    return mIndirectPageReference;
  }

  public final long getRevisionCount() {
    return mRevisionCount;
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final IWriteTransactionState state) throws Exception {
    state.commit(mIndirectPageReference);
    mRevisionCount += 1;
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    out.writeVarLong(mRevisionCount);
    serialize(out, mIndirectPageReference);
  }

}
