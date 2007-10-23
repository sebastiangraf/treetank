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
 * $Id$
 */

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class ParentAxisTest {

  public static final String PATH =
      "generated" + File.separator + "ParentAxisTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(3L);
    final Iterator<INode> axis1 = new ParentAxis(wtx);
    assertEquals(true, axis1.hasNext());
    assertEquals(1L, wtx.getNodeKey());

    assertEquals(false, axis1.hasNext());

    wtx.moveTo(7L);
    final Iterator<INode> axis2 = new ParentAxis(wtx);
    assertEquals(true, axis2.hasNext());
    assertEquals(1L, wtx.getNodeKey());

    assertEquals(false, axis2.hasNext());

    wtx.abort();
    session.close();

  }

}
