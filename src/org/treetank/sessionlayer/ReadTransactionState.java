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
import org.treetank.utils.StaticTree;

public class ReadTransactionState implements IReadTransactionState {

  /** Page cache mapping start address of page to IPage. */
  protected final Map<Long, IPage> mPageCache;

  private final PageReader mPageReader;

  private final StaticTree mStaticNodeTree;

  protected RevisionRootPage mRevisionRootPage;

  protected NodePage mNodePage;

  protected NamePage mNamePage;

  public ReadTransactionState(
      final Map<Long, IPage> pageCache,
      final PageReader pageReader,
      final RevisionRootPage revisionRootPage) {
    mPageCache = pageCache;
    mPageReader = pageReader;
    mRevisionRootPage = revisionRootPage;
    if (revisionRootPage != null) {
      mStaticNodeTree =
          new StaticTree(revisionRootPage.getIndirectPageReference());
    } else {
      mStaticNodeTree = null;
    }
    mNodePage = null;
    mNamePage = null;
  }

  /**
   * {@inheritDoc}
   */
  public final StaticTree getStaticNodeTree() {
    return mStaticNodeTree;
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
      mNodePage =
          dereferenceNodePage(
              mStaticNodeTree.get(this, nodePageKey),
              nodePageKey);
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

  public final NodePage dereferenceNodePage(
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

  public final NamePage dereferenceNamePage(final PageReference reference)
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

  public final UberPage dereferenceUberPage(final PageReference reference)
      throws Exception {

    // Get uncommitted referenced page if there is one.
    UberPage page = (UberPage) reference.getPage();

    // Get committed referenced page from cache if there is one.
    if (page == null) {
      page = (UberPage) mPageCache.get(reference.getStart());
    }

    // Get committed referenced page from storage.
    if (page == null) {
      final FastByteArrayReader in = mPageReader.read(reference);
      page = UberPage.read(in);
      mPageCache.put(reference.getStart(), page);
    }

    return page;
  }

}
