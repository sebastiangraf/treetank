package com.treetank.service.xml.serialize;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.serialize.SerializerBuilder.SAXSerializerBuilder;
import com.treetank.utils.DocumentCreater;

/**
 * Test SAXSerializer.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class SAXSerializerTest extends XMLTestCase {
  @Before
  public void setUp() throws TreetankException {
    TestHelper.deleteEverything();
  }

  @After
  public void tearDown() throws TreetankException {
    TestHelper.closeEverything();
  }

  @Test
  public void testSAXSerializer() {
    try {
      // Setup test file.
      final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
      final ISession session = database.getSession();
      final IWriteTransaction testTrx =
          session.beginWriteTransaction();
      DocumentCreater.create(testTrx);
      testTrx.commit();
      testTrx.close();
      
      final IReadTransaction rtx = session.beginReadTransaction();

      final StringBuilder strBuilder = new StringBuilder();
      final ContentHandler contHandler = new XMLFilterImpl() {

        @Override
        public void startDocument() {
          strBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        }
        
        @Override
        public void startElement(
            final String uri,
            final String localName,
            final String qName,
            final Attributes atts) throws SAXException {
          strBuilder.append("<" + qName);

          for (int i = 0; i < atts.getLength(); i++) {
            strBuilder.append(" " + atts.getQName(i));
            strBuilder.append("=\"" + atts.getValue(i) + "\"");
          }

          strBuilder.append(">");
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
          strBuilder.append("</" + qName + ">");
        }

        @Override
        public void characters(
            final char[] ch,
            final int start,
            final int length) throws SAXException {
          for (int i = start; i < start + length; i++) {
            strBuilder.append(ch[i]);
          }
        }
      };

      final SAXSerializer serializer =
          new SAXSerializerBuilder(new DescendantAxis(rtx), contHandler)
              .build();
      serializer.call();

      assertXMLEqual(DocumentCreater.XML, strBuilder.toString());
    } catch (final TreetankException e) {
      TestCase.fail("Treetank exception occured!");
    } catch (final Exception e) {
      TestCase.fail("Any exception occured!");
    }
  }
}
