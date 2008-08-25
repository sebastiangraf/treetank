/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import org.treetank.nodelayer.AbstractNode;
import org.treetank.nodelayer.DocumentRootNode;
import org.treetank.nodelayer.ElementNode;
import org.treetank.nodelayer.FullTextLeafNode;
import org.treetank.nodelayer.FullTextNode;
import org.treetank.nodelayer.TextNode;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.IConstants;

/**
 * <h1>WriteTransactionState</h1>
 * 
 * <p>
 * See {@link ReadTransactionState}.
 * </p>
 */
public final class WriteTransactionState extends ReadTransactionState {

  /** Page writer to serialize. */
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
        .getLastCommittedRevisionNumber(), null);
    mPageWriter = new PageWriter(sessionConfiguration);
    setRevisionRootPage(prepareRevisionRootPage());
  }

  /**
   * Getter for page writer.
   * 
   * @return Page writer assigned to this transaction.
   */
  protected final PageWriter getPageWriter() {
    return mPageWriter;
  }

  /**
   * Prepare node for modifications (COW).
   */
  protected final AbstractNode prepareNode(final long nodeKey) {
    return prepareNodePage(nodePageKey(nodeKey)).getNode(
        nodePageOffset(nodeKey));
  }

  /**
   * Create fresh node and prepare node page for modifications (COW).
   * 
   * @param <N> Subclass of AbstractNode.
   * @param node node to add.
   * @return Unmodified node from parameter for convenience.
   */
  protected final <N extends AbstractNode> N createNode(final N node) {
    // Allocate node key and increment node count.
    getRevisionRootPage().incrementNodeCountAndMaxNodeKey();
    // Prepare node page (COW).
    prepareNodePage(nodePageKey(getRevisionRootPage().getMaxNodeKey()))
        .setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()), node);
    return node;
  }

  protected final DocumentRootNode createDocumentNode() {
    return createNode(new DocumentRootNode());
  }

  protected final ElementNode createElementNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int nameKey,
      final int uriKey,
      final int type) {
    return createNode(new ElementNode(
        getRevisionRootPage().getMaxNodeKey() + 1,
        parentKey,
        firstChildKey,
        leftSiblingKey,
        rightSiblingKey,
        nameKey,
        uriKey,
        type));
  }

  protected final TextNode createTextNode(
      final long parentKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int valueType,
      final byte[] value) {
    return createNode(new TextNode(
        getRevisionRootPage().getMaxNodeKey() + 1,
        parentKey,
        leftSiblingKey,
        rightSiblingKey,
        valueType,
        value));
  }

  protected final FullTextNode createFullTextNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int nameKey) {
    return createNode(new FullTextNode(
        getRevisionRootPage().getMaxNodeKey() + 1,
        parentKey,
        firstChildKey,
        leftSiblingKey,
        rightSiblingKey,
        nameKey));
  }

  protected final FullTextLeafNode createFullTextLeafNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey) {
    return createNode(new FullTextLeafNode(
        getRevisionRootPage().getMaxNodeKey() + 1,
        parentKey,
        firstChildKey,
        leftSiblingKey,
        rightSiblingKey));
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
  public final void commit(final PageReference<? extends AbstractPage> reference) {
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
  protected final void commit(
      final PageReference<? extends AbstractPage>[] references) {
    for (int i = 0, l = references.length; i < l; i++) {
      commit(references[i]);
    }
  }

  protected final UberPage commit(
      final SessionConfiguration sessionConfiguration) {

    final PageReference<UberPage> uberPageReference =
        new PageReference<UberPage>();
    final UberPage uberPage = getUberPage();

    RandomAccessFile file = null;

    try {
      file =
          new RandomAccessFile(
              sessionConfiguration.getAbsolutePath(),
              IConstants.READ_WRITE);

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

      byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];

      // Write secondary beacon.
      file.seek(file.length());
      file.writeLong(uberPageReference.getStart());
      file.writeInt(uberPageReference.getLength());
      uberPageReference.getChecksum(tmp);
      file.write(tmp);

      // Write primary beacon.
      file.seek(IConstants.BEACON_START);
      file.writeLong(uberPageReference.getStart());
      file.writeInt(uberPageReference.getLength());
      uberPageReference.getChecksum(tmp);
      file.write(tmp);

    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

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

  protected final NamePage prepareNamePage(
      final PageReference<NamePage> reference) {

    NamePage page = reference.getPage();

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

  protected final IndirectPage prepareIndirectPage(
      final PageReference<IndirectPage> reference) {

    IndirectPage page = reference.getPage();

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
    PageReference<NodePage> reference =
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
            .getLastCommittedRevisionNumber()));

    // Prepare indirect tree to hold reference to prepared revision root page.
    final PageReference<RevisionRootPage> revisionRootPageReference =
        prepareLeafOfTree(
            getUberPage().getIndirectPageReference(),
            getUberPage().getRevisionNumber());

    // Link the prepared revision root page with the prepared indirect tree.
    revisionRootPageReference.setPage(revisionRootPage);

    // Return prepared revision root page.
    return revisionRootPage;
  }

  protected final PageReference prepareLeafOfTree(
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
      reference = prepareIndirectPage(reference).getReference(offset);
    }

    // Return reference to leaf of indirect tree.
    return reference;
  }

}
