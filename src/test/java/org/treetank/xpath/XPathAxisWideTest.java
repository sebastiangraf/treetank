/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xpath;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.xmllayer.XMLShredder;

public class XPathAxisWideTest {

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "factbook.xml";

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "XPathWideTest.tnk";

  public static final String XML2 =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "shakespeare.xml";

  public static final String PATH2 =
      "target" + File.separator + "tnk" + File.separator + "XPathShakeTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
    Session.removeSession(PATH2);

  }

  @Test
  public void testIterateFactbook() throws IOException, XMLStreamException {
    // Setup parsed session.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/mondial/continent[@id]"),
        new long[] { 3L, 4L, 5L, 6L, 7L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "mondial/continent[@name]"), new long[] { 3L, 4L, 5L, 6L, 7L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "mondial/continent[@id=\"f0_119\"]"), new long[] { 3L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "/mondial/continent[@name = \"Africa\"]"), new long[] { 7L });

    final IAxis axis5 = new XPathAxis(rtx, "mondial/lake/node()");
    for (int i = 0; i < 61; i++) {
      assertEquals(true, axis5.hasNext());
    }
    //    assertEquals(29891L, axis5.next());
    assertEquals(false, axis5.hasNext());

    final IAxis axis6 = new XPathAxis(rtx, "mondial/country/religions/node()");
    for (int i = 0; i < 446; i++) {
      assertEquals(true, axis6.hasNext());
      axis6.next();
    }
    assertEquals(false, axis6.hasNext());

    final IAxis axis7 =
        new XPathAxis(rtx, "child::mondial/child::lake/child::node()");
    for (int i = 0; i < 60; i++) {
      assertEquals(true, axis7.hasNext());
      axis7.next();
    }
    assertEquals(true, axis7.hasNext());
    //  assertEquals(29891L, axis7.next());
    assertEquals(false, axis7.hasNext());

    final IAxis axis8 = new XPathAxis(rtx, "//*[@id]");
    for (int i = 0; i < 5562; i++) {
      assertEquals(true, axis8.hasNext());
      axis8.next();
    }
    assertEquals(false, axis8.hasNext());

    final IAxis axis9 =
        new XPathAxis(rtx, "/mondial/country/attribute::car_code");
    for (int i = 0; i < 194; i++) {
      assertEquals(true, axis9.hasNext());
      axis9.next();
    }
    assertEquals(false, axis9.hasNext());

    final IAxis axis10 = new XPathAxis(rtx, "//country[@*]");
    for (int i = 0; i < 231; i++) {
      assertEquals(true, axis10.hasNext());
      axis10.next();
    }
    assertEquals(false, axis10.hasNext());

    rtx.close();
    session.close();

  }

  ////  lasts too long
  //    @Test
  //    public void testIterateShakespreare() throws IOException, XMLStreamException {
  //   // Setup parsed session.
  //      XMLShredder.shred(XML2, new SessionConfiguration(PATH2));
  //  
  //      // Verify.
  //      final ISession session2 = Session.beginSession(PATH2);
  //      final IReadTransaction rtx2 = session2.beginReadTransaction(new ItemList());
  //      rtx2.moveToDocumentRoot();
  //  
  //      XPathStringTest.testIAxisConventions(new XPathAxis(rtx2, "fn:count(//PLAY)"), 
  //          new String[]{"37"});
  //      
  //      rtx2.moveTo(3L);
  //      XPathStringTest.testIAxisConventions(new XPathAxis(rtx2, "fn:count(TITLE)"), 
  //          new String[]{"1"});
  //      
  //      XPathStringTest.testIAxisConventions(new XPathAxis(rtx2, "fn:string(TITLE)"), 
  //          new String[]{"The Tragedy of Antony and Cleopatra"});
  //          
  //          XPathStringTest.testIAxisConventions(new XPathAxis(rtx2, "fn:count(//*)"), 
  //              new String[]{"179619"});
  //          
  //          
  //          XPathStringTest.testIAxisConventions(new XPathAxis(rtx2, "fn:count(/PLAYS/PLAY/PERSONAE)"), 
  //              new String[]{"37"});
  //  //    
  //  //    final IAxis axis1 = new XPathAxis(rtx2, "//PERSONA");
  //  //    for (int i = 0; i < 969; i++) {
  //  //      assertEquals(true, axis1.hasNext());
  //  //      axis1.next();
  //  //    }
  //  //    assertEquals(false, axis1.hasNext());
  //  //    
  //  //  
  //  //    
  //  //    final IAxis axis2 = new XPathAxis(rtx2, "/PLAYS/PLAY//SPEECH");
  //  //    for (int i = 0; i < 31014; i++) {
  //  //      assertEquals(true, axis2.hasNext());
  //  //      axis2.next();
  //  //    }
  //  //    assertEquals(false, axis2.hasNext());
  //  //    
  //  //    final IAxis axis3 = new XPathAxis(rtx2, "PLAYS/PLAY//STAGEDIR");
  //  //    for (int i = 0; i < 6257; i++) {
  //  //      assertEquals(true, axis3.hasNext());
  //  //      axis3.next();
  //  //    }
  //  //    assertEquals(false, axis3.hasNext());
  //  //    
  //  //    
  //  //    final IAxis axis4 = new XPathAxis(rtx2, "PLAYS/PLAY//STAGEDIR/text()");
  //  //    for (int i = 0; i < 6257; i++) {
  //  //      assertEquals(true, axis4.hasNext());
  //  //      axis4.next();
  //  //    }
  //  //    assertEquals(false, axis4.hasNext());
  //  //    
  //  //    final IAxis axis5 = new XPathAxis(rtx2, "/PLAYS/PLAY//SCNDESCR");
  //  //    for (int i = 0; i < 37; i++) {
  //  //      assertEquals(true, axis5.hasNext());
  //  //      axis5.next();
  //  //    }
  //  //    assertEquals(false, axis5.hasNext());
  //  //  
  //  //  final IAxis axis6 = new XPathAxis(rtx2, "//SPEECH/SPEAKER");
  //  //  for (int i = 0; i < 31067; i++) {
  //  //    assertEquals(true, axis6.hasNext());
  //  //    axis6.next();
  //  //  }
  //  //  assertEquals(false, axis6.hasNext());
  //  //  
  //  //  final IAxis axis7 = new XPathAxis(rtx2, "PLAYS/PLAY");
  //  //  for (int i = 0; i < 37; i++) {
  //  //    assertEquals(true, axis7.hasNext());
  //  //    axis7.next();
  //  //  }
  //  //  assertEquals(false, axis7.hasNext());
  //  //  
  //  //
  //      rtx2.close();
  //      session2.close();
  //    }
}
