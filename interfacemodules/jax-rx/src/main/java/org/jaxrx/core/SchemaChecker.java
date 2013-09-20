/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jaxrx.core;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class validates XML documents against a specified XML schema.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class SchemaChecker {
    /**
     * The validation schema.
     */
    private final String xslSchema;

    /**
     * Constructor.
     * 
     * @param schema
     *            schema to check
     */
    public SchemaChecker(final String schema) {
        xslSchema = "/" + schema + ".xsd";
    }

    /**
     * This method parses an XML input with a W3C DOM implementation and
     * validates it then with the available XML schema.
     * 
     * @param input
     *            The input stream containing the XML query.
     * @return The parsed XML source as {@link Document}.
     */
    public Document check(final InputStream input) {
        Document document;
        try {
            final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = docBuilder.parse(input);

            final InputStream is = getClass().getResourceAsStream(xslSchema);
            final Source source = new SAXSource(new InputSource(is));
            checkIsValid(document, source);
        } catch (final SAXException exce) {
            throw new JaxRxException(400, exce.getMessage());
        } catch (final ParserConfigurationException exce) {
            throw new JaxRxException(exce);
        } catch (final IOException exce) {
            throw new JaxRxException(exce);
        }
        return document;
    }

    /**
     * This method checks the parsed document if it is valid to a given XML
     * schema. If not, an exception is thrown
     * 
     * @param document
     *            The parsed document.
     * @param source
     *            The {@link String} representation of the XML schema file.
     * @throws SAXException
     *             if the document is invalid
     * @throws IOException
     *             if the input cannot be read
     */
    private void checkIsValid(final Document document, final Source source) throws SAXException, IOException {

        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(source);
        final Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }
}
