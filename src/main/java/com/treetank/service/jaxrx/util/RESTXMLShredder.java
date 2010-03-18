/**
 * 
 */
package com.treetank.service.jaxrx.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This class is responsible to create a TreeTank {@link XMLStreamReader} out of
 * an {@link InputStream}.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public final class RESTXMLShredder {

    /**
     * The empty constructor.
     */
    private RESTXMLShredder() {
        // i do nothing
    }

    /**
     * This method creates an {@link XMLStreamReader} out of an
     * {@link InputStream}.
     * 
     * @param inputStream
     *            The {@link InputStream} containing the XML file that has to be
     *            stored.
     * @return The {@link XMLStreamReader} object.
     * @throws IOException
     *             The exception occurred.
     * @throws XMLStreamException
     *             The exception occurred.
     */
    public static XMLStreamReader createReader(final InputStream inputStream)
            throws IOException, XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory.createXMLStreamReader(inputStream);
    }

}
