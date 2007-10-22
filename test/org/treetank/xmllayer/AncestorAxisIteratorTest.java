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

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class AncestorAxisIteratorTest {

  public static final String PATH =
      "generated" + File.separator + "AncestorAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    // Build simple test tree.
    final ISession session = new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    // Find ancestors starting from nodeKey 0L (root).
    trx.moveTo(8L);
    final IAxisIterator ancestorIterator1 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator1.next());
    assertEquals(7L, trx.getNodeKey());

    assertEquals(true, ancestorIterator1.next());
    assertEquals(1L, trx.getNodeKey());

    assertEquals(false, ancestorIterator1.next());

    // Find ancestors starting from nodeKey 1L (first child of root).
    trx.moveTo(3L);
    final IAxisIterator ancestorIterator2 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator2.next());
    assertEquals(1L, trx.getNodeKey());

    assertEquals(false, ancestorIterator2.next());

    // Find ancestors starting from nodeKey 4L (second child of root).
    trx.moveTo(2L);
    final IAxisIterator ancestorIterator3 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator3.next());
    assertEquals(1L, trx.getNodeKey());

    assertEquals(false, ancestorIterator3.next());

    // Find ancestors starting from nodeKey 5L (last in document order).
    trx.moveTo(1L);
    final IAxisIterator ancestorIterator4 = new AncestorAxisIterator(trx);
    assertEquals(false, ancestorIterator4.next());

    session.abort();
    session.close();

  }

}
