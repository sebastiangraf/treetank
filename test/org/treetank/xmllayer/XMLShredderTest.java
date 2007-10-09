/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.nodelayer.SessionConfiguration;
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
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

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
          new String(expectedTrx.getValue(), IConstants.DEFAULT_ENCODING),
          new String(trx.getValue(), IConstants.DEFAULT_ENCODING));
    }

    expectedSession.close();
    session.close();

  }

}
