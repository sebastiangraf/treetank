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

package com.treetank.service.xml.xpath;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Session;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.utils.TypedValue;

public class XPathParserTest {

    private XPathParser parser;

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testLiterals() throws TreetankException {

        // Build simple test tree.
        final ISession session = Session.beginSession(ITestConstants.PATH1);

        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveTo(2L);

        IAxis axis;

        axis = new XPathAxis(rtx, "\"12.5\"");
        assertEquals(true, axis.hasNext());
        assertEquals("12.5", TypedValue
                .parseString(rtx.getNode().getRawValue()));
        assertEquals(rtx.keyForName("xs:string"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "\"He said, \"\"I don't like it\"\"\"");
        assertEquals(true, axis.hasNext());
        assertEquals("He said, I don't like it", TypedValue.parseString(rtx
                .getNode().getRawValue()));
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
        assertEquals("12.5", TypedValue
                .parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "12.5E2");
        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals("12.5E2", TypedValue.parseString(rtx.getNode()
                .getRawValue()));
        assertEquals(false, axis.hasNext());

        axis = new XPathAxis(rtx, "1");
        assertEquals(true, axis.hasNext());
        assertEquals("1", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(rtx.keyForName("xs:integer"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        rtx.close();
        session.close();
    }

    @Test
    public void testEBNF() throws TreetankException {

        // Build simple test tree.
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        final IReadTransaction rtx = session.beginReadTransaction();

        parser = new XPathParser(rtx, "/p:a");
        parser.parseQuery();

        parser = new XPathParser(rtx,
                "/p:a/node(), /b/descendant-or-self::adsfj");
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
