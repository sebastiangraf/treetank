
package org.treetank.xpath;

import java.io.File;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.sessionlayer.Session;
import org.treetank.xpath.XPathAxis;

public class TestClass {

  public static final String PATH = "target" + File.separator
      + "address.tnk";


  public static void main(String[] args) {
    
  
    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction wtx = session.beginReadTransaction();

////    wtx.moveTo(17L);
//    
   String query = "//email";
////    String query = "./email";
////   String query = "lastname";
////    IAxis axis = new XPathAxis(wtx, query);
////
////    System.out.println("Query: " + query);
////    while (axis.hasNext()) {
////      System.out.println(axis.next());
////    }
//
//  
    for (long key : new XPathAxis(wtx, query)) {
//      System.out.println(key);
    }
  
  wtx.close();
  session.close();
  }

}
