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
import org.treetank.xmllayer.DescendantAxisIterator;
import org.treetank.xmllayer.IAxisIterator;
import org.treetank.xmllayer.NameTestAxisIterator;


public class NameTestAxisIteratorTest {

  public static final String PATH = "generated/NameTestAxisIteratorTest.tnk";

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

    // Find descendants starting from nodeKey 0L (root).
    trx.moveTo(0L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator descendantIterator1 =
        new NameTestAxisIterator(trx, new DescendantAxisIterator(trx), "b");
    assertEquals(true, descendantIterator1.next());
    assertEquals(3L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());

    assertEquals(true, descendantIterator1.next());
    assertEquals(7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT, trx.getNodeKey());
    assertEquals(false, descendantIterator1.next());

    session.abort();
    session.close();

  }

}
