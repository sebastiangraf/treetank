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
 * $Id: $
 */

package org.treetank.xpath.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.XPathAxis;
import org.treetank.xpath.functions.XPathError;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 */
public class OrExprTest {

  public static final String PATH = "generated" + File.separator
      + "OrExprTest.tnk";

  IAxis ifExpr, thenExpr, elseExpr;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

  }
  
  @Test
  public void testOr() {
    
    final ISession session = Session.beginSession(PATH);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    
    long iTrue = rtx.getItemList().addItem(new AtomicValue(true));
    long iFalse = rtx.getItemList().addItem(new AtomicValue(false));
    
    IAxis trueLit1 = new LiteralExpr(rtx, iTrue);
    IAxis trueLit2 = new LiteralExpr(rtx, iTrue);
    IAxis falseLit1 = new LiteralExpr(rtx, iFalse);
    IAxis falseLit2 = new LiteralExpr(rtx, iFalse);
    
    IAxis axis1 = new OrExpr(rtx, trueLit1, trueLit2);
    assertEquals(true, axis1.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());
    
    IAxis axis2 = new OrExpr(rtx, trueLit1, falseLit1);
    assertEquals(true, axis2.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());
    
    IAxis axis3 = new OrExpr(rtx, falseLit1, trueLit1);
    assertEquals(true, axis3.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());
    
    IAxis axis4 = new OrExpr(rtx, falseLit1, falseLit2);
    assertEquals(true, axis4.hasNext());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis4.hasNext());
    
    rtx.close();
    session.close();
  }

  @Test
  public void testOrQuery() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    rtx.moveTo(2L);

    final IAxis axis1 = new XPathAxis(rtx, "text() or node()");
    assertEquals(true, axis1.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new XPathAxis(rtx, "comment() or node()");
    assertEquals(true, axis2.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    final IAxis axis3 = new XPathAxis(rtx, "1 eq 1 or 2 eq 2");
    assertEquals(true, axis3.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());

    final IAxis axis4 = new XPathAxis(rtx, "1 eq 1 or 2 eq 3");
    assertEquals(true, axis4.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis4.hasNext());

    final IAxis axis5 = new XPathAxis(rtx, "1 eq 2 or (3 idiv 0 = 1)");
    try {
      assertEquals(true, axis5.hasNext());
      assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
      assertEquals(false, axis5.hasNext());
      fail("Exprected XPathError");
    } catch (XPathError e) {
      assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
    }

    final IAxis axis6 = new XPathAxis(rtx, "1 eq 1 or (3 idiv 0 = 1)");
    assertEquals(true, axis6.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
      
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}
