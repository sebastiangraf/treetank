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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

public class FullTextTest {

  public static final String TEST_PATH =
      "generated" + File.separator + "FullTextTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(TEST_PATH).delete();
  }

  @Test
  public void testInsertChild() throws IOException {

    final ISession session = Session.beginSession(TEST_PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.moveToFullTextRoot();
    wtx.insertFullTextAsFirstChild(13);
    wtx.setReferenceKey(23L);
    wtx.insertFullTextAsFirstChild(14);
    wtx.setReferenceKey(24L);
    wtx.insertFullTextAsRightSibling(15);
    wtx.setReferenceKey(25L);
    wtx.insertFullTextAsRightSibling(16);
    wtx.setReferenceKey(26L);
    wtx.commit();
    wtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();

    rtx.moveToFullTextRoot();
    TestCase.assertEquals(1L, rtx.getChildCount());

    rtx.moveToFirstChild();
    TestCase.assertEquals(3L, rtx.getChildCount());
    TestCase.assertEquals(13, rtx.getLocalPartKey());
    TestCase.assertEquals(23L, rtx.getReferenceKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(true, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(true, rtx.hasReference());

    rtx.moveToFirstChild();
    TestCase.assertEquals(0L, rtx.getChildCount());
    TestCase.assertEquals(14, rtx.getLocalPartKey());
    TestCase.assertEquals(24L, rtx.getReferenceKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(true, rtx.hasReference());

    rtx.moveToRightSibling();
    TestCase.assertEquals(0L, rtx.getChildCount());
    TestCase.assertEquals(15, rtx.getLocalPartKey());
    TestCase.assertEquals(25L, rtx.getReferenceKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(true, rtx.hasReference());

    rtx.moveToRightSibling();
    TestCase.assertEquals(0L, rtx.getChildCount());
    TestCase.assertEquals(16, rtx.getLocalPartKey());
    TestCase.assertEquals(26L, rtx.getReferenceKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(true, rtx.hasReference());

    rtx.close();

    session.close();
  }

}
