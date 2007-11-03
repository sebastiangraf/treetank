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
 * $Id: ParentAxisTest.java 3342 2007-10-30 10:35:51Z kramis $
 */

package org.treetank.axislayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.UTF;

public class FullTextAxisTest {

  public static final String PATH =
      "generated" + File.separator + "FullTextAxisTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild(UTF.convert("foo"));
    final long nodeKey2 = wtx.insertTextAsRightSibling(UTF.convert("foo"));
    final long tokenKey1 = wtx.index("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.index("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    // Verify axis
    final IAxis axis1 = new FullTextAxis(wtx, "foo");
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey1, axis1.next().getNodeKey());
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey2, axis1.next().getNodeKey());
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new FullTextAxis(wtx, "bar");
    assertEquals(false, axis2.hasNext());

    try {
      final IAxis axis3 = new FullTextAxis(wtx, null);
      assertEquals(false, axis3.hasNext());
      TestCase.fail();
    } catch (Exception e) {
      // Must catch.
    }

    try {
      final IAxis axis3 = new FullTextAxis(wtx, "");
      assertEquals(false, axis3.hasNext());
      TestCase.fail();
    } catch (Exception e) {
      // Must catch.
    }

    wtx.abort();
    wtx.close();
    session.close();

  }

}
