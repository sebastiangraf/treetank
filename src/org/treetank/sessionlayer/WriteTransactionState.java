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
import org.treetank.api.IPage;
import org.treetank.api.IWriteTransactionState;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.pagelayer.UberPage;

/**
 * <h1>WriteTransactionState</h1>
 * 
 * <p>
 * See {@link ReadTransactionState}.
 * </p>
 */
public final class WriteTransactionState extends ReadTransactionState
    implements
    IWriteTransactionState {

  private final PageWriter mPageWriter;

  /**
   * Standard constructor.
   * 
   * @param pageCache Shared page cache.
   * @param pageReader Exclusive page reader.
   * @param pageWriter Exclusive page writer.
   * @param revisionRootPage Root of revision.
   */
  public WriteTransactionState(
      final Map<Long, IPage> pageCache,
      final PageReader pageReader,
      final PageWriter pageWriter,
      final UberPage uberPage) throws Exception {
    super(pageCache, pageReader, uberPage, uberPage.getRevisionCount());
    mPageWriter = pageWriter;
    setRevisionRootPage(prepareRevisionRootPage());
  }

  /**
   * {@inheritDoc}
   */
  public final PageWriter getPageWriter() {
    return mPageWriter;
  }

  /**
   * {@inheritDoc}
   */
  public final NodePage prepareNodePage(final long nodePageKey)
      throws Exception {

    // Indirect reference.
    PageReference reference = getRevisionRootPage().getIndirectPageReference();

    // Remaining levels.
    int levelSteps = 0;
    long levelKey = nodePageKey;
    for (int i = 0; i < IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i++) {

      // Calculate offset of current level.
      levelSteps =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

      // Fetch page from current level.
      setIndirectOffset(i, levelSteps);
      setIndirectPage(i, prepareIndirectPage(reference));
      reference = getIndirectPage(i).getPageReference(levelSteps);
    }

    // Last level points to node page.
    NodePage page = (NodePage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page = NodePage.clone(dereferenceNodePage(reference, nodePageKey));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = NodePage.create(nodePageKey);
      reference.setPage(page);
    }

    // Cache node page.
    setNodePage(page);

    return page;
  }

  /**
   * {@inheritDoc}
   */
  public final Node prepareNode(final long nodeKey) throws Exception {
    return prepareNodePage(Node.nodePageKey(nodeKey)).getNode(
        Node.nodePageOffset(nodeKey));
  }

  public final Node createNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int kind,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) throws Exception {

    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();

    final Node node =
        new Node(
            getRevisionRootPage().getMaxNodeKey(),
            parentKey,
            firstChildKey,
            leftSiblingKey,
            rightSiblingKey,
            kind,
            localPartKey,
            uriKey,
            prefixKey,
            value);

    // Write node into node page.
    prepareNodePage(Node.nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(
            Node.nodePageOffset(getRevisionRootPage().getMaxNodeKey()),
            node);

    return node;
  }

  /**
   * {@inheritDoc}
   */
  public final void removeNode(final long nodeKey) throws Exception {
    getRevisionRootPage().decrementNodeCount();
    prepareNodePage(Node.nodePageKey(nodeKey)).setNode(
        Node.nodePageOffset(nodeKey),
        null);
  }

  /**
   * {@inheritDoc}
   */
  public final int createNameKey(final String name) throws Exception {
    final String string = (name == null ? "" : name);
    final int nameKey = string.hashCode();
    if (getName(nameKey) == null) {
      setNamePage(prepareNamePage(getRevisionRootPage().getNamePageReference()));
      getNamePage().setName(nameKey, string);
    }
    return nameKey;
  }

  /**
   * {@inheritDoc}
   */
  protected final NamePage prepareNamePage(final PageReference reference)
      throws Exception {

    NamePage page = (NamePage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page = NamePage.clone(dereferenceNamePage(reference));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = NamePage.create();
      reference.setPage(page);
    }

    return page;

  }

  /**
   * {@inheritDoc}
   */
  public final IndirectPage prepareIndirectPage(final PageReference reference)
      throws Exception {

    IndirectPage page = (IndirectPage) reference.getPage();

    // Load page if it is already existing in a committed revision.
    if (reference.isCommitted() && !reference.isInstantiated()) {
      page = IndirectPage.clone(dereferenceIndirectPage(reference));
      reference.setPage(page);
    }

    // Assert page is properly instantiated.
    if (!reference.isInstantiated()) {
      page = IndirectPage.create();
      reference.setPage(page);
    }

    return page;

  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageReference reference) throws Exception {
    if (reference.isInstantiated() && reference.isDirty()) {

      // Recursively write indirectely referenced pages.
      reference.getPage().commit(this);

      mPageWriter.write(reference);
      getPageCache().put(reference.getStart(), reference.getPage());
      reference.setPage(null);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageReference[] references) throws Exception {
    for (int i = 0, l = references.length; i < l; i++) {
      commit(references[i]);
    }
  }

  public final RevisionRootPage prepareRevisionRootPage() throws Exception {

    // Indirect reference.
    PageReference reference = getUberPage().getIndirectPageReference();

    // Remaining levels.
    int levelSteps = 0;
    long levelKey = getUberPage().getRevisionCount() + 1;
    for (int i = 0; i < IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i++) {

      // Calculate offset of current level.
      levelSteps =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

      // Fetch page from current level.
      reference = prepareIndirectPage(reference).getPageReference(levelSteps);
    }

    RevisionRootPage rrp =
        getRevisionRootPage(getUberPage().getRevisionCount());
    rrp = RevisionRootPage.clone(rrp);
    reference.setPage(rrp);

    return rrp;

  }

}
