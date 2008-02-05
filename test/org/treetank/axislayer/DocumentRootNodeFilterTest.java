package org.treetank.axislayer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DocumentRootNodeFilter;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;



public class DocumentRootNodeFilterTest {


  public static final String PATH =
      "generated" + File.separator + "DocumentRootNodeFilterTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testIFilterConvetions() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(0L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), true);
    
    wtx.moveTo(2L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(2L);
    wtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(3L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(4L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(5L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);

    wtx.moveTo(8L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(8L);
    wtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
   
    wtx.moveTo(10L);
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), false);
    
    wtx.moveTo(10L);
    wtx.moveToDocumentRoot();
    IFilterTest.testIFilterConventions(new DocumentRootNodeFilter(wtx), true);
    

    wtx.abort();
    wtx.close();
    session.close();

  }

}

