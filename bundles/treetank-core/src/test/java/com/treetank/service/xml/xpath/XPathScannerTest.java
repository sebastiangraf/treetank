/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: XPathScannerTest.java 4362 2008-08-24 11:46:16Z kramis $
 */

package com.treetank.service.xml.xpath;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test class to test the functionality of the XPathScanner.
 * 
 * @author Tina Scherer
 */
public class XPathScannerTest {

    /** A test query to test the scanner. */
    private final String mQUERY = "/afFl/Fha:eufh    /789//]@eucbsbcds ==423e+33E" + "[t81sh\n<=@*?<<<><";

    private final String mQUERY2 = "(/af::)Fl/Fhae(:uf:(h (:   /:)789:)//]@eucbsbcds ==423" + "[t81sh\n<=*";

    /** Instance of the scanner that will be tested. */
    private XPathScanner scanner;

    /** Instance of the scanner that will be tested. */
    private XPathScanner scanner2;

    /**
     * Sets up the variables for the test.
     * 
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
        assertEquals(XPathConstants.Token.INTERROGATION, scanner.nextToken().getType());
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
        assertEquals(XPathConstants.Token.DESC_STEP, scanner2.nextToken().getType());
        assertEquals(XPathConstants.Token.CLOSE_SQP, scanner2.nextToken().getType());
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
