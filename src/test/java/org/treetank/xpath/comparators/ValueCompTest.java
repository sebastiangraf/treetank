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
