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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.xml.sax.InputSource;

public class SAXHandlerTest {

  public static final String PATH =
      "generated" + File.separator + "SAXHandlerTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedSAXHandlerTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
    new File(EXPECTED_PATH).delete();
  }

  @Test
  public void testSAXHandler() throws Exception {

    // Setup expected session.
    final ISession expectedSession = new Session(EXPECTED_PATH);
    final IWriteTransaction expectedWTX =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedWTX);

    // Setup parsed session.
    final ISession session = new Session(PATH);
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    parser.parse(inputSource, new SAXHandler(session));

    final IReadTransaction rtx = session.beginReadTransaction();

    expectedWTX.moveToRoot();
    rtx.moveToRoot();
    final IAxisIterator expectedDescendants =
        new DescendantAxisIterator(expectedWTX);
    final IAxisIterator descendants = new DescendantAxisIterator(rtx);

    while (expectedDescendants.next() && descendants.next()) {
      assertEquals(expectedWTX.getNodeKey(), rtx.getNodeKey());
      assertEquals(expectedWTX.getParentKey(), rtx.getParentKey());
      assertEquals(expectedWTX.getFirstChildKey(), rtx.getFirstChildKey());
      assertEquals(expectedWTX.getLeftSiblingKey(), rtx.getLeftSiblingKey());
      assertEquals(expectedWTX.getRightSiblingKey(), rtx.getRightSiblingKey());
      assertEquals(expectedWTX.getChildCount(), rtx.getChildCount());
      assertEquals(expectedWTX.getKind(), rtx.getKind());
      assertEquals(expectedWTX.nameForKey(expectedWTX.getLocalPartKey()), rtx
          .nameForKey(rtx.getLocalPartKey()));
      assertEquals(expectedWTX.nameForKey(expectedWTX.getURIKey()), rtx
          .nameForKey(rtx.getURIKey()));
      assertEquals(expectedWTX.nameForKey(expectedWTX.getPrefixKey()), rtx
          .nameForKey(rtx.getPrefixKey()));
      assertEquals(new String(
          expectedWTX.getValue(),
          IConstants.DEFAULT_ENCODING), new String(
          rtx.getValue(),
          IConstants.DEFAULT_ENCODING));
    }

    expectedWTX.abort();
    expectedSession.close();

    session.close();

  }

}
