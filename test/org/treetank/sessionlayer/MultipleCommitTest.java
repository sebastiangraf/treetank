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
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

public class MultipleCommitTest {

  public static final String PATH =
      "generated" + File.separator + "MultipleCommitTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void test() throws IOException {
    ISession session = Session.beginSession(PATH);
    IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionNumber());
    TestCase.assertEquals(2L, wtx.getRevisionSize());
    wtx.commit();

    wtx.insertElementAsFirstChild("foo", "", "");
    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestCase.assertEquals(3L, wtx.getRevisionSize());
    wtx.abort();

    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestCase.assertEquals(2L, wtx.getRevisionSize());
    wtx.close();

    session.close();
  }

}
