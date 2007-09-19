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

/**
 * <h1>AbstractPage</h1>
 * 
 * <p>
 * A collection of commonly used page tools to reduce code clutter and
 * stability.
 * </p>
 */
public abstract class AbstractPage implements IPage {

  /** Shared read mPageCache to read pages from. */
  protected final PageCache mPageCache;

  /**
   * Constructor to assure minimal common setup.
   * 
   * @param pageCache IPageCache to read from.
   */
  public AbstractPage(final PageCache pageCache) {
    mPageCache = pageCache;
  }

  /**
   * Initialize given page reference with virgin page reference.
   */
  public static final PageReference createPageReference() {
    return new PageReference();
  }

  /**
   * Initialize given page reference array with virgin page references.
   * 
   * @param references Page reference array to initialize.
   */
  public static final void createPageReferences(final PageReference[] references) {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = createPageReference();
    }
  }

  /**
   * Deserialize page reference from reader to page reference.
   * 
   * @param references Page reference array to deserialize.
   * @param in Reader to read from.
   * @throws Exception of any kind.
   */
  public static final PageReference readPageReference(
      final FastByteArrayReader in) throws Exception {
    return new PageReference(in);
  }

  /**
   * Deserialize page references from reader to page reference array.
   * 
   * @param references Page reference array to deserialize.
   * @param in Reader to read from.
   * @throws Exception of any kind.
   */
  public static final void readPageReferences(
      final PageReference[] references,
      final FastByteArrayReader in) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = readPageReference(in);
    }
  }

  /**
   * COW committed page reference to virgin page reference.
   * 
   * @param committedReference Committed page reference to COW.
   */
  public static final PageReference clonePageReference(
      final PageReference committedReference) {
    return new PageReference(committedReference);
  }

  /**
   * COW committed page reference array to virgin page reference array.
   * 
   * @param references Virgin page reference array.
   * @param committedReferences Committed page reference array to COW.
   */
  public static final void clonePageReferences(
      final PageReference[] references,
      final PageReference[] committedReferences) {
    for (int i = 0, l = references.length; i < l; i++) {
      references[i] = clonePageReference(committedReferences[i]);
    }
  }

  /**
   * COW revision root page or instantiate virgin one from given revision
   * root page reference.
   * 
   * @param reference Revision root page reference.
   * @param revisionKey Key of revision root page in case it must be created.
   * @return COWed revision root page.
   * @throws Exception of any kind.
   */
  public final RevisionRootPage prepareRevisionRootPage(
      final PageReference reference,
      final long revisionKey) throws Exception {

    RevisionRootPage page = (RevisionRootPage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page =
          RevisionRootPage.clone(revisionKey, (RevisionRootPage) mPageCache
              .get(reference, IConstants.REVISION_ROOT_PAGE));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = RevisionRootPage.create(mPageCache, revisionKey);
      reference.setPage(page);
    }

    return page;

  }

  /**
   * COW name page or instantiate virgin one from given name page reference.
   * 
   * @param reference Name page reference.
   * @return COWed name page.
   * @throws Exception of any kind.
   */
  public final NamePage prepareNamePage(final PageReference reference)
      throws Exception {

    NamePage page = (NamePage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page =
          NamePage.clone((NamePage) mPageCache.get(
              reference,
              IConstants.NAME_PAGE));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = NamePage.create(mPageCache);
      reference.setPage(page);
    }

    return page;

  }

  /**
   * COW node page or instantiate virgin one from given node page reference.
   * 
   * @param reference INode page reference.
   * @param nodePageKey Key of node page in case it must be created.
   * @return COWed node page.
   * @throws Exception of any kind.
   */
  public final NodePage prepareNodePage(
      final PageReference reference,
      final long nodePageKey) throws Exception {

    NodePage page = (NodePage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page =
          NodePage.clone((NodePage) mPageCache.get(
              reference,
              IConstants.NODE_PAGE));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = NodePage.create(nodePageKey);
      reference.setPage(page);
    }

    return page;

  }

  /**
   * COW indirect page or instantiate virgin one from given indirect page
   * reference.
   * 
   * @param reference Indirect page reference.
   * @return COWed indirect page.
   * @throws Exception of any kind.
   */
  public final IndirectPage prepareIndirectPage(final PageReference reference)
      throws Exception {

    IndirectPage page = (IndirectPage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page =
          IndirectPage.clone((IndirectPage) mPageCache.get(
              reference,
              IConstants.INDIRECT_PAGE));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = IndirectPage.create(mPageCache);
      reference.setPage(page);
    }

    return page;

  }

  /**
   * Safely commit and serialize dereferenced dirty page.
   * 
   * @param writer PageWriter to write page to.
   * @param reference Reference to dereference and serialize.
   * @throws Exception of any kind.
   */
  public final void commit(
      final PageWriter writer,
      final PageReference reference) throws Exception {
    if (reference.isInstantiated() && reference.isDirty()) {
      writer.write(reference);
      mPageCache.put(reference);
    }
  }

  /**
   * Safely commit and serialize dereferenced dirty page.
   * 
   * @param writer PageWriter to write page to.
   * @param references Reference array to dereference and serialize.
   * @throws Exception of any kind.
   */
  public final void commit(
      final PageWriter writer,
      final PageReference[] references) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      commit(writer, references[i]);
    }
  }

  /**
   * Safely serialize referenced page.
   * 
   * @param out FastByteArrayWriter to write page to.
   * @param reference Reference to dereference and serialize.
   * @throws Exception of any kind.
   */
  public final void serialize(
      final FastByteArrayWriter out,
      final PageReference reference) throws Exception {
    reference.serialize(out);
  }

  /**
   * Safely serialize referenced pages.
   * 
   * @param out FastByteArrayWriter to write page to.
   * @param references Reference array to dereference and serialize.
   * @throws Exception of any kind.
   */
  public final void serialize(
      final FastByteArrayWriter out,
      final PageReference[] references) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      serialize(out, references[i]);
    }
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
      return mPageCache.get(reference, kind);
    }
  }

  /**
   * {@inheritDoc}
   */
  public abstract void commit(PageWriter pageWriter) throws Exception;

  /**
   * {@inheritDoc}
   */
  public abstract void serialize(FastByteArrayWriter out) throws Exception;

}
