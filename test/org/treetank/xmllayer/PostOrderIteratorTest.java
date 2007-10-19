package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class PostOrderIteratorTest {
	
	public static final String PATH = "generated/DescendantAxisIteratorTest.ic3";

	  @Before
	  public void setUp() throws Exception {
	    new File(PATH).delete();
	  }

	  @Test
	  public void testIterate() throws Exception {

	    // Build simple test tree.
	    final ISession session = new Session(PATH);
	    final IWriteTransaction trx = session.beginWriteTransaction();
	    TestDocument.create(trx);

	    final IAxisIterator postOrderIterator = new PostOrderIterator(trx,true);
	    
	    assertEquals(2L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(4L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(5L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(3L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(6L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(8L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(9L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(7L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(10L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());

	    assertEquals(1L, trx.getNodeKey());
	    assertEquals(true, postOrderIterator.next());
	    
	    assertEquals(0L, trx.getNodeKey());
      assertEquals(false, postOrderIterator.next());
	    
	    session.abort();
	    session.close();

	  }
}
