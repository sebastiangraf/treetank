/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.xmllayer.ChildAxisIterator;
import org.treetank.xmllayer.IAxisIterator;


public class ChildAxisIteratorTest {

  public static final String TEST_ITERATE_PATH =
      "generated/ChildAxitIteratorTest_Iterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "generated/ChildAxitIteratorTest_Persistent.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_ITERATE_PATH).delete();
    new File(TEST_PERSISTENT_PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session = new Session(TEST_ITERATE_PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    trx.moveTo(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator1 = new ChildAxisIterator(trx);
    assertEquals(true, childIterator1.next());
    assertEquals(2L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(3, trx.getKind());
    assertEquals("", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("oops1", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(1, trx.getKind());
    assertEquals("b", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(6L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(3, trx.getKind());
    assertEquals("", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("oops2", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(1, trx.getKind());
    assertEquals("b", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx
        .getNodeKey());
    assertEquals(3, trx.getKind());
    assertEquals("", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("oops3", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(false, childIterator1.next());

    trx.moveTo(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator2 = new ChildAxisIterator(trx);
    assertEquals(true, childIterator2.next());
    assertEquals(4L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(3, trx.getKind());
    assertEquals("", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("foo", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator2.next());
    assertEquals(5L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(1, trx.getKind());
    assertEquals("c", trx.nameForKey(trx.getLocalPartKey()));
    assertEquals("", trx.nameForKey(trx.getURIKey()));
    assertEquals("", trx.nameForKey(trx.getPrefixKey()));
    assertEquals("", new String(trx.getValue(), IConstants.ENCODING));

    assertEquals(false, childIterator2.next());

    trx.moveTo(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator4 = new ChildAxisIterator(trx);
    assertEquals(false, childIterator4.next());

    session.abort();
    session.close();

  }

  @Test
  public void testPersistent() throws Exception {

    final ISession session = new Session(TEST_PERSISTENT_PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);
    session.commit();

    final ISession session1 = new Session(TEST_PERSISTENT_PATH);
    final IReadTransaction rTrx = session1.beginReadTransaction();

    rTrx.moveTo(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator1 = new ChildAxisIterator(rTrx);
    assertEquals(true, childIterator1.next());
    assertEquals(2L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(3, rTrx.getKind());
    assertEquals("", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("oops1", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(1, rTrx.getKind());
    assertEquals("b", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(6L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(3, rTrx.getKind());
    assertEquals("", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("oops2", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(1, rTrx.getKind());
    assertEquals("b", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator1.next());
    assertEquals(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(3, rTrx.getKind());
    assertEquals("", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("oops3", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(false, childIterator1.next());

    rTrx.moveTo(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator2 = new ChildAxisIterator(rTrx);
    assertEquals(true, childIterator2.next());
    assertEquals(4L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(3, rTrx.getKind());
    assertEquals("", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("foo", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(true, childIterator2.next());
    assertEquals(5L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, rTrx
        .getNodeKey());
    assertEquals(1, rTrx.getKind());
    assertEquals("c", rTrx.nameForKey(rTrx.getLocalPartKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getURIKey()));
    assertEquals("", rTrx.nameForKey(rTrx.getPrefixKey()));
    assertEquals("", new String(rTrx.getValue(), IConstants.ENCODING));

    assertEquals(false, childIterator2.next());

    rTrx.moveTo(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator childIterator4 = new ChildAxisIterator(rTrx);
    assertEquals(false, childIterator4.next());

    session1.close();

  }

}
