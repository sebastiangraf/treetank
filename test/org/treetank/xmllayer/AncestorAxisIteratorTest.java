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
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.xmllayer.AncestorAxisIterator;
import org.treetank.xmllayer.IAxisIterator;


public class AncestorAxisIteratorTest {

  public static final String PATH = "generated/AncestorAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    // Build simple test tree.
    final ISession session = new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    // Find ancestors starting from nodeKey 0L (root).
    trx.moveTo(8L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator ancestorIterator1 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator1.next());
    assertEquals(7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());

    assertEquals(true, ancestorIterator1.next());
    assertEquals(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());

    assertEquals(false, ancestorIterator1.next());

    // Find ancestors starting from nodeKey 1L (first child of root).
    trx.moveTo(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator ancestorIterator2 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator2.next());
    assertEquals(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());

    assertEquals(false, ancestorIterator2.next());

    // Find ancestors starting from nodeKey 4L (second child of root).
    trx.moveTo(2L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator ancestorIterator3 = new AncestorAxisIterator(trx);
    assertEquals(true, ancestorIterator3.next());
    assertEquals(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());

    assertEquals(false, ancestorIterator3.next());

    // Find ancestors starting from nodeKey 5L (last in document order).
    trx.moveTo(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator ancestorIterator4 = new AncestorAxisIterator(trx);
    assertEquals(false, ancestorIterator4.next());

    session.abort();
    session.close();

  }

}
