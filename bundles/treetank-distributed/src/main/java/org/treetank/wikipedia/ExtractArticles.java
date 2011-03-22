/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.wikipedia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.treetank.utils.LogWrapper;

/**
 * <h1>ExtractArticles</h1>
 * 
 * <p>
 * Provides a SAX based mechanism to extract the first N Wikipedia articles found in the XML document. The
 * threshold value N can be set by the user or defaults to 10.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ExtractArticles extends XMLFilterImpl {

    /**
     * Logger for determining the log level.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractArticles.class);

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** Number of articles to extract. */
    private transient int mArtNr = 10;

    /** Articles parsed so far. */
    private transient int mArtParsed;

    /** Root node of wikipedia dump file. */
    private static final QName WIKIROOT =
        new QName(XMLConstants.NULL_NS_URI, "mediawiki", XMLConstants.DEFAULT_NS_PREFIX);

    /** System independent path separator. */
    private static final String SEP = System.getProperty("file.separator");

    /** File to write SAX output to. */
    private static final File TARGET = new File("target" + SEP + "wikipedia.xml");
    
    /** Start of computation in milliseconds. */
    private static long start;

    /**
     * Constructor.
     * 
     * @param parent
     *            An XMLReader instance {@link XMLReader}.
     */
    public ExtractArticles(final XMLReader parent) {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param paramArtNr
     *            Number of articles to extract
     */
    public ExtractArticles(final int paramArtNr) {
        mArtNr = paramArtNr;
    }

    @Override
    public void startElement(final String paramURI, final String paramLocalName,
        final String paramQName, final Attributes paramAtts) throws SAXException {

        if (paramQName.equalsIgnoreCase("page")) {
            mArtParsed++;
        }

        if (mArtParsed < mArtNr) {
            super.startElement(paramURI, paramLocalName, paramQName, paramAtts);
        } else {
            // After mArtNr articles have been parsed close root element / end document and exit.
            super.endElement(WIKIROOT.getNamespaceURI(), WIKIROOT.getLocalPart(), WIKIROOT.getPrefix() + ":"
                + WIKIROOT.getLocalPart());
            super.endDocument();
            System.out.println("done in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
            System.exit(0);
        }
    }

    /**
     * Main method.
     * 
     * @param paramArgs
     *            First param specifies the Wikipedia dump to parse.
     */
    public static void main(final String[] paramArgs) {
        if (paramArgs.length != 1) {
            new IllegalStateException("First param must be the wikipedia dump!");
        }

        start = System.currentTimeMillis();

        System.out.print("Start extracting articles... ");

        final String wikiDump = new File(paramArgs[0]).getAbsolutePath();
        final XMLReader parser = new ExtractArticles(new SAXParser());

        if (parser != null) {
            try {
                TARGET.delete();
                TARGET.createNewFile();
                final XMLSerializer printer = new XMLSerializer(new FileWriter(TARGET), new OutputFormat());
                parser.setContentHandler(printer);
                parser.parse(wikiDump);
            } catch (final IOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final SAXException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }
    }
}
