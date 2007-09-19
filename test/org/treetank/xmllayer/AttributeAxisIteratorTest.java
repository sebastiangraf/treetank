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
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.xmllayer.AttributeAxisIterator;
import org.treetank.xmllayer.IAxisIterator;


public class AttributeAxisIteratorTest {

  public static final String PATH = "generated/AttributeAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    final ISession session =
        new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    trx.moveTo(0L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator attributeIterator1 = new AttributeAxisIterator(trx);

    assertEquals(false, attributeIterator1.next());

    trx.moveTo(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator attributeIterator2 = new AttributeAxisIterator(trx);
    assertEquals(true, attributeIterator2.next());
    assertEquals((1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT) + 1, trx.getNodeKey());

    assertEquals(false, attributeIterator2.next());

    trx.moveTo(7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator attributeIterator4 = new AttributeAxisIterator(trx);
    assertEquals(true, attributeIterator4.next());
    assertEquals((7L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT) + 1, trx.getNodeKey());

    assertEquals(false, attributeIterator4.next());

    trx.moveTo(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
    final IAxisIterator attributeIterator5 = new AttributeAxisIterator(trx);
    assertEquals(false, attributeIterator5.next());

    session.abort();
    session.close();

  }

}
