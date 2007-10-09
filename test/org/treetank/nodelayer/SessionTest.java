/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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

package org.treetank.nodelayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class SessionTest {

  public static final String TEST_INSERT_CHILD_PATH =
      "generated/SessionTest_InsertChild.tnk";

  public static final String TEST_REVISION_PATH =
      "generated/SessionTest_Revision.tnk";

  public static final String TEST_SHREDDED_REVISION_PATH =
      "generated/SessionTest_ShreddedRevision.tnk";

  public static final String TEST_EXISTING_PATH =
      "generated/SessionTest_Existing.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_INSERT_CHILD_PATH).delete();
    new File(TEST_REVISION_PATH).delete();
    new File(TEST_SHREDDED_REVISION_PATH).delete();
    new File(TEST_EXISTING_PATH).delete();
  }

  @Test
  public void testInsertChild() throws Exception {

    final ISession session = new Session(TEST_INSERT_CHILD_PATH);

    final IWriteTransaction trx = session.beginWriteTransaction();

    TestDocument.create(trx);

    assertEquals(true, trx.moveToRoot());
    assertEquals(IConstants.DOCUMENT, trx.getKind());

    assertEquals(true, trx.moveToFirstChild());
    assertEquals(IConstants.ELEMENT, trx.getKind());
    assertEquals("a", trx.getLocalPart());

    //    assertEquals(true, trx.moveToFirstAttribute());
    //    assertEquals(IConstants.ATTRIBUTE, trx.getKind());
    //    assertEquals("j", new String(trx.getValue(), IConstants.ENCODING));

    session.abort();
    session.close();

  }

  @Test
  public void testRevision() throws Exception {

    final ISession session = new Session(TEST_REVISION_PATH);

    final IReadTransaction rTrx = session.beginReadTransaction();
    assertEquals(IConstants.UBP_ROOT_REVISION_KEY, rTrx.revisionKey());
    assertEquals(0L, rTrx.revisionSize());

    final IWriteTransaction wTrx = session.beginWriteTransaction();
    assertEquals(1L, wTrx.revisionKey());
    assertEquals(0L, wTrx.revisionSize());

    assertEquals(IConstants.UBP_ROOT_REVISION_KEY, rTrx.revisionKey());
    assertEquals(0L, rTrx.revisionSize());

    // Insert root and check.
    wTrx.insertRoot("foo");
    assertEquals(1L, wTrx.revisionKey());
    assertEquals(1L, wTrx.revisionSize());
    assertEquals("foo", new String(wTrx.getValue(), IConstants.ENCODING));

    assertEquals(IConstants.UBP_ROOT_REVISION_KEY, rTrx.revisionKey());
    assertEquals(0L, rTrx.revisionSize());

    // Commit and check.
    session.commit();

    assertEquals(IConstants.UBP_ROOT_REVISION_KEY, rTrx.revisionKey());
    assertEquals(0L, rTrx.revisionSize());

    final IReadTransaction rTrx2 = session.beginReadTransaction();
    assertEquals(1L, rTrx2.revisionKey());
    assertEquals(1L, rTrx2.revisionSize());

  }

  @Test
  public void testShreddedRevision() throws Exception {

    final ISession session = new Session(TEST_SHREDDED_REVISION_PATH);

    final IWriteTransaction wTrx1 = session.beginWriteTransaction();
    TestDocument.create(wTrx1);
    assertEquals(1L, wTrx1.revisionKey());
    assertEquals(11L, wTrx1.revisionSize());
    session.commit();

    final IReadTransaction rTrx1 = session.beginReadTransaction();
    assertEquals(1L, rTrx1.revisionKey());
    rTrx1.moveTo(9L);
    assertEquals("bar", new String(rTrx1.getValue(), IConstants.ENCODING));

    final IWriteTransaction wTrx2 = session.beginWriteTransaction();
    assertEquals(2L, wTrx2.revisionKey());
    wTrx2.moveTo(9L);
    wTrx2.setValue(UTF.convert("bar2"));

    assertEquals("bar", new String(rTrx1.getValue(), IConstants.ENCODING));
    assertEquals("bar2", new String(wTrx2.getValue(), IConstants.ENCODING));

    session.abort();

    final IReadTransaction rTrx2 = session.beginReadTransaction();
    assertEquals(1L, rTrx2.revisionKey());
    rTrx2.moveTo(9L);
    assertEquals("bar", new String(rTrx2.getValue(), IConstants.ENCODING));

  }

  @Test
  public void testExisting() throws Exception {

    final ISession session1 = new Session(TEST_EXISTING_PATH);

    final IWriteTransaction wTrx1 = session1.beginWriteTransaction();
    TestDocument.create(wTrx1);
    session1.commit();
    session1.close();

    final ISession session2 = new Session(TEST_EXISTING_PATH);
    final IReadTransaction rTrx1 = session2.beginReadTransaction();
    assertEquals(1L, rTrx1.revisionKey());
    rTrx1.moveTo(9L);
    assertEquals("bar", new String(rTrx1.getValue(), IConstants.ENCODING));

    final IWriteTransaction wTrx2 = session2.beginWriteTransaction();
    assertEquals(2L, wTrx2.revisionKey());
    wTrx2.moveTo(9L);
    wTrx2.setValue(UTF.convert("bar2"));

    assertEquals("bar", new String(rTrx1.getValue(), IConstants.ENCODING));
    assertEquals("bar2", new String(wTrx2.getValue(), IConstants.ENCODING));

    session2.commit();

    final ISession session3 = new Session(TEST_EXISTING_PATH);
    final IReadTransaction rTrx2 = session3.beginReadTransaction();
    assertEquals(2L, rTrx2.revisionKey());
    rTrx2.moveTo(9L);
    assertEquals("bar2", new String(rTrx2.getValue(), IConstants.ENCODING));

  }

}
