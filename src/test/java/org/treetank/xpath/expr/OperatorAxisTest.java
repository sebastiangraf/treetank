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

import java.io.File;
//
//package org.treetank.extensions.xpath.functions;
//
//import static org.hamcrest.core.Is.is;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.treetank.api.IAxis;
//import org.treetank.api.ISession;
//import org.treetank.api.IWriteTransaction;
//import org.treetank.extensions.xpath.XPathAxis;
//import org.treetank.extensions.xpath.XPathReadTransaction;
//import org.treetank.sessionlayer.Session;
//import org.treetank.utils.TestDocument;
//
///**
// * JUnit-test class to test the functionality of the OperatorAxis.
// * 
// * @author Tina Scherer
// */
//public class OperatorAxisTest {
//
//  public static final String PATH = "target"
//    + File.separator
//    + "tnk"
//    + File.separator
//      + "OperatorAxisTest.tnk";
//
//  @Before
//  public void setUp() {
//
//    Session.removeSession(PATH);
//  }
//
//  @Test
//  public void testOps() throws IOException {
//
//    // Build simple test tree.
//    final ISession session = Session.beginSession(PATH);
//    final IWriteTransaction wtx = session.beginWriteTransaction();
//    TestDocument.create(wtx);
//    XPathReadTransaction xtx;
//
//    // Find descendants starting from nodeKey 0L (root).
//    wtx.moveToDocumentRoot();
//
//    final IAxis axis1 = new XPathAxis(wtx, "1.0 + 3.0");
//    xtx = (XPathReadTransaction) axis1.getTransaction();
//    assertEquals(true, axis1.hasNext());
//    assertThat(4.0, is(xtx.getValueAsDouble()));
//    assertEquals(false, axis1.hasNext());
//    
//    final IAxis axis2 = new XPathAxis(wtx, "15 - 3");
//    xtx = (XPathReadTransaction) axis2.getTransaction();
//    assertEquals(true, axis2.hasNext());
//    assertThat(12.0, is(xtx.getValueAsDouble()));
//    assertEquals(false, axis2.hasNext());
//    
//    final IAxis axis3 = new XPathAxis(wtx, "2 * 45");
//    xtx = (XPathReadTransaction) axis3.getTransaction();
//    assertEquals(true, axis3.hasNext());
//    assertThat(90.0, is(xtx.getValueAsDouble()));
//    assertEquals(false, axis3.hasNext());
//    
//    final IAxis axis4 = new XPathAxis(wtx, "14 idiv 3");
//    xtx = (XPathReadTransaction) axis4.getTransaction();
//    assertEquals(true, axis4.hasNext());
//    assertEquals(4, xtx.getValueAsInt());
//    assertEquals(false, axis4.hasNext());
//
//    wtx.abort();
//    wtx.close();
//    session.close();
//
//  }
//
//}