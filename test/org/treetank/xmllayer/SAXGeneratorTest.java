/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class SAXGeneratorTest {

  public static final String PATH =
      "generated" + File.separator + "SAXGeneratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testSAXGenerator() {
    try {
      // Setup session.
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.create(wtx);
      wtx.commit();

      // Generate from this session.
      final Writer writer = new StringWriter();
      final IReadTransaction rtx = session.beginReadTransaction();
      final SAXGenerator generator =
          new SAXGenerator(new DescendantAxis(rtx), writer, false);
      generator.run();
      TestCase.assertEquals(TestDocument.XML, writer.toString());
      rtx.close();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }
  
  @Test
  public void testSubtreeSAXGenerator() {
    try {
      // Setup session.
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.createWithoutAttributes(wtx);
      wtx.commit();

      // Generate from this session.
      final IReadTransaction rtx = session.beginReadTransaction();
      ContentHandler testHandler = new SubtreeHandlerTest(TestDocument.XMLWITHOUTATTRIBUTES);
      final SubtreeSAXGenerator generator =
          new SubtreeSAXGenerator(rtx,testHandler);
      generator.run();
      rtx.close();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }
  
  class SubtreeHandlerTest implements ContentHandler {
    
    String document;
    String generatedDocument;
    
    SubtreeHandlerTest(String paramDocument) {
      document = paramDocument;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
      this.generatedDocument = this.generatedDocument + new String(arg0, arg1, arg2);
    }

    public void endDocument() throws SAXException {
      assertEquals(document, generatedDocument);
    }

    public void endElement(String arg0, String arg1, String arg2)
        throws SAXException {
      this.generatedDocument = this.generatedDocument + "</" + arg2 + ">";     
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
        throws SAXException {
    }

    public void processingInstruction(String arg0, String arg1)
        throws SAXException {
    }

    public void setDocumentLocator(Locator arg0) {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void startDocument() throws SAXException {
      this.generatedDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    }

    public void startElement(
        String arg0,
        String arg1,
        String qname,
        Attributes arg3) throws SAXException {
      this.generatedDocument = this.generatedDocument + "<" + qname + ">";
    }

    public void startPrefixMapping(String arg0, String arg1)
        throws SAXException {
      // TODO Auto-generated method stub
      
    }

    
    
  }
  

}
