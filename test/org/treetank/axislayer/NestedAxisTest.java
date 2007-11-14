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

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class NestedAxisTest {

  public static final String PATH =
      "generated" + File.separator + "ChainedAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testChainedAxisTest() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /a/b/text():
    // Part: /a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("a")));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("b")));
    // Part: /text()
    final IAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter());
    // Part: /a/b/text():
    final IAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

    Assert.assertEquals(true, axis.hasNext());
    Assert.assertEquals(5L, axis.next());
    Assert.assertEquals(true, axis.hasNext());
    Assert.assertEquals(10L, axis.next());
    Assert.assertEquals(false, axis.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testChainedAxisTest2() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /a/b/@x:
    // Part: /a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("a")));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("b")));
    // Part: /@x
    final IAxis attributeX =
        new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx
            .keyForName("x")));
    // Part: /a/b/@x:
    final IAxis axis =
        new NestedAxis(new NestedAxis(childA, childB), attributeX);

    Assert.assertEquals(true, axis.hasNext());
    Assert.assertEquals(8L, axis.next());
    Assert.assertEquals(false, axis.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
