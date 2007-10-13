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

  public static final String TEST_PATH = "generated/UpdateTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_PATH).delete();
  }

//  @Test
//  public void testInsertChild() throws Exception {
//
//    ISession session = new Session(TEST_PATH);
//
//    // Document root.
//    IWriteTransaction trx = session.beginWriteTransaction();
//    trx.insertRoot("test");
//    session.commit();
//
//    IReadTransaction rTrx = session.beginReadTransaction();
//    assertEquals(1L, rTrx.revisionSize());
//    assertEquals(1L, rTrx.revisionKey());
//
//    // Insert 100 children.
//    for (int i = 1; i <= 10; i++) {
//      session = new Session(TEST_PATH);
//      trx = session.beginWriteTransaction();
//      trx.moveToRoot();
//      trx.insertFirstChild(IConstants.TEXT, "", "", "", UTF.convert(Integer
//          .toString(i)));
//      session.commit();
//      session.close();
//
//      session = new Session(TEST_PATH);
//      rTrx = session.beginReadTransaction();
//      rTrx.moveToRoot();
//      rTrx.moveToFirstChild();
//      assertEquals(Integer.toString(i), new String(rTrx.getValue()));
//      assertEquals(i + 1L, rTrx.revisionSize());
//      assertEquals(i + 1L, rTrx.revisionKey());
//    }
//
//    session = new Session(TEST_PATH);
//    rTrx = session.beginReadTransaction();
//    rTrx.moveToRoot();
//    rTrx.moveToFirstChild();
//    assertEquals(Integer.toString(10), new String(rTrx.getValue()));
//    assertEquals(11L, rTrx.revisionSize());
//    assertEquals(11L, rTrx.revisionKey());
//    session.close();
//
//  }

  @Test
  public void testInsertPath() throws Exception {

    final ISession session = new Session(TEST_PATH);

    IWriteTransaction wtx = session.beginWriteTransaction();
    assertEquals(0L, wtx.insertRoot("foo"));
    session.commit();
    
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

    session.commit();

    final IWriteTransaction wtx2 = session.beginWriteTransaction();

    assertEquals(true, wtx2.moveToRoot());
    assertEquals(5L, wtx.insertFirstChild(
        IConstants.ELEMENT,
        "",
        "",
        "",
        UTF.EMPTY));

    session.commit();

    session.close();
  }

}
