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

package org.treetank.axislayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class DescendantAxisTest {

  public static final String PATH =
      "generated" + File.separator + "DescendantAxisTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocument();
    final IAxis axis1 = new DescendantAxis(wtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(2L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(3L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(4L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(5L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(6L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(7L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(true, axis1.hasNext());
    assertEquals(11L, wtx.getNodeKey());

    assertEquals(false, axis1.hasNext());

    // Find descendants starting from nodeKey 1L (first child of root).
    wtx.moveTo(2L);
    final IAxis axis2 = new DescendantAxis(wtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(3L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(4L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(5L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(6L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(7L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(true, axis2.hasNext());
    assertEquals(11L, wtx.getNodeKey());

    assertEquals(false, axis2.hasNext());

    // Find descendants starting from nodeKey 4L (second child of root).
    wtx.moveTo(8L);
    final IAxis axis3 = new DescendantAxis(wtx);
    assertEquals(true, axis3.hasNext());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, axis3.hasNext());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(false, axis3.hasNext());

    // Find descendants starting from nodeKey 5L (last in document order).
    wtx.moveTo(11L);
    final IAxis axis4 = new DescendantAxis(wtx);
    assertEquals(false, axis4.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
