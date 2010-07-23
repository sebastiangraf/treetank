package com.treetank.service.xml.serialize;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.utils.DocumentCreater;

/**
 * Test StAXSerializer.
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public class StAXSerializerTest {
    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testStAXSerializer() {
        try {
            // Setup test file.
            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1
                    .getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final XMLSerializerBuilder builder = new XMLSerializerBuilder(
                    session, out);
            builder.setDeclaration(false);
            final XMLSerializer xmlSerializer = builder.build();
            xmlSerializer.call();

            final IReadTransaction rtx = session.beginReadTransaction();
            final StAXSerializer serializer = new StAXSerializer(
                    new DescendantAxis(rtx));
            final StringBuilder strBuilder = new StringBuilder();
            boolean isEmptyElement = false;

            while (serializer.hasNext()) {
                XMLEvent event = serializer.nextEvent();

                System.out.println(event);

                switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    strBuilder
                            .append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    emitElement(event, strBuilder);

                    if (serializer.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
                        strBuilder.append("/>");
                        isEmptyElement = true;
                    } else {
                        strBuilder.append('>');
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (!isEmptyElement) {
                        emitQName(true, event, strBuilder);
                        strBuilder.append('>');
                    } else {
                        isEmptyElement = false;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    strBuilder.append(((Characters) event).getData());
                    break;
                }
            }

            assertEquals(out.toString(), strBuilder.toString());

            wtx.close();
            rtx.close();
            session.close();
            database.close();
        } catch (final TreetankException e) {
            TestCase.fail("Treetank exception occured!");
        } catch (final Exception e) {
            TestCase.fail("Any exception occured!");
        }
    }

    /**
     * Emit an element.
     * 
     * @param event
     *            {@link XMLEvent}, either a start tag or an end tag.
     * @param strBuilder
     *            String builder to build the string representation.
     */
    @Ignore
    private void emitElement(final XMLEvent event,
            final StringBuilder strBuilder) {
        emitQName(true, event, strBuilder);

        if (event.isStartElement()) {
            final StartElement elem = ((StartElement) event);
            // Parse namespaces.
            for (Iterator<?> it = elem.getNamespaces(); it.hasNext();) {
                final Namespace namespace = (Namespace) it.next();

                if ("".equals(namespace.getPrefix())) {
                    strBuilder.append(" xmlns=\"")
                            .append(namespace.getNamespaceURI()).append("\"");
                } else {
                    strBuilder.append(" xmlns:").append(namespace.getPrefix())
                            .append("=\"").append(namespace.getNamespaceURI())
                            .append("\"");
                }
            }

            // Parse attributes.
            for (Iterator<?> it = elem.getAttributes(); it.hasNext();) {
                final Attribute attribute = (Attribute) it.next();
                emitQName(false, attribute, strBuilder);
                strBuilder.append("=\"").append(attribute.getValue())
                        .append("\"");
            }
        }
    }

    /**
     * Emit a qualified name.
     * 
     * @param event
     *            {@link XMLEvent}, either a start tag or an end tag.
     * @param strBuilder
     *            String builder to build the string representation.
     * @param isElem
     *            Determines if it is an element or an attribute.
     */
    @Ignore
    private void emitQName(final boolean isElem, final XMLEvent event,
            final StringBuilder strBuilder) {
        QName qName;
        if (isElem) {
            if (event.isStartElement()) {
                strBuilder.append('<');
                qName = ((StartElement) event).getName();
            } else {
                strBuilder.append("</");
                qName = ((EndElement) event).getName();
            }
        } else {
            qName = ((Attribute) event).getName();
        }

        if (!isElem) {
            strBuilder.append(' ');
        }

        if (qName.getPrefix() == "") {
            strBuilder.append(qName.getLocalPart());
        } else {
            strBuilder.append(qName.getPrefix() + ':' + qName.getLocalPart());
        }
    }
}
