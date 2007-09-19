/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id:NamePage.java 2941 2007-09-18 16:45:16Z kramis $
 * 
 */

package org.treetank.pagelayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.UTF;

final public class NamePage extends AbstractPage implements IPage {

  /** Map the hash of a name to its name. */
  private final Map<Integer, String> mNameMap;

  private NamePage(final PageCache pageCache) {
    super(pageCache);
    mNameMap = new HashMap<Integer, String>();
  }

  public static final NamePage create(final PageCache pageCache) {

    final NamePage namePage = new NamePage(pageCache);

    return namePage;

  }

  public static final NamePage read(
      final PageCache pageCache,
      final FastByteArrayReader in) throws Exception {

    final NamePage namePage = new NamePage(pageCache);

    // Names (deep load).
    for (int i = 0, l = in.readInt(); i < l; i++) {
      namePage.mNameMap.put(in.readInt(), UTF.convert(in.readByteArray()));
    }

    return namePage;

  }

  public static final NamePage clone(final NamePage committedNamePage) {

    final NamePage namePage = new NamePage(committedNamePage.mPageCache);

    // Names (deep COW).
    namePage.mNameMap.putAll(committedNamePage.mNameMap);

    return namePage;
  }

  /**
   * Get name belonging to name key.
   * 
   * @param nameKey Name key identifying name.
   * @return Name of name key.
   */
  public final String getName(final int nameKey) {
    return mNameMap.get(nameKey);
  }

  /**
   * Create name key given a name.
   * 
   * @param name Name to create key for.
   * @return Name key.
   */
  public final void setName(final int nameKey, final String name) {
    mNameMap.put(nameKey, name);
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageWriter pageWriter) throws Exception {
    // Nothing to do here.
  }

  /**
   * {@inheritDoc}
   */
  public void serialize(final FastByteArrayWriter out) throws Exception {
    out.writeInt(mNameMap.size());
    Iterator<Integer> keyIterator = mNameMap.keySet().iterator();
    int key;
    while (keyIterator.hasNext()) {
      key = keyIterator.next();
      out.writeInt(key);
      out.writeByteArray(UTF.convert(mNameMap.get(key)));
    }
  }

}
