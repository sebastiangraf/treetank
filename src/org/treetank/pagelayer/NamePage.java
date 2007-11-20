/*
 * Copyright (c) 2007, Marc Kramis
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

package org.treetank.pagelayer;

import java.util.HashMap;
import java.util.Map;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.TypedValue;

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
      mNameMap.put(in.readVarInt(), TypedValue.parseString(in.readByteArray()));
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
      out.writeByteArray(TypedValue.getBytes(mNameMap.get(key)));
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
