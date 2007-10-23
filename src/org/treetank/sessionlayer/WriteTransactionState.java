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

import java.io.RandomAccessFile;
import java.util.Map;

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
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
public final class WriteTransactionState extends ReadTransactionState {

  private final PageWriter mPageWriter;

  /**
   * Standard constructor.
   * 
   * @param sessionConfiguration Configuration of session.
   * @param pageCache Shared page cache.
   * @param uberPage Root of revision.
   */
  public WriteTransactionState(
      final SessionConfiguration sessionConfiguration,
      final Map<Long, IPage> pageCache,
      final UberPage uberPage) {
    super(sessionConfiguration, pageCache, uberPage, uberPage
        .getLastCommittedRevisionKey());
    mPageWriter = new PageWriter(sessionConfiguration);
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
  public final Node prepareNode(final long nodeKey) throws Exception {
    return prepareNodePage(nodePageKey(nodeKey)).getNode(
        nodePageOffset(nodeKey));
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
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);

    return node;
  }

  /**
   * {@inheritDoc}
   */
  public final void removeNode(final long nodeKey) throws Exception {
    getRevisionRootPage().decrementNodeCount();
    prepareNodePage(nodePageKey(nodeKey))
        .setNode(nodePageOffset(nodeKey), null);
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
  public final void commit(final PageReference reference) throws Exception {
    if (reference != null && reference.isInstantiated() && reference.isDirty()) {

      // Recursively commit indirectely referenced pages and then write self.
      reference.getPage().commit(this);
      mPageWriter.write(reference);

      // Make sure the reference tree does not grow beyond memory.
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

  public final UberPage commit(final SessionConfiguration sessionConfiguration)
      throws Exception {
    final PageReference uberPageReference = new PageReference();
    final UberPage uberPage = getUberPage();
    final RandomAccessFile file =
        new RandomAccessFile(sessionConfiguration.getPath(), "rw");

    if (uberPage.isBootstrap()) {
      file.setLength(IConstants.BEACON_LENGTH);
    }

    // Recursively write indirectely referenced pages.
    uberPage.commit(this);

    uberPageReference.setPage(uberPage);
    mPageWriter.write(uberPageReference);
    getPageCache().put(
        uberPageReference.getStart(),
        uberPageReference.getPage());
    uberPageReference.setPage(null);

    // Write secondary beacon.
    file.seek(file.length());
    file.writeLong(uberPageReference.getStart());
    file.writeInt(uberPageReference.getLength());
    file.writeLong(uberPageReference.getChecksum());

    // Write primary beacon.
    file.seek(0L);
    file.writeLong(uberPageReference.getStart());
    file.writeInt(uberPageReference.getLength());
    file.writeLong(uberPageReference.getChecksum());

    file.close();

    return uberPage;
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    mPageWriter.close();
    super.close();
  }

  protected final NamePage prepareNamePage(final PageReference reference) {

    NamePage page = (NamePage) reference.getPage();

    if (!reference.isInstantiated()) {
      if (reference.isCommitted()) {
        page = NamePage.clone(dereferenceNamePage(reference));
        reference.setPage(page);
      } else {
        page = NamePage.create();
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = NamePage.clone(page);
        reference.setPage(page);
      }
    }

    return page;
  }

  protected final IndirectPage prepareIndirectPage(final PageReference reference) {

    IndirectPage page = (IndirectPage) reference.getPage();

    if (!reference.isInstantiated()) {
      if (reference.isCommitted()) {
        page = IndirectPage.clone(dereferenceIndirectPage(reference));
        reference.setPage(page);
      } else {
        page = IndirectPage.create();
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = IndirectPage.clone(page);
        reference.setPage(page);
      }
    }

    return page;

  }

  protected final NodePage prepareNodePage(final long nodePageKey) {

    // Indirect reference.
    PageReference reference =
        prepareLeafOfTree(
            getRevisionRootPage().getIndirectPageReference(),
            nodePageKey);

    // Last level points to node page.
    NodePage page = (NodePage) reference.getPage();

    if (!reference.isInstantiated()) {
      if (reference.isCommitted()) {
        page = NodePage.clone(dereferenceNodePage(reference, nodePageKey));
        reference.setPage(page);
      } else {
        page = NodePage.create(nodePageKey);
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = NodePage.clone(page);
        reference.setPage(page);
      }
    }

    // Cache node page.
    setNodePage(page);

    return page;
  }

  protected final RevisionRootPage prepareRevisionRootPage() {

    if (getUberPage().isBootstrap()) {
      return getRevisionRootPage();
    }

    // Prepare revision root page.
    final RevisionRootPage revisionRootPage =
        RevisionRootPage.clone(getRevisionRootPage(getUberPage()
            .getLastCommittedRevisionKey()));

    // Prepare indirect tree to hold reference to prepared revision root page.
    final PageReference revisionRootPageReference =
        prepareLeafOfTree(
            getUberPage().getIndirectPageReference(),
            getUberPage().getRevisionKey());

    // Link the prepared revision root page with the prepared indirect tree.
    revisionRootPageReference.setPage(revisionRootPage);

    // Return prepared revision root page.
    return revisionRootPage;
  }

  protected final PageReference prepareLeafOfTree(
      final PageReference startReference,
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
      reference = prepareIndirectPage(reference).getPageReference(offset);
    }

    // Return reference to leaf of indirect tree.
    return reference;
  }

}
