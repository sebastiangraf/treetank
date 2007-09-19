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
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;

public class XMLShredderTest {

  public static final String XML = "xml/test.xml";

  public static final String PATH = "generated/XMLShredderTest.tnk";

  public static final String EXPECTED_PATH =
      "generated/Expected_XMLShredderTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
    new File(EXPECTED_PATH).delete();
  }

  @Test
  public void testSTAXShredder() throws Exception {

    // Setup expected session.
    final ISession expectedSession = new Session(EXPECTED_PATH);
    final IWriteTransaction expectedTrx =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedTrx);
    expectedSession.commit();
    expectedTrx.moveToRoot();

    // Setup parsed session.
    XMLShredder.shred(XML, PATH);

    // Verify.
    final ISession session = new Session(PATH);
    final IReadTransaction trx = session.beginReadTransaction();
    trx.moveToRoot();
    final IAxisIterator expectedDescendants =
        new DescendantAxisIterator(expectedTrx);
    final IAxisIterator descendants = new DescendantAxisIterator(trx);

    assertEquals(expectedTrx.revisionSize(), trx.revisionSize());
    while (expectedDescendants.next() && descendants.next()) {
      assertEquals(expectedTrx.getNodeKey(), trx.getNodeKey());
      assertEquals(expectedTrx.getParentKey(), trx.getParentKey());
      assertEquals(expectedTrx.getFirstChildKey(), trx.getFirstChildKey());
      assertEquals(expectedTrx.getLeftSiblingKey(), trx.getLeftSiblingKey());
      assertEquals(expectedTrx.getRightSiblingKey(), trx.getRightSiblingKey());
      assertEquals(expectedTrx.getChildCount(), trx.getChildCount());
      assertEquals(expectedTrx.getKind(), trx.getKind());
      assertEquals(expectedTrx.nameForKey(expectedTrx.getLocalPartKey()), trx
          .nameForKey(trx.getLocalPartKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getURIKey()), trx
          .nameForKey(trx.getURIKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getPrefixKey()), trx
          .nameForKey(trx.getPrefixKey()));
      assertEquals(
          new String(expectedTrx.getValue(), IConstants.ENCODING),
          new String(trx.getValue(), IConstants.ENCODING));
    }

    expectedSession.close();
    session.close();

  }

}
