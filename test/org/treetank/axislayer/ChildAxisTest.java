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
