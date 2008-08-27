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

package org.treetank.xpath.operators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.sessionlayer.Session;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.expr.LiteralExpr;
import org.treetank.xpath.types.Type;

public class AbstractOpAxisTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "AbstractOpAxisTest.tnk";

  @Before
  public void setUp() throws Exception {

    Session.removeSession(PATH);

  }

  @Test
  public final void testHasNext() {

    final ISession session = Session.beginSession(PATH);
    IReadTransaction rtx = session.beginReadTransaction();
    IItem item1 = new AtomicValue(1.0, Type.DOUBLE);
    IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

    IAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
    IAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
    AbstractOpAxis axis = new DivOpAxis(rtx, op1, op2);

    assertEquals(true, axis.hasNext());
    assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
    assertEquals(false, axis.hasNext());

    //here both operands are the empty sequence
    axis = new DivOpAxis(rtx, op1, op2);
    assertEquals(true, axis.hasNext());
    assertThat(Double.NaN, is(Double.parseDouble(rtx.getValue())));
    assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
    assertEquals(false, axis.hasNext());

  }

}
