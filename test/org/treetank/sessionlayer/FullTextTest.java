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
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

public class FullTextTest {

  public static final String PATH =
      "generated" + File.separator + "FullTextTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testInsertChild() throws IOException {

    ISession session = Session.beginSession(PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.moveToFullTextRoot();
    TestCase.assertEquals(2L, wtx.insertFullTextAsFirstChild(13));
    TestCase.assertEquals(3L, wtx.insertFullTextAsFirstChild(14));
    TestCase.assertEquals(4L, wtx.insertFullTextAsRightSibling(15));
    TestCase.assertEquals(5L, wtx.insertFullTextAsRightSibling(16));
    TestCase.assertEquals(6L, wtx.insertFullTextAsFirstChild(17));
    wtx.moveToParent();
    TestCase.assertEquals(7L, wtx.insertFullTextAsFirstChild(18));
    wtx.commit();
    wtx.close();
    session.close();

    session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();

    rtx.moveToFullTextRoot();
    TestCase.assertEquals(IConstants.FULLTEXT_ROOT_KEY, rtx.getNodeKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getParentKey());
    TestCase.assertEquals(2L, rtx.getFirstChildKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(13, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(true, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(2L, rtx.getNodeKey());
    TestCase.assertEquals(IConstants.FULLTEXT_ROOT_KEY, rtx.getParentKey());
    TestCase.assertEquals(3L, rtx.getFirstChildKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(14, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(3L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getFirstChildKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getLeftSiblingKey());
    TestCase.assertEquals(4L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(15, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(4L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getFirstChildKey());
    TestCase.assertEquals(3L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(5L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(16, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(true, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(5L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(7L, rtx.getFirstChildKey());
    TestCase.assertEquals(4L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(18, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(7L, rtx.getNodeKey());
    TestCase.assertEquals(5L, rtx.getParentKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getFirstChildKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getLeftSiblingKey());
    TestCase.assertEquals(6L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(17, rtx.getLocalPartKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(6L, rtx.getNodeKey());
    TestCase.assertEquals(5L, rtx.getParentKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getFirstChildKey());
    TestCase.assertEquals(7L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IConstants.NULL_KEY, rtx.getRightSiblingKey());

    rtx.close();

    session.close();
  }

}
