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
 * $Id:UpdateTest.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class UpdateTest {

  public static final String PATH =
      "generated" + File.separator + "UpdateTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testInsertChild() throws IOException {

    ISession session = Session.beginSession(PATH);

    // Document root.
    IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();
    wtx.close();

    IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(2L, rtx.getRevisionSize());
    assertEquals(0L, rtx.getRevisionNumber());
    rtx.close();

    // Insert 100 children.
    for (int i = 1; i <= 10; i++) {
      wtx = session.beginWriteTransaction();
      wtx.moveToDocumentRoot();
      wtx.insertTextAsFirstChild(UTF.convert(Integer.toString(i)));
      wtx.commit();
      wtx.close();

      rtx = session.beginReadTransaction();
      rtx.moveToDocumentRoot();
      rtx.moveToFirstChild();
      assertEquals(Integer.toString(i), new String(rtx.getValue()));
      assertEquals(i + 2L, rtx.getRevisionSize());
      assertEquals(i, rtx.getRevisionNumber());
      rtx.close();
    }

    rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();
    rtx.moveToFirstChild();
    assertEquals(Integer.toString(10), new String(rtx.getValue()));
    assertEquals(12L, rtx.getRevisionSize());
    assertEquals(10L, rtx.getRevisionNumber());
    rtx.close();

    session.close();

  }

  @Test
  public void testInsertPath() throws IOException {

    final ISession session = Session.beginSession(PATH);

    IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();
    wtx.close();

    wtx = session.beginWriteTransaction();
    TestCase.assertNotNull(wtx.moveToDocumentRoot());
    assertEquals(2L, wtx.insertElementAsFirstChild("", "", ""));

    assertEquals(3L, wtx.insertElementAsFirstChild("", "", ""));
    assertEquals(4L, wtx.insertElementAsFirstChild("", "", ""));

    TestCase.assertNotNull(wtx.moveToParent());
    assertEquals(5L, wtx.insertElementAsRightSibling("", "", ""));

    wtx.commit();
    wtx.close();

    final IWriteTransaction wtx2 = session.beginWriteTransaction();

    TestCase.assertNotNull(wtx2.moveToDocumentRoot());
    assertEquals(6L, wtx2.insertElementAsFirstChild("", "", ""));

    wtx2.commit();
    wtx2.close();

    session.close();
  }

  @Test
  public void testPageBoundary() throws IOException {

    ISession session = Session.beginSession(PATH);

    // Document root.
    IWriteTransaction wtx = session.beginWriteTransaction();

    for (int i = 0; i < 256 * 256 + 1; i++) {
      wtx.insertTextAsFirstChild(UTF.EMPTY);
    }

    TestCase.assertNotNull(wtx.moveTo(2L));
    assertEquals(2L, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testRemoveDocument() throws IOException {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    try {
      wtx.moveToDocumentRoot();
      wtx.remove();
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }

    wtx.abort();
    wtx.close();
    session.close();
  }

  @Test
  public void testRemoveDescendant() throws IOException {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    wtx.remove();

    wtx.abort();
    wtx.close();
    session.close();
  }

}
