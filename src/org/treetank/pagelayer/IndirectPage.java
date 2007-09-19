/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.pagelayer;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;

final public class IndirectPage extends AbstractPage implements IPage {

  private final PageReference[] mIndirectPageReferences;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  private IndirectPage(final PageCache pageCache) {
    super(pageCache);
    mIndirectPageReferences = new PageReference[IConstants.IP_REFERENCE_COUNT];
  }

  /**
   * Create new uncommitted in-memory uber page.
   * 
   * @param pageCache
   * @return
   * @throws Exception
   */
  public static final IndirectPage create(final PageCache pageCache) {

    final IndirectPage indirectPage = new IndirectPage(pageCache);

    createPageReferences(indirectPage.mIndirectPageReferences);

    return indirectPage;

  }

  /**
   * Read committed uber page from disk.
   * 
   * @param pageCache
   * @param in
   * @param pageKind
   * @throws Exception
   */
  public static final IndirectPage read(
      final PageCache pageCache,
      final FastByteArrayReader in) throws Exception {

    final IndirectPage indirectPage = new IndirectPage(pageCache);

    readPageReferences(indirectPage.mIndirectPageReferences, in);

    return indirectPage;
  }

  public static final IndirectPage clone(
      final IndirectPage committedIndirectPage) {

    final IndirectPage indirectPage =
        new IndirectPage(committedIndirectPage.mPageCache);

    clonePageReferences(
        indirectPage.mIndirectPageReferences,
        committedIndirectPage.mIndirectPageReferences);

    return indirectPage;
  }

  /**
   * Get page by page offset.
   * 
   * @param pageOffset Offset of referenced page.
   * @return INode page with this key.
   * @throws Exception of any kind.
   */
  public final IPage getPage(final int pageOffset, final byte pageKind)
      throws Exception {
    return dereference(mIndirectPageReferences[pageOffset], pageKind);
  }

  /**
   * Get reference by page offset.
   * 
   * @param pageOffset Offset of referenced page.
   * @return Reference at given offset.
   */
  public final PageReference getPageReference(final int pageOffset) {
    return mIndirectPageReferences[pageOffset];
  }

  /**
   * Set page at page offset.
   * 
   * @param pageOffset Offset of referenced page.
   * @param page Page to set at pageOffset.
   */
  public final void setPage(final int pageOffset, final IPage page) {
    mIndirectPageReferences[pageOffset].setPage(page);
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageWriter pageWriter) throws Exception {
    commit(pageWriter, mIndirectPageReferences);
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    serialize(out, mIndirectPageReferences);
  }

}
