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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import org.treetank.api.IConstants;
import org.treetank.nodelayer.AbstractNode;
import org.treetank.nodelayer.DocumentRootNode;
import org.treetank.nodelayer.ElementNode;
import org.treetank.nodelayer.FullTextRootNode;
import org.treetank.nodelayer.TextNode;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
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

  private PageWriter mPageWriter;

  /**
   * Standard constructor.
   * 
   * @param sessionConfiguration Configuration of session.
   * @param pageCache Shared page cache.
   * @param uberPage Root of revision.
   */
  protected WriteTransactionState(
      final SessionConfiguration sessionConfiguration,
      final Map<Long, AbstractPage> pageCache,
      final UberPage uberPage) {
    super(sessionConfiguration, pageCache, uberPage, uberPage
        .getLastCommittedRevisionKey());
    mPageWriter = new PageWriter(sessionConfiguration);
    setRevisionRootPage(prepareRevisionRootPage());
  }

  /**
   * {@inheritDoc}
   */
  protected final PageWriter getPageWriter() {
    return mPageWriter;
  }

  /**
   * {@inheritDoc}
   */
  protected final AbstractNode prepareNode(final long nodeKey) {
    return prepareNodePage(nodePageKey(nodeKey)).getNode(
        nodePageOffset(nodeKey));
  }

  protected final DocumentRootNode createDocumentNode() {

    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();

    final DocumentRootNode node = new DocumentRootNode();

    // Write node into node page.
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);

    return node;
  }

  protected final ElementNode createElementNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int localPartKey,
      final int uriKey,
      final int prefixKey) {

    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();

    final ElementNode node =
        new ElementNode(
            getRevisionRootPage().getMaxNodeKey(),
            parentKey,
            firstChildKey,
            leftSiblingKey,
            rightSiblingKey,
            localPartKey,
            uriKey,
            prefixKey);

    // Write node into node page.
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);

    return node;
  }

  protected final TextNode createTextNode(
      final long parentKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final byte[] value) {

    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();

    final TextNode node =
        new TextNode(
            getRevisionRootPage().getMaxNodeKey(),
            parentKey,
            leftSiblingKey,
            rightSiblingKey,
            value);

    // Write node into node page.
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);

    return node;
  }

  protected final FullTextRootNode createFullTextNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int localPartKey) {

    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();

    final FullTextRootNode node =
        new FullTextRootNode(
            getRevisionRootPage().getMaxNodeKey(),
            parentKey,
            firstChildKey,
            leftSiblingKey,
            rightSiblingKey,
            localPartKey);

    // Write node into node page.
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);

    return node;
  }

  /**
   * {@inheritDoc}
   */
  protected final void removeNode(final long nodeKey) {
    getRevisionRootPage().decrementNodeCount();
    prepareNodePage(nodePageKey(nodeKey))
        .setNode(nodePageOffset(nodeKey), null);
  }

  /**
   * {@inheritDoc}
   */
  protected final int createNameKey(final String name) {
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
  public final void commit(final PageReference reference) throws IOException {
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
  protected final void commit(final PageReference[] references)
      throws IOException {
    for (int i = 0, l = references.length; i < l; i++) {
      commit(references[i]);
    }
  }

  protected final UberPage commit(
      final SessionConfiguration sessionConfiguration) throws IOException {
    final PageReference uberPageReference = new PageReference();
    final UberPage uberPage = getUberPage();
    final RandomAccessFile file =
        new RandomAccessFile(sessionConfiguration.getAbsolutePath(), "rw");

    if (uberPage.isBootstrap()) {
      file.setLength(IConstants.BEACON_START + IConstants.BEACON_LENGTH);
      file.writeInt(getSessionConfiguration().getVersionMajor());
      file.writeInt(getSessionConfiguration().getVersionMinor());
      file.writeBoolean(getSessionConfiguration().isChecksummed());
      file.writeBoolean(getSessionConfiguration().isEncrypted());
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
    file.seek(IConstants.BEACON_START);
    file.writeLong(uberPageReference.getStart());
    file.writeInt(uberPageReference.getLength());
    file.writeLong(uberPageReference.getChecksum());

    file.close();

    return uberPage;
  }

  /**
   * {@inheritDoc}
   */
  protected void close() {
    mPageWriter.close();
    mPageWriter = null;
    super.close();
  }

  protected final NamePage prepareNamePage(final PageReference reference) {

    NamePage page = (NamePage) reference.getPage();

    if (!reference.isInstantiated()) {
      if (reference.isCommitted()) {
        page = new NamePage(dereferenceNamePage(reference));
        reference.setPage(page);
      } else {
        page = new NamePage();
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = new NamePage(page);
        reference.setPage(page);
      }
    }

    return page;
  }

  protected final IndirectPage prepareIndirectPage(final PageReference reference) {

    IndirectPage page = (IndirectPage) reference.getPage();

    if (!reference.isInstantiated()) {
      if (reference.isCommitted()) {
        page = new IndirectPage(dereferenceIndirectPage(reference));
        reference.setPage(page);
      } else {
        page = new IndirectPage();
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = new IndirectPage(page);
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
        page = new NodePage(dereferenceNodePage(reference, nodePageKey));
        reference.setPage(page);
      } else {
        page = new NodePage(nodePageKey);
        reference.setPage(page);
      }
    } else {
      if (!reference.isDirty()) {
        page = new NodePage(page);
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
        new RevisionRootPage(getRevisionRootPage(getUberPage()
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
      reference = prepareIndirectPage(reference).getReference(offset);
    }

    // Return reference to leaf of indirect tree.
    return reference;
  }

}
