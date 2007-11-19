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

package org.treetank.sessionlayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class SessionTest {

  public static final String NON_EXISTING_PATH =
      "generated" + File.separator + "NonExistingSessionTest.tnk";

  public static final String TEST_INSERT_CHILD_PATH =
      "generated" + File.separator + "InsertChildSessionTest.tnk";

  public static final String TEST_REVISION_PATH =
      "generated" + File.separator + "RevisionSessionTest.tnk";

  public static final String TEST_SHREDDED_REVISION_PATH =
      "generated" + File.separator + "ShreddedRevisionSessionTest.tnk";

  public static final String TEST_EXISTING_PATH =
      "generated" + File.separator + "ExistingSessionTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(NON_EXISTING_PATH);
    Session.removeSession(TEST_INSERT_CHILD_PATH);
    Session.removeSession(TEST_REVISION_PATH);
    Session.removeSession(TEST_SHREDDED_REVISION_PATH);
    Session.removeSession(TEST_EXISTING_PATH);
  }

  @Test
  public void testClosed() throws IOException {

    ISession session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    session.close();

    try {
      session.getAbsolutePath();
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }

    session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    IReadTransaction rtx = session.beginReadTransaction();
    rtx.close();

    try {
      rtx.getAttributeCount();
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }

    session.close();

    try {
      session.getAbsolutePath();
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }
  }

  @Test
  public void testNoWritesBeforeFirstCommit() throws IOException {

    ISession session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());
    session.close();
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());

    session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());

    final IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();
    wtx.close();
    session.close();

    session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.close();
    session.close();

    TestCase.assertNotSame(0L, new File(TEST_INSERT_CHILD_PATH).length());
  }

  @Test
  public void testNonExisting() {
    try {
      Session.beginSession(NON_EXISTING_PATH);
      final Thread secondAccess = new Thread() {
        @Override
        public void run() {
          try {
            Session.beginSession(NON_EXISTING_PATH);
            fail();
          } catch (final Exception e) {
            // Must catch to pass test.
          }
        }
      };
      secondAccess.start();
      Thread.sleep(100);
      if (secondAccess.isAlive()) {
        fail("Second access should have died!");
      }

    } catch (final Exception e) {
      fail(e.toString());
      e.printStackTrace();
    }
  }

  @Test
  public void testInsertChild() throws IOException {

    final ISession session = Session.beginSession(TEST_INSERT_CHILD_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();

    TestDocument.create(wtx);

    TestCase.assertNotNull(wtx.moveToDocumentRoot());
    assertEquals(IConstants.DOCUMENT_ROOT, wtx.getKind());

    TestCase.assertNotNull(wtx.moveToFirstChild());
    assertEquals(IConstants.ELEMENT, wtx.getKind());
    assertEquals("a", wtx.getLocalPart());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testRevision() throws IOException {

    final ISession session = Session.beginSession(TEST_REVISION_PATH);

    IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(0L, rtx.getRevisionNumber());
    assertEquals(2L, rtx.getRevisionSize());

    final IWriteTransaction wtx = session.beginWriteTransaction();
    assertEquals(0L, wtx.getRevisionNumber());
    assertEquals(2L, wtx.getRevisionSize());

    // Commit and check.
    wtx.commit();

    rtx = session.beginReadTransaction();

    assertEquals(IConstants.UBP_ROOT_REVISION_NUMBER, rtx.getRevisionNumber());
    assertEquals(2L, rtx.getRevisionSize());

    final IReadTransaction rtx2 = session.beginReadTransaction();
    assertEquals(0L, rtx2.getRevisionNumber());
    assertEquals(2L, rtx2.getRevisionSize());

  }

  @Test
  public void testShreddedRevision() throws IOException {

    final ISession session = Session.beginSession(TEST_SHREDDED_REVISION_PATH);

    final IWriteTransaction wtx1 = session.beginWriteTransaction();
    TestDocument.create(wtx1);
    assertEquals(0L, wtx1.getRevisionNumber());
    assertEquals(12L, wtx1.getRevisionSize());
    wtx1.commit();
    wtx1.close();

    final IReadTransaction rtx1 = session.beginReadTransaction();
    assertEquals(0L, rtx1.getRevisionNumber());
    rtx1.moveTo(10L);
    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));

    final IWriteTransaction wtx2 = session.beginWriteTransaction();
    assertEquals(1L, wtx2.getRevisionNumber());
    wtx2.moveTo(10L);
    wtx2.setValue(UTF.getBytes("bar2"));

    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));
    assertEquals("bar2", new String(
        wtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    wtx2.abort();
    wtx2.close();

    final IReadTransaction rtx2 = session.beginReadTransaction();
    assertEquals(0L, rtx2.getRevisionNumber());
    rtx2.moveTo(10L);
    assertEquals(
        "bar",
        new String(rtx2.getValue(), IConstants.DEFAULT_ENCODING));

  }

  @Test
  public void testExisting() throws IOException {

    final ISession session1 = Session.beginSession(TEST_EXISTING_PATH);

    final IWriteTransaction wtx1 = session1.beginWriteTransaction();
    TestDocument.create(wtx1);
    assertEquals(0L, wtx1.getRevisionNumber());
    wtx1.commit();
    wtx1.close();
    session1.close();

    final ISession session2 = Session.beginSession(TEST_EXISTING_PATH);
    final IReadTransaction rtx1 = session2.beginReadTransaction();
    assertEquals(0L, rtx1.getRevisionNumber());
    rtx1.moveTo(10L);
    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));

    final IWriteTransaction wtx2 = session2.beginWriteTransaction();
    assertEquals(1L, wtx2.getRevisionNumber());
    wtx2.moveTo(10L);
    wtx2.setValue(UTF.getBytes("bar2"));

    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));
    assertEquals("bar2", new String(
        wtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    rtx1.close();
    wtx2.commit();
    wtx2.close();
    session2.close();

    final ISession session3 = Session.beginSession(TEST_EXISTING_PATH);
    final IReadTransaction rtx2 = session3.beginReadTransaction();
    assertEquals(1L, rtx2.getRevisionNumber());
    rtx2.moveTo(10L);
    assertEquals("bar2", new String(
        rtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    rtx2.close();
    session3.close();

  }

  @Test
  public void testIdempotentClose() throws IOException {

    final ISession session = Session.beginSession(TEST_EXISTING_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();
    wtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(12L, rtx.getRevisionSize());
    assertEquals(false, rtx.moveTo(12L));
    rtx.close();
    rtx.close();

    session.close();
    session.close();
  }

  @Test
  public void testAutoCommit() throws IOException {

    final ISession session = Session.beginSession(TEST_EXISTING_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(12L, rtx.getRevisionSize());
    assertEquals(false, rtx.moveTo(12L));
    rtx.close();

    session.close();
  }

}
