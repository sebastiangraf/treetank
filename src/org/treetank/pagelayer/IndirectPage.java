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
import org.treetank.utils.FastByteArrayReader;

/**
 * <h1>IndirectPage</h1>
 * 
 * <p>
 * Indirect page holds a set of references to build a reference tree.
 * </p>
 */
public final class IndirectPage extends Page {

  /**
   * Create indirect page.
   */
  public IndirectPage() {
    super(IConstants.INP_REFERENCE_COUNT);
  }

  /**
   * Read indirect page.
   * 
   * @param in Input bytes.
   */
  public IndirectPage(final FastByteArrayReader in) {
    super(IConstants.INP_REFERENCE_COUNT, in);
  }

  /**
   * Clone indirect page.
   * 
   * @param page Page to clone.
   */
  public IndirectPage(final IndirectPage page) {
    super(IConstants.INP_REFERENCE_COUNT, page);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString() + ": isDirty=" + isDirty();
  }

}
