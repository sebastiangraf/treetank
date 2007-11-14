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

public class FilterAxisTest {

  public static final String PATH =
      "generated" + File.separator + "FilterAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testNameAxisTest() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    final IAxis axis1 =
        new FilterAxis(new DescendantAxis(wtx), new NameAxisFilter(wtx
            .keyForName("b")));

    assertEquals(true, axis1.hasNext());
    assertEquals(4L, axis1.next());

    assertEquals(true, axis1.hasNext());
    assertEquals(8L, axis1.next());
    assertEquals(false, axis1.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testValueAxisTest() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    final IAxis axis1 =
        new FilterAxis(new DescendantAxis(wtx), new ValueAxisFilter("foo"));

    assertEquals(true, axis1.hasNext());
    assertEquals(5L, axis1.next());

    assertEquals(false, axis1.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
