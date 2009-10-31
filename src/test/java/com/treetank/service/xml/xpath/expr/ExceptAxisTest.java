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
 * $Id: ExceptAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IAxisTest;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
public class ExceptAxisTest {
    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testExcept() throws TreetankException {
        // Build simple test tree.
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::node() except b"), new long[] { 4L, 8L, 13L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::node() except child::node()[attribute::p:x]"),
                new long[] { 4L, 5L, 8L, 13L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::node()/parent::node() except self::node()"),
                new long[] {});

        IAxisTest
                .testIAxisConventions(new XPathAxis(rtx,
                        "//node() except //text()"), new long[] { 1L, 5L, 9L,
                        7L, 11L });

        rtx.moveTo(1L);
        IAxisTest
                .testIAxisConventions(new XPathAxis(rtx,
                        "b/preceding::node() except text()"), new long[] { 7L,
                        6L, 5L });

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
    }

}
