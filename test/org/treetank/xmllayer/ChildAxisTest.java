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
import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class ChildAxisTest {

  public static final String TEST_ITERATE_PATH =
      "generated" + File.separator + "ChildAxisTestIterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "generated" + File.separator + "ChildAxisTestPersistent.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_ITERATE_PATH).delete();
    new File(TEST_PERSISTENT_PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session = Session.beginSession(TEST_ITERATE_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(1L);
    final Iterator<INode> axis1 = new ChildAxis(wtx);
    assertEquals(true, axis1.hasNext());
    INode node = axis1.next();
    assertEquals(2L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops1", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(3L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("b", wtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(node.getURIKey()));
    assertEquals("", wtx.nameForKey(node.getPrefixKey()));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(6L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops2", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(7L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("b", wtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(node.getURIKey()));
    assertEquals("", wtx.nameForKey(node.getPrefixKey()));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(10L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops3", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(false, axis1.hasNext());

    wtx.moveTo(3L);
    final Iterator<INode> axis2 = new ChildAxis(wtx);
    assertEquals(true, axis2.hasNext());
    node = axis2.next();
    assertEquals(4L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals(
        "foo",
        new String(node.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis2.hasNext());
    node = axis2.next();
    assertEquals(5L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("c", wtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(node.getURIKey()));
    assertEquals("", wtx.nameForKey(node.getPrefixKey()));

    assertEquals(false, axis2.hasNext());

    wtx.moveTo(10L);
    final Iterator<INode> axis4 = new ChildAxis(wtx);
    assertEquals(false, axis4.hasNext());

    wtx.abort();
    session.close();

  }

  @Test
  public void testPersistent() throws Exception {

    final ISession session = Session.beginSession(TEST_PERSISTENT_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    session.close();

    final ISession session1 = Session.beginSession(TEST_PERSISTENT_PATH);
    final IReadTransaction rtx = session1.beginReadTransaction();

    rtx.moveTo(1L);
    final Iterator<INode> axis1 = new ChildAxis(rtx);
    assertEquals(true, axis1.hasNext());
    INode node = axis1.next();
    assertEquals(2L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops1", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(3L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("b", rtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(node.getURIKey()));
    assertEquals("", rtx.nameForKey(node.getPrefixKey()));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(6L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops2", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(7L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("b", rtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(node.getURIKey()));
    assertEquals("", rtx.nameForKey(node.getPrefixKey()));

    assertEquals(true, axis1.hasNext());
    node = axis1.next();
    assertEquals(10L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals("oops3", new String(
        node.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(false, axis1.hasNext());

    rtx.moveTo(3L);
    final Iterator<INode> axis2 = new ChildAxis(rtx);
    assertEquals(true, axis2.hasNext());
    node = axis2.next();
    assertEquals(4L, node.getNodeKey());
    assertEquals(3, node.getKind());
    assertEquals(
        "foo",
        new String(node.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, axis2.hasNext());
    node = axis2.next();
    assertEquals(5L, node.getNodeKey());
    assertEquals(1, node.getKind());
    assertEquals("c", rtx.nameForKey(node.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(node.getURIKey()));
    assertEquals("", rtx.nameForKey(node.getPrefixKey()));

    assertEquals(false, axis2.hasNext());

    rtx.moveTo(10L);
    final Iterator<INode> axis4 = new ChildAxis(rtx);
    assertEquals(false, axis4.hasNext());

    rtx.close();
    session1.close();

  }

}
