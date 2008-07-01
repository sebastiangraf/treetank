/**
 * 
 */
package org.treetank.xpath;


import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.treetank.xpath.XPathConstants;
import org.treetank.xpath.XPathScanner;


/** 
 * JUnit test class to test the functionality of the XPathScanner.
 * 
 * @author Tina Scherer
 */
public class XPathScannerTest {
  
  /** A test query to test the scanner. */
  private final String mQUERY = 
    "/afFl/Fha:eufh    /789//]@eucbsbcds ==423e+33E" 
    + "[t81sh\n<=@*?<<<><";
  
  private final String mQUERY2 = 
    "(/af::)Fl/Fhae(:uf:(h (:   /:)789:)//]@eucbsbcds ==423" 
    + "[t81sh\n<=*";
  /** Instance of the scanner that will be tested. */
  private XPathScanner scanner;
  
  /** Instance of the scanner that will be tested. */
  private XPathScanner scanner2;
  
  /** Sets up the variables for the test.
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() {
    
    scanner = new XPathScanner(mQUERY);
    scanner2 = new XPathScanner(mQUERY2);
  }
  
  @Test
  public void testScan() throws IOException {
    assertEquals(XPathConstants.Token.SLASH, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SLASH, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.COLON, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SLASH, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.VALUE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.DESC_STEP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.CLOSE_SQP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.AT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.EQ, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.EQ, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.VALUE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.E_NUMBER, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.PLUS, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.VALUE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.E_NUMBER, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.OPEN_SQP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.COMP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.AT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.STAR, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.INTERROGATION, scanner.nextToken().
        getType());
    assertEquals(XPathConstants.Token.L_SHIFT, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.COMP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.COMP, scanner.nextToken().getType());
    assertEquals(XPathConstants.Token.COMP, scanner.nextToken().getType());
  }
  
  @Test
  public void testComment() throws IOException {
    assertEquals(XPathConstants.Token.OPEN_BR, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.SLASH, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.COLON, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.COLON, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.CLOSE_BR, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.SLASH, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.DESC_STEP, scanner2.nextToken()
        .getType());
    assertEquals(XPathConstants.Token.CLOSE_SQP, scanner2.nextToken()
        .getType());
    assertEquals(XPathConstants.Token.AT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.EQ, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.EQ, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.VALUE, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.OPEN_SQP, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.TEXT, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.SPACE, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.COMP, scanner2.nextToken().getType());
    assertEquals(XPathConstants.Token.STAR, scanner2.nextToken().getType());
  }

}
