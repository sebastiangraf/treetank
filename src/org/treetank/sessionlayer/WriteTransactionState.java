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

import org.treetank.api.IWriteTransactionState;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageCache;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.RevisionRootPage;

public final class WriteTransactionState extends ReadTransactionState
    implements
    IWriteTransactionState {

  private final PageWriter mPageWriter;

  public WriteTransactionState(
      final PageCache pageCache,
      final PageReader pageReader,
      final PageWriter pageWriter,
      final RevisionRootPage revisionRootPage) {
    super(pageCache, pageReader, revisionRootPage);
    mPageWriter = pageWriter;
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
    mNodePage =
        AbstractPage.prepareNodePage(this, getStaticNodeTree().prepare(
            this,
            nodePageKey), nodePageKey);
    return mNodePage;
  }

  /**
   * {@inheritDoc}
   */
  public final int createNameKey(final String name) throws Exception {
    final String string = (name == null ? "" : name);
    final int nameKey = string.hashCode();
    if (getName(nameKey) == null) {
      mNamePage =
          AbstractPage.prepareNamePage(this, mRevisionRootPage
              .getNamePageReference());
      mNamePage.setName(nameKey, string);
    }
    return nameKey;
  }

}
