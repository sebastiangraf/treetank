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
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the CompAxis.
 * 
 * @author Tina Scherer
 */
public class CompAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "CompAxisTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testComp() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction();

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveToDocumentRoot();

    final IAxis axis1 = new XPathAxis(rtx, "1.0 = 1.0");
    assertEquals(true, axis1.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new XPathAxis(rtx, "(1, 2, 3) < (2, 3)");
    assertEquals(true, axis2.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    final IAxis axis3 = new XPathAxis(rtx, "(1, 2, 3) > (3, 4)");
    assertEquals(true, axis3.hasNext());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}