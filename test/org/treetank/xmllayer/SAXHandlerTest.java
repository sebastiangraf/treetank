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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.xml.sax.InputSource;

public class SAXHandlerTest {

  public static final String PATH =
      "generated" + File.separator + "SAXHandlerTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedSAXHandlerTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(PATH).delete();
    new File(EXPECTED_PATH).delete();
  }

  @Test
  public void testSAXHandler() throws Exception {

    // Setup expected session.
    final ISession expectedSession = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction expectedWTX =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedWTX);
    expectedWTX.commit();
    //    expectedWTX.close();

    // Setup parsed session.
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    final ISession session = Session.beginSession(new File(PATH));
    final IWriteTransaction wrtx = session.beginWriteTransaction();
    parser.parse(inputSource, new SAXHandler(wrtx));
    wrtx.commit();
    //    wrtx.close();

    final IReadTransaction expectedTrx = expectedSession.beginReadTransaction();
    final IReadTransaction rtrx2 = session.beginReadTransaction();

    final AbstractAxis expectedDescendants = new DescendantAxis(expectedTrx);
    final AbstractAxis descendants = new DescendantAxis(rtrx2);

    while (expectedDescendants.hasNext() && descendants.hasNext()) {
      if (!expectedDescendants.next().equals(descendants.next())) {
        fail(expectedDescendants.getCurrentNode().toString()
            + " and "
            + descendants.getCurrentNode()
            + " are not the same!");
      }
    }

    expectedTrx.close();
    expectedSession.close();

    rtrx2.close();
    session.close();

  }
}
