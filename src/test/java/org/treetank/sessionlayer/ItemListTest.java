/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: AttributeAndNamespaceTest.java 4017 2008-03-26 16:59:23Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IItemList;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.TextNode;
import org.treetank.utils.TestDocument;

public class ItemListTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "ItemListTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAddItem() throws IOException {

    final IItemList itemList = new ItemList();

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();

    itemList.addItem(new TextNode(-3, 0, -1, -1, 0, null));

    final IReadTransaction rtx = session.beginReadTransaction(itemList);
    Assert.assertEquals(true, rtx.moveTo(2));
    Assert.assertEquals(1, rtx.getAttributeCount());
    Assert.assertEquals(true, rtx.moveTo(-2));
    Assert.assertEquals(-2, rtx.getNodeKey());
    Assert.assertEquals(false, rtx.moveTo(-3));
    rtx.close();

    session.close();

  }

}
