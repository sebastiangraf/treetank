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

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.UTF;

public class UpdateTest {

  public static final String TEST_PATH =
      "generated" + File.separator + "UpdateTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_PATH).delete();
  }

  @Test
  public void testInsertChild() throws Exception {

    ISession session = Session.beginSession(TEST_PATH);

    // Document root.
    IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();

    IReadTransaction rtx = session.beginReadTransaction();
    assertEquals(1L, rtx.revisionSize());
    assertEquals(0L, rtx.revisionKey());
    rtx.close();

    // Insert 100 children.
    for (int i = 1; i <= 10; i++) {
      wtx = session.beginWriteTransaction();
      wtx.moveToRoot();
      wtx.insertFirstChild(IConstants.TEXT, "", "", "", UTF.convert(Integer
          .toString(i)));
      wtx.commit();

      rtx = session.beginReadTransaction();
      rtx.moveToRoot();
      rtx.moveToFirstChild();
      assertEquals(Integer.toString(i), new String(rtx.getValue()));
      assertEquals(i + 1L, rtx.revisionSize());
      assertEquals(i, rtx.revisionKey());
      rtx.close();
    }

    rtx = session.beginReadTransaction();
    rtx.moveToRoot();
    rtx.moveToFirstChild();
    assertEquals(Integer.toString(10), new String(rtx.getValue()));
    assertEquals(11L, rtx.revisionSize());
    assertEquals(10L, rtx.revisionKey());
    rtx.close();

    session.close();

  }

  @Test
  public void testInsertPath() throws Exception {

    final ISession session = Session.beginSession(TEST_PATH);

    IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.commit();

    wtx = session.beginWriteTransaction();
    assertEquals(true, wtx.moveToRoot());
    assertEquals(1L, wtx.insertFirstChild(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));

    assertEquals(2L, wtx.insertFirstChild(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));
    assertEquals(3L, wtx.insertFirstChild(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));

    assertEquals(true, wtx.moveToParent());
    assertEquals(4L, wtx.insertRightSibling(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));

    wtx.commit();

    final IWriteTransaction wtx2 = session.beginWriteTransaction();

    assertEquals(true, wtx2.moveToRoot());
    assertEquals(5L, wtx.insertFirstChild(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));

    wtx2.commit();

    session.close();
  }

  @Test
  public void testPageBoundary() throws Exception {

    ISession session = Session.beginSession(TEST_PATH);

    // Document root.
    IWriteTransaction wtx = session.beginWriteTransaction();

    for (int i = 0; i < 256 * 256 + 1; i++) {
      wtx.insertFirstChild("", "", "");
    }

    assertEquals(true, wtx.moveTo(0L));
    assertEquals(0L, wtx.getNodeKey());

    wtx.abort();
    session.close();

  }

}
