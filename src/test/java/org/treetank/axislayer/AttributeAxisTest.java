/*
 * Copyright (c) 2007, Marc Kramis
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

package org.treetank.axislayer;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class AttributeAxisTest {

  public static final String PATH =
      "generated" + File.separator + "AttributeAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testIterate() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

    wtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] { 2L });

    wtx.moveTo(8L);
    IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] { 8L });

    wtx.moveTo(11L);
    IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testMultipleAttributes() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey = wtx.insertElementAsFirstChild("foo", "");
    wtx.insertAttribute("foo0", "", 0);
    wtx.insertAttribute("foo1", "", 1);
    wtx.insertAttribute("foo2", "", 2);

    Assert.assertEquals(true, wtx.moveTo(nodeKey));
    Assert.assertEquals("foo0", wtx.getAttributeName(0));
    Assert.assertEquals("foo1", wtx.getAttributeName(1));
    Assert.assertEquals("foo2", wtx.getAttributeName(2));
    Assert.assertEquals(0, wtx.getAttributeValueAsInt(0));
    Assert.assertEquals(1, wtx.getAttributeValueAsInt(1));
    Assert.assertEquals(2, wtx.getAttributeValueAsInt(2));

    Assert.assertEquals(true, wtx.moveTo(nodeKey));
    final IAxis axis = new AttributeAxis(wtx);

    Assert.assertEquals(true, axis.hasNext());
    axis.next();
    Assert.assertEquals(nodeKey, wtx.getNodeKey());
    Assert.assertEquals("foo0", wtx.getName());
    Assert.assertEquals(0, wtx.getValueAsInt());

    Assert.assertEquals(true, axis.hasNext());
    axis.next();
    Assert.assertEquals(nodeKey, wtx.getNodeKey());
    Assert.assertEquals("foo1", wtx.getName());
    Assert.assertEquals(1, wtx.getValueAsInt());

    Assert.assertEquals(true, axis.hasNext());
    axis.next();
    Assert.assertEquals(nodeKey, wtx.getNodeKey());
    Assert.assertEquals("foo2", wtx.getName());
    Assert.assertEquals(2, wtx.getValueAsInt());

    wtx.abort();
    wtx.close();
    session.close();
  }

}