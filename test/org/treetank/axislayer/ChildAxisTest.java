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
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class ChildAxisTest {

  public static final String TEST_ITERATE_PATH =
      "generated" + File.separator + "ChildAxisTestIterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "generated" + File.separator + "ChildAxisTestPersistent.tnk";

  @Before
  public void setUp() {
    Session.removeSession(TEST_ITERATE_PATH);
    Session.removeSession(TEST_PERSISTENT_PATH);
  }

  @Test
  public void testIterate() {

    final ISession session = Session.beginSession(TEST_ITERATE_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    final IAxis axis1 = new ChildAxis(wtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(3L, axis1.next());
    assertEquals(3, wtx.getKind());
    assertEquals("oops1", UTF.parseString(wtx.getValue()));

    assertEquals(true, axis1.hasNext());
    assertEquals(4L, axis1.next());
    assertEquals(1, wtx.getKind());
    assertEquals("b", wtx.getLocalPart());
    assertEquals("", wtx.getURI());
    assertEquals("", wtx.getPrefix());

    assertEquals(true, axis1.hasNext());
    assertEquals(7L, axis1.next());
    assertEquals(3, wtx.getKind());
    assertEquals("oops2", UTF.parseString(wtx.getValue()));

    assertEquals(true, axis1.hasNext());
    assertEquals(8L, axis1.next());
    assertEquals(1, wtx.getKind());
    assertEquals("b", wtx.getLocalPart());
    assertEquals("", wtx.getURI());
    assertEquals("", wtx.getPrefix());

    assertEquals(true, axis1.hasNext());
    assertEquals(11L, axis1.next());
    assertEquals(3, wtx.getKind());
    assertEquals("oops3", UTF.parseString(wtx.getValue()));

    assertEquals(false, axis1.hasNext());
    assertEquals(2L, wtx.getNodeKey());

    wtx.moveTo(4L);
    final IAxis axis2 = new ChildAxis(wtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(5L, axis2.next());
    assertEquals(3, wtx.getKind());
    assertEquals("foo", UTF.parseString(wtx.getValue()));

    assertEquals(true, axis2.hasNext());
    assertEquals(6L, axis2.next());
    assertEquals(1, wtx.getKind());
    assertEquals("c", wtx.getLocalPart());
    assertEquals("", wtx.getURI());
    assertEquals("", wtx.getPrefix());

    assertEquals(false, axis2.hasNext());
    assertEquals(4L, wtx.getNodeKey());

    wtx.moveTo(11L);
    final IAxis axis4 = new ChildAxis(wtx);
    assertEquals(false, axis4.hasNext());
    assertEquals(11L, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testPersistent() {

    final ISession session = Session.beginSession(TEST_PERSISTENT_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();
    session.close();

    final ISession session1 = Session.beginSession(TEST_PERSISTENT_PATH);
    final IReadTransaction rtx = session1.beginReadTransaction();

    rtx.moveTo(2L);
    final IAxis axis1 = new ChildAxis(rtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(3L, axis1.next());
    assertEquals(3, rtx.getKind());
    assertEquals("oops1", UTF.parseString(rtx.getValue()));

    assertEquals(true, axis1.hasNext());
    assertEquals(4L, axis1.next());
    assertEquals(1, rtx.getKind());
    assertEquals("b", rtx.getLocalPart());
    assertEquals("", rtx.getURI());
    assertEquals("", rtx.getPrefix());

    assertEquals(true, axis1.hasNext());
    assertEquals(7L, axis1.next());
    assertEquals(3, rtx.getKind());
    assertEquals("oops2", UTF.parseString(rtx.getValue()));

    assertEquals(true, axis1.hasNext());
    assertEquals(8L, axis1.next());
    assertEquals(1, rtx.getKind());
    assertEquals("b", rtx.getLocalPart());
    assertEquals("", rtx.getURI());
    assertEquals("", rtx.getPrefix());

    assertEquals(true, axis1.hasNext());
    assertEquals(11L, axis1.next());
    assertEquals(3, rtx.getKind());
    assertEquals("oops3", UTF.parseString(rtx.getValue()));

    assertEquals(false, axis1.hasNext());
    assertEquals(2L, rtx.getNodeKey());

    rtx.moveTo(4L);
    final IAxis axis2 = new ChildAxis(rtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(5L, axis2.next());
    assertEquals(3, rtx.getKind());
    assertEquals("foo", UTF.parseString(rtx.getValue()));

    assertEquals(true, axis2.hasNext());
    assertEquals(6L, axis2.next());
    assertEquals(1, rtx.getKind());
    assertEquals("c", rtx.getLocalPart());
    assertEquals("", rtx.getURI());
    assertEquals("", rtx.getPrefix());

    assertEquals(false, axis2.hasNext());
    assertEquals(4L, rtx.getNodeKey());

    rtx.moveTo(11L);
    final IAxis axis4 = new ChildAxis(rtx);
    assertEquals(false, axis4.hasNext());
    assertEquals(11L, rtx.getNodeKey());

    rtx.close();
    session1.close();

  }

}
