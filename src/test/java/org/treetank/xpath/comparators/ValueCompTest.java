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
package org.treetank.xpath.comparators;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.comparators.AbstractComparator;
import org.treetank.xpath.comparators.CompKind;
import org.treetank.xpath.comparators.NodeComp;
import org.treetank.xpath.expr.LiteralExpr;


public class ValueCompTest {
  
  AbstractComparator comparator;

  public static final String PATH = "generated" + File.separator
      + "ValueCompTest.tnk";

  ISession session;

  IWriteTransaction wtx;

  IReadTransaction rtx;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

    // Build simple test tree.
    session = Session.beginSession(PATH);
    wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    rtx = session.beginReadTransaction(new ItemList());
    
    comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), 
        new LiteralExpr(rtx, -1), CompKind.EQ);
    
  }

//  @Test
//  public void testCompare() {
//
//    fail("Not yet implemented");
//  }
//
//  @Test
//  public void testAtomize() {
//
//    fail("Not yet implemented");
//  }
//
//  @Test
//  public void testGetType() {
//
//    fail("Not yet implemented");
//  }

}
