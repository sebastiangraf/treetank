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

package org.treetank.xpath.expr;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 * 
 */
public class FunctionAxisTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "FunctionAxisTest.tnk";

  IAxis ifExpr, thenExpr, elseExpr;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

  }

  @Test
  public void testFunctions() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    rtx.moveTo(2L);

    final IAxis axis1 = new XPathAxis(rtx, "fn:count(text())");
    assertEquals(true, axis1.hasNext());
    assertEquals(3, (int) Double.parseDouble(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new XPathAxis(rtx, "fn:count(//node())");
    assertEquals(true, axis2.hasNext());
    assertEquals(10, (int) Double.parseDouble(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    final IAxis axis3 = new XPathAxis(rtx, "fn:string(//node())");
    assertEquals(true, axis3.hasNext());
    assertEquals(
        "oops1 foo oops2 bar oops3 oops1 foo oops2 bar oops3 foo bar",
        rtx.getValue());
    assertEquals(false, axis3.hasNext());

    final IAxis axis4 = new XPathAxis(rtx, "fn:string()");
    assertEquals(true, axis4.hasNext());
    assertEquals("oops1 foo oops2 bar oops3", rtx.getValue());
    assertEquals(false, axis4.hasNext());

    //    rtx.moveToAttribute(0);
    //    final IAxis axis5 = new XPathAxis(rtx, "fn:string()");
    //    assertEquals(true, axis5.hasNext());
    //    assertEquals("j", rtx.getValue());
    //    assertEquals(false, axis5.hasNext());

    final IAxis axis5 =
        new XPathAxis(rtx, "fn:string(./attribute::attribute())");
    assertEquals(true, axis5.hasNext());
    assertEquals("j", rtx.getValue());
    assertEquals(false, axis5.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}
