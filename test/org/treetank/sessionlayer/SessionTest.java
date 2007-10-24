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
 * $Id:SessionTest.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

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
  public void setUp() throws Exception {
    new File(NON_EXISTING_PATH).delete();
    new File(TEST_INSERT_CHILD_PATH).delete();
    new File(TEST_REVISION_PATH).delete();
    new File(TEST_SHREDDED_REVISION_PATH).delete();
    new File(TEST_EXISTING_PATH).delete();
  }

  @Test
  public void testNoWritesBeforeFirstCommit() throws Exception {

    ISession session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());
    session.close();
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());

    session = Session.beginSession(TEST_INSERT_CHILD_PATH);
    assertEquals(0L, new File(TEST_INSERT_CHILD_PATH).length());

    final IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();
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
      long counter = 0;
      while (secondAccess.isAlive() && counter < 100000) {
        counter++;
      }
      if (secondAccess.isAlive()) {
        fail("Second access should have died!");
      }

    } catch (final Exception e) {
      fail(e.toString());
      e.printStackTrace();
    }
  }

  @Test
  public void testInsertChild() throws Exception {

    final ISession session = Session.beginSession(TEST_INSERT_CHILD_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();

    TestDocument.create(wtx);

    TestCase.assertNotNull(wtx.moveToRoot());
    assertEquals(IConstants.DOCUMENT, wtx.getKind());

    TestCase.assertNotNull(wtx.moveToFirstChild());
    assertEquals(IConstants.ELEMENT, wtx.getKind());
    assertEquals("a", wtx.getLocalPart());

    wtx.abort();
    session.close();

  }

  @Test
  public void testRevision() throws Exception {

    final ISession session = Session.beginSession(TEST_REVISION_PATH);

    IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(0L, rtx.revisionKey());
    assertEquals(1L, rtx.revisionSize());

    final IWriteTransaction wtx = session.beginWriteTransaction();
    assertEquals(0L, wtx.revisionKey());
    assertEquals(1L, wtx.revisionSize());

    // Commit and check.
    wtx.commit();

    rtx = session.beginReadTransaction();

    assertEquals(IConstants.UBP_ROOT_REVISION_KEY, rtx.revisionKey());
    assertEquals(1L, rtx.revisionSize());

    final IReadTransaction rtx2 = session.beginReadTransaction();
    assertEquals(0L, rtx2.revisionKey());
    assertEquals(1L, rtx2.revisionSize());

  }

  @Test
  public void testShreddedRevision() throws Exception {

    final ISession session = Session.beginSession(TEST_SHREDDED_REVISION_PATH);

    final IWriteTransaction wtx1 = session.beginWriteTransaction();
    TestDocument.create(wtx1);
    assertEquals(0L, wtx1.revisionKey());
    assertEquals(11L, wtx1.revisionSize());
    wtx1.commit();

    final IReadTransaction rtx1 = session.beginReadTransaction();
    assertEquals(0L, rtx1.revisionKey());
    rtx1.moveTo(9L);
    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));

    final IWriteTransaction wtx2 = session.beginWriteTransaction();
    assertEquals(1L, wtx2.revisionKey());
    wtx2.moveTo(9L);
    wtx2.setValue(UTF.convert("bar2"));

    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));
    assertEquals("bar2", new String(
        wtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    wtx2.abort();

    final IReadTransaction rtx2 = session.beginReadTransaction();
    assertEquals(0L, rtx2.revisionKey());
    rtx2.moveTo(9L);
    assertEquals(
        "bar",
        new String(rtx2.getValue(), IConstants.DEFAULT_ENCODING));

  }

  @Test
  public void testExisting() throws Exception {

    final ISession session1 = Session.beginSession(TEST_EXISTING_PATH);

    final IWriteTransaction wtx1 = session1.beginWriteTransaction();
    TestDocument.create(wtx1);
    assertEquals(0L, wtx1.revisionKey());
    wtx1.commit();
    session1.close();

    final ISession session2 = Session.beginSession(TEST_EXISTING_PATH);
    final IReadTransaction rtx1 = session2.beginReadTransaction();
    assertEquals(0L, rtx1.revisionKey());
    rtx1.moveTo(9L);
    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));

    final IWriteTransaction wtx2 = session2.beginWriteTransaction();
    assertEquals(1L, wtx2.revisionKey());
    wtx2.moveTo(9L);
    wtx2.setValue(UTF.convert("bar2"));

    assertEquals(
        "bar",
        new String(rtx1.getValue(), IConstants.DEFAULT_ENCODING));
    assertEquals("bar2", new String(
        wtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    rtx1.close();
    wtx2.commit();
    session2.close();

    final ISession session3 = Session.beginSession(TEST_EXISTING_PATH);
    final IReadTransaction rtx2 = session3.beginReadTransaction();
    assertEquals(1L, rtx2.revisionKey());
    rtx2.moveTo(9L);
    assertEquals("bar2", new String(
        rtx2.getValue(),
        IConstants.DEFAULT_ENCODING));

    rtx2.close();
    session3.close();

  }

  @Test
  public void testIsSelected() throws Exception {

    final ISession session = Session.beginSession(TEST_EXISTING_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();

    final IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(11L, rtx.revisionSize());
    assertEquals(true, rtx.isSelected());
    TestCase.assertNull(rtx.moveTo(12L));
    assertEquals(false, rtx.isSelected());

    rtx.close();
    session.close();

  }

}
