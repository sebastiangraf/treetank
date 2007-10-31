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

package org.treetank.axislayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class AttributeAxisTest {

  public static final String PATH =
      "generated" + File.separator + "AttributeAxisTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(0L);
    final IAxis axis1 = new AttributeAxis(wtx);

    assertEquals(false, axis1.hasNext());

    wtx.moveTo(2L);
    final IAxis axis2 = new AttributeAxis(wtx);
    assertEquals(true, axis2.hasNext());
    INode node = axis2.next();
    assertEquals(2L, node.getNodeKey());

    assertEquals(false, axis2.hasNext());

    wtx.moveTo(8L);
    final IAxis axis4 = new AttributeAxis(wtx);
    assertEquals(true, axis4.hasNext());
    node = axis4.next();
    assertEquals(8L, node.getNodeKey());

    assertEquals(false, axis4.hasNext());

    wtx.moveTo(11L);
    final IAxis axis5 = new AttributeAxis(wtx);
    assertEquals(false, axis5.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
