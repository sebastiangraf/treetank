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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xmllayer.DescendantAxisIterator;
import org.treetank.xmllayer.SAXHandler;
import org.xml.sax.InputSource;


public class SAXHandlerTest {

  public static final String PATH = "generated/SAXHandlerTest.tnk";

  public static final String EXPECTED_PATH =
      "generated/Expected_SAXHandlerTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
    new File(EXPECTED_PATH).delete();
  }

  @Test
  public void testIdefixSAXHandler() throws Exception {

    // Setup expected session.
    final ISession expectedSession = new Session(EXPECTED_PATH);
    final IWriteTransaction expectedTrx =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedTrx);

    // Setup parsed session.
    final ISession session = new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    parser.parse(inputSource, new SAXHandler(inputSource.getSystemId(), trx));

    expectedTrx.moveToRoot();
    trx.moveToRoot();
    final IAxisIterator expectedDescendants =
        new DescendantAxisIterator(expectedTrx);
    final IAxisIterator descendants = new DescendantAxisIterator(trx);

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

    expectedSession.abort();
    expectedSession.close();

    session.abort();
    session.close();

  }

}
