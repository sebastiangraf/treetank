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

import java.util.HashMap;
import java.util.Map;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.UTF;

/**
 * <h1>NamePage</h1>
 * 
 * <p>
 * Name page holds all names and their keys for a revision.
 * </p>
 */
public final class NamePage extends AbstractPage {

  /** Map the hash of a name to its name. */
  private final Map<Integer, String> mNameMap;

  /**
   * Create name page.
   */
  public NamePage() {
    super(0);
    mNameMap = new HashMap<Integer, String>();
  }

  /**
   * Read name page.
   * 
   * @param in Input bytes to read from.
   */
  public NamePage(final FastByteArrayReader in) {
    super(0, in);
    mNameMap = new HashMap<Integer, String>();

    for (int i = 0, l = in.readVarInt(); i < l; i++) {
      mNameMap.put(in.readVarInt(), UTF.convert(in.readByteArray()));
    }
  }

  /**
   * Clone name page.
   * 
   * @param committedNamePage Page to clone.
   */
  public NamePage(final NamePage committedNamePage) {
    super(0, committedNamePage);
    mNameMap = new HashMap<Integer, String>(committedNamePage.mNameMap);
  }

  /**
   * Get name belonging to name key.
   * 
   * @param key Name key identifying name.
   * @return Name of name key.
   */
  public final String getName(final int key) {
    return mNameMap.get(key);
  }

  /**
   * Create name key given a name.
   * 
   * @param key Key for given name.
   * @param name Name to create key for.
   */
  public final void setName(final int key, final String name) {
    mNameMap.put(key, name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    super.serialize(out);

    out.writeVarInt(mNameMap.size());

    for (final int key : mNameMap.keySet()) {
      out.writeVarInt(key);
      out.writeByteArray(UTF.convert(mNameMap.get(key)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString()
        + ": nameCount="
        + mNameMap.size()
        + ", isDirty="
        + isDirty();
  }

}
