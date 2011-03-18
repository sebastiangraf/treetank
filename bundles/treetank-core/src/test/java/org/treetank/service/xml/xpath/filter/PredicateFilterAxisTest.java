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
 * $Id: PredicateFilterAxisTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package org.treetank.service.xml.xpath.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxisTest;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the PredicateAxis.
 * 
 * @author Tina Scherer
 */
public class PredicateFilterAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testPredicates() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveToDocumentRoot();

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a[@i]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x]"), new long[] {
            9L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[text()]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[element()]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[node()/text()]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()/node()/node()]"), new long[] {});

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[//element()]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[/text()]"), new long[] {});

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3<4]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13>=4]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13.0>=4]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[4 = 4]"), new long[] {
            1L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3=4]"), new long[] {});

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3.2 = 3.22]"), new long[] {});

        rtx.moveTo(1L);

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b[child::c]"), new long[] {
            5L, 9L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*[text() or c]"), new long[] {
            5l, 9L
        });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*[text() or c], /node(), //c"), new long[] {
            5l, 9L, 1L, 7L, 11L
        });

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}
