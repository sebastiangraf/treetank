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

import org.treetank.api.IReadTransactionState;
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageCache;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.utils.StaticTree;

public class ReadTransactionState implements IReadTransactionState {

  private final PageCache mPageCache;

  private final PageReader mPageReader;

  private final StaticTree mStaticNodeTree;

  protected RevisionRootPage mRevisionRootPage;

  protected NodePage mNodePage;

  protected NamePage mNamePage;

  public ReadTransactionState(
      final PageCache pageCache,
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
  public final PageCache getPageCache() {
    return mPageCache;
  }

  /**
   * {@inheritDoc}
   */
  public final PageReader getPageReader() {
    return mPageReader;
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
  public final NodePage getNodePage(final long nodePageKey) throws Exception {
    if (mNodePage == null || mNodePage.getNodePageKey() != nodePageKey) {
      mNodePage = mRevisionRootPage.getNodePage(this, nodePageKey);
    }
    return mNodePage;
  }

  /**
   * {@inheritDoc}
   */
  public final String getName(final int nameKey) throws Exception {
    if (mNamePage == null) {
      mNamePage =
          mPageCache.dereferenceNamePage(this, mRevisionRootPage
              .getNamePageReference());
    }
    return mNamePage.getName(nameKey);
  }

}
