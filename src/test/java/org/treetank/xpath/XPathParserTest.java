
package org.treetank.xpath;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.XPathAxis;
import org.treetank.xpath.XPathParser;
import org.treetank.xpath.types.Type;

public class XPathParserTest {

  XPathParser parser;

  public static final String PATH =
    "target" + File.separator + "tnk" + File.separator + "Test.tnk";


@Test
public void testLiterals() throws IOException {
  
//Build simple test tree.
  final ISession session = Session.beginSession(PATH);
  
  final IReadTransaction rtx = session.beginReadTransaction(new ItemList());
  rtx.moveTo(2L);
  
  
  IAxis axis;

  axis = new XPathAxis(rtx, "\"12.5\"");
  assertEquals(true, axis.hasNext());
  assertEquals("12.5", rtx.getValue());
  assertEquals(rtx.keyForName("xs:string"), rtx.getTypeKey());
  assertEquals(false, axis.hasNext());

  axis = new XPathAxis(rtx, "\"He said, \"\"I don't like it\"\"\"");
  assertEquals(true, axis.hasNext());
  assertEquals("He said, I don't like it", rtx.getValue());
  assertEquals(rtx.keyForName("xs:string"), rtx.getTypeKey());
  assertEquals(false, axis.hasNext());

  axis = new XPathAxis(rtx, "12");
  assertEquals(true, axis.hasNext());
  assertEquals(rtx.keyForName("xs:integer"), rtx.getTypeKey());
  // assertEquals("12", rtx.getValueAsInt());
  assertEquals(false, axis.hasNext());

  axis = new XPathAxis(rtx, "12.5");
  assertEquals(true, axis.hasNext());
  assertEquals(rtx.keyForName("xs:decimal"), rtx.getTypeKey());
  // assertEquals(12.5, rtx.getValueAsDouble());
  assertEquals(false, axis.hasNext());

  axis = new XPathAxis(rtx, "12.5E2");
  assertEquals(true, axis.hasNext());
  assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
  // assertEquals(12.5E2, rtx.getValueAsDouble());
  assertEquals(false, axis.hasNext());

  axis = new XPathAxis(rtx, "1");
  assertEquals(true, axis.hasNext());
  // assertEquals("1", rtx.getValueAsInt());
  assertEquals(rtx.keyForName("xs:integer"), rtx.getTypeKey());
  assertEquals(false, axis.hasNext());


  rtx.close(); 
  session.close();
  }


  @Test
  public void testEBNF() throws IOException {
    
 // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction(new ItemList());
  

    parser = new XPathParser(rtx, "/p:a");
    parser.parseQuery();

    parser = new XPathParser(rtx, "/p:a/node(), /b/descendant-or-self::adsfj");
    parser.parseQuery();

    parser = new XPathParser(rtx, "for $i in /p:a return $i");
    parser.parseQuery();

    parser = new XPathParser(rtx, "for $i in /p:a return /p:a");
    parser.parseQuery();

    parser = new XPathParser(rtx, "child::element(person)");
    parser.parseQuery();

    parser = new XPathParser(rtx, "child::element(person, xs:string)");
    parser.parseQuery();

    parser = new XPathParser(rtx, " child::element(*, xs:string)");
    parser.parseQuery();

    parser = new XPathParser(rtx, "child::element()");
    parser.parseQuery();

    // parser = new XPathParser(rtx, ". treat as item()");
    // parser.parseQuery();

    parser = new XPathParser(rtx, "/b instance of item()");
    parser.parseQuery();

    rtx.close();
    session.close();

  }



}
