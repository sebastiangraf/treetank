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
import org.treetank.utils.TestDocument;

public class MinimumCommitTest {

  public static final String TEST_PATH =
      "generated" + File.separator + "MinimumCommitTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(TEST_PATH).delete();
  }

  @Test
  public void test() throws IOException {

    ISession session = Session.beginSession(TEST_PATH);
    IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionNumber());
    wtx.commit();

    wtx.close();
    session.close();

    session = Session.beginSession(TEST_PATH);
    wtx = session.beginWriteTransaction();
    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();

    wtx = session.beginWriteTransaction();
    TestCase.assertEquals(2L, wtx.getRevisionNumber());
    wtx.commit();
    wtx.close();

    IReadTransaction rtx = session.beginReadTransaction();
    TestCase.assertEquals(2L, rtx.getRevisionNumber());
    rtx.close();
    session.close();

  }

  @Test
  public void testTimestamp() throws IOException {

    ISession session = Session.beginSession(TEST_PATH);
    IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionTimestamp());
    wtx.commit();
    wtx.close();

    IReadTransaction rtx = session.beginReadTransaction();
    if (rtx.getRevisionTimestamp() >= System.currentTimeMillis()) {
      TestCase.fail("Committed revision timestamp must be smaller than now.");
    }
    rtx.close();

    session.close();

  }

}
