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
 * $Id: XPathParserTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package org.treetank.service.xml.xpath.parser;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.parser.XPathParser;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPathParserTest {

    private XPathParser parser;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testLiterals() throws AbsTTException {

        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(2L);

        AbsAxis axis;

        axis = new XPathAxis(rtx, "\"12.5\"");
        assertEquals(true, axis.hasNext());
        assertEquals("12.5", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(rtx.keyForName("xs:string"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "\"He said, \"\"I don't like it\"\"\"");
        assertEquals(true, axis.hasNext());
        assertEquals("He said, I don't like it", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(rtx.keyForName("xs:string"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "12");
        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:integer"), rtx.getNode().getTypeKey());
        assertEquals("12", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "12.5");
        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:decimal"), rtx.getNode().getTypeKey());
        assertEquals("12.5", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "12.5E2");
        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals("12.5E2", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "1");
        assertEquals(true, axis.hasNext());
        assertEquals("1", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(rtx.keyForName("xs:integer"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        rtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testEBNF() throws AbsTTException {

        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();

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
        database.close();

    }

}
