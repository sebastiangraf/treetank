package org.treetank.axislayer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.AttributeFilter;
import org.treetank.axislayer.ElementFilter;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;



public class ElementFilterTest {


  public static final String PATH =
      "generated" + File.separator + "ElementFilterTest.tnk";

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
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);
    
    wtx.moveTo(2L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), true);
    
    wtx.moveTo(2L);
    wtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);
    
    wtx.moveTo(3L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);
    
    wtx.moveTo(4L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), true);
    
    wtx.moveTo(5L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);

    wtx.moveTo(8L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), true);
    
    wtx.moveTo(8L);
    wtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);
   
    wtx.moveTo(10L);
    IFilterTest.testIFilterConventions(new ElementFilter(wtx), false);
    

    

    wtx.abort();
    wtx.close();
    session.close();

  }

}
