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

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IConstants;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class DescendantAxisTest {

  public static final String PATH =
      "generated" + File.separator + "DescendantAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testIterate() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    final IAxis axis1 = new DescendantAxis(wtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(2L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(3L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(4L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(5L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(6L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(7L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(8L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(9L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(10L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(11L, axis1.next());

    assertEquals(false, axis1.hasNext());
    assertEquals(IConstants.DOCUMENT_ROOT_KEY, wtx.getNodeKey());

    // Find descendants starting from nodeKey 1L (first child of root).
    wtx.moveTo(2L);
    final IAxis axis2 = new DescendantAxis(wtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(3L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(4L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(5L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(6L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(7L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(8L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(9L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(10L, axis2.next());

    assertEquals(true, axis2.hasNext());
    assertEquals(11L, axis2.next());

    assertEquals(false, axis2.hasNext());
    assertEquals(2L, wtx.getNodeKey());

    // Find descendants starting from nodeKey 4L (second child of root).
    wtx.moveTo(8L);
    final IAxis axis3 = new DescendantAxis(wtx);
    assertEquals(true, axis3.hasNext());
    assertEquals(9L, axis3.next());

    assertEquals(true, axis3.hasNext());
    assertEquals(10L, axis3.next());

    assertEquals(false, axis3.hasNext());
    assertEquals(8L, wtx.getNodeKey());

    // Find descendants starting from nodeKey 5L (last in document order).
    wtx.moveTo(11L);
    final IAxis axis4 = new DescendantAxis(wtx);
    assertEquals(false, axis4.hasNext());
    assertEquals(11L, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
