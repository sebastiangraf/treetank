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

public class DescendantAxisIteratorTest {

  public static final String PATH =
      "generated" + File.separator + "DescendantAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToRoot();
    final IAxisIterator descendantIterator1 = new DescendantAxisIterator(wtx);
    assertEquals(true, descendantIterator1.next());
    assertEquals(1L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(2L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(3L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(4L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(5L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(6L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(7L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(false, descendantIterator1.next());

    // Find descendants starting from nodeKey 1L (first child of root).
    wtx.moveTo(1L);
    final IAxisIterator descendantIterator2 = new DescendantAxisIterator(wtx);
    assertEquals(true, descendantIterator2.next());
    assertEquals(2L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(3L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(4L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(5L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(6L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(7L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, descendantIterator2.next());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(false, descendantIterator2.next());

    // Find descendants starting from nodeKey 4L (second child of root).
    wtx.moveTo(7L);
    final IAxisIterator descendantIterator3 = new DescendantAxisIterator(wtx);
    assertEquals(true, descendantIterator3.next());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, descendantIterator3.next());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(false, descendantIterator3.next());

    // Find descendants starting from nodeKey 5L (last in document order).
    wtx.moveTo(10L);
    final IAxisIterator descendantIterator4 = new DescendantAxisIterator(wtx);
    assertEquals(false, descendantIterator4.next());

    wtx.abort();
    session.close();

  }

}
