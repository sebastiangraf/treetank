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
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class ChildAxisIteratorTest {

  public static final String TEST_ITERATE_PATH =
      "generated" + File.separator + "ChildAxitIteratorTestIterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "generated" + File.separator + "ChildAxitIteratorTestPersistent.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_ITERATE_PATH).delete();
    new File(TEST_PERSISTENT_PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session = Session.getSession(TEST_ITERATE_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(1L);
    final IAxisIterator childIterator1 = new ChildAxisIterator(wtx);
    assertEquals(true, childIterator1.next());
    assertEquals(2L, wtx.getNodeKey());
    assertEquals(3, wtx.getKind());
    assertEquals("", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("oops1", new String(
        wtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(3L, wtx.getNodeKey());
    assertEquals(1, wtx.getKind());
    assertEquals("b", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("", new String(wtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(6L, wtx.getNodeKey());
    assertEquals(3, wtx.getKind());
    assertEquals("", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("oops2", new String(
        wtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(7L, wtx.getNodeKey());
    assertEquals(1, wtx.getKind());
    assertEquals("b", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("", new String(wtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(10L, wtx.getNodeKey());
    assertEquals(3, wtx.getKind());
    assertEquals("", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("oops3", new String(
        wtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(false, childIterator1.next());

    wtx.moveTo(3L);
    final IAxisIterator childIterator2 = new ChildAxisIterator(wtx);
    assertEquals(true, childIterator2.next());
    assertEquals(4L, wtx.getNodeKey());
    assertEquals(3, wtx.getKind());
    assertEquals("", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("foo", new String(wtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator2.next());
    assertEquals(5L, wtx.getNodeKey());
    assertEquals(1, wtx.getKind());
    assertEquals("c", wtx.nameForKey(wtx.getLocalPartKey()));
    assertEquals("", wtx.nameForKey(wtx.getURIKey()));
    assertEquals("", wtx.nameForKey(wtx.getPrefixKey()));
    assertEquals("", new String(wtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(false, childIterator2.next());

    wtx.moveTo(10L);
    final IAxisIterator childIterator4 = new ChildAxisIterator(wtx);
    assertEquals(false, childIterator4.next());

    wtx.abort();
    session.close();

  }

  @Test
  public void testPersistent() throws Exception {

    final ISession session = Session.getSession(TEST_PERSISTENT_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();

    final ISession session1 = Session.getSession(TEST_PERSISTENT_PATH);
    final IReadTransaction rtx = session1.beginReadTransaction();

    rtx.moveTo(1L);
    final IAxisIterator childIterator1 = new ChildAxisIterator(rtx);
    assertEquals(true, childIterator1.next());
    assertEquals(2L, rtx.getNodeKey());
    assertEquals(3, rtx.getKind());
    assertEquals("", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("oops1", new String(
        rtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(3L, rtx.getNodeKey());
    assertEquals(1, rtx.getKind());
    assertEquals("b", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("", new String(rtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(6L, rtx.getNodeKey());
    assertEquals(3, rtx.getKind());
    assertEquals("", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("oops2", new String(
        rtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(7L, rtx.getNodeKey());
    assertEquals(1, rtx.getKind());
    assertEquals("b", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("", new String(rtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(10L, rtx.getNodeKey());
    assertEquals(3, rtx.getKind());
    assertEquals("", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("oops3", new String(
        rtx.getValue(),
        IConstants.DEFAULT_ENCODING));

    assertEquals(false, childIterator1.next());

    rtx.moveTo(3L);
    final IAxisIterator childIterator2 = new ChildAxisIterator(rtx);
    assertEquals(true, childIterator2.next());
    assertEquals(4L, rtx.getNodeKey());
    assertEquals(3, rtx.getKind());
    assertEquals("", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("foo", new String(rtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(true, childIterator2.next());
    assertEquals(5L, rtx.getNodeKey());
    assertEquals(1, rtx.getKind());
    assertEquals("c", rtx.nameForKey(rtx.getLocalPartKey()));
    assertEquals("", rtx.nameForKey(rtx.getURIKey()));
    assertEquals("", rtx.nameForKey(rtx.getPrefixKey()));
    assertEquals("", new String(rtx.getValue(), IConstants.DEFAULT_ENCODING));

    assertEquals(false, childIterator2.next());

    rtx.moveTo(10L);
    final IAxisIterator childIterator4 = new ChildAxisIterator(rtx);
    assertEquals(false, childIterator4.next());

    rtx.close();
    session1.close();

  }

}
