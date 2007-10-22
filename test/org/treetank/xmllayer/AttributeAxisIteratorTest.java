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

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class AttributeAxisIteratorTest {

  public static final String PATH =
      "generated" + File.separator + "AttributeAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session = Session.getSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(0L);
    final IAxisIterator attributeIterator1 = new AttributeAxisIterator(wtx);

    assertEquals(false, attributeIterator1.next());

    wtx.moveTo(1L);
    final IAxisIterator attributeIterator2 = new AttributeAxisIterator(wtx);
    assertEquals(true, attributeIterator2.next());
    assertEquals((1L) + 1, wtx.getNodeKey());

    assertEquals(false, attributeIterator2.next());

    wtx.moveTo(7L);
    final IAxisIterator attributeIterator4 = new AttributeAxisIterator(wtx);
    assertEquals(true, attributeIterator4.next());
    assertEquals((7L) + 1, wtx.getNodeKey());

    assertEquals(false, attributeIterator4.next());

    wtx.moveTo(10L);
    final IAxisIterator attributeIterator5 = new AttributeAxisIterator(wtx);
    assertEquals(false, attributeIterator5.next());

    wtx.abort();
    session.close();

  }

}
