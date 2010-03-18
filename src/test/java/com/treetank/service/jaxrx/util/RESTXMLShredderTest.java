/**
 * 
 */
package com.treetank.service.jaxrx.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

/**
 * This class tests the class {@link RESTXMLShredder}.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public class RESTXMLShredderTest {

    /**
     * Test method for
     * {@link org.treetank.rest.util.RESTXMLShredder#createReader(java.io.InputStream)}
     * .
     * 
     * @throws XMLStreamException
     * @throws IOException
     */
    @Test
    public final void testCreateReader() throws IOException, XMLStreamException {
        final InputStream input = RESTXMLShredderTest.class
                .getResourceAsStream("/books.xml");
        final XMLStreamReader reader = RESTXMLShredder.createReader(input);
        assertNotNull("Test if the reader has been created", reader);
    }

}
