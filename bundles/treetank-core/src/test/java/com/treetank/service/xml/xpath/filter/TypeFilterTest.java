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
 * $Id: TypeFilterTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.filter.IFilterTest;
import com.treetank.axis.filter.TypeFilter;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.XPathError;

public class TypeFilterTest {

    public static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test.xml";

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testIFilterConvetions() throws Exception {

        // Build simple test tree.
        // final ISession session = Session.beginSession(PATH);
        // final IWriteTransaction wtx = session.beginWriteTransaction();
        // TestDocument.create(wtx);
        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

        // Verify.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        final IAxis axis = new XPathAxis(rtx, "a");
        final IReadTransaction xtx = axis.getTransaction();

        xtx.moveTo(9L);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:long"), false);

        xtx.moveTo(4L);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:double"), false);

        xtx.moveTo(1L);
        xtx.moveToAttribute(0);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untypedAtomic"), true);

        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:anyType"), false);
        try {
            IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:bla"), false);
            fail("Expected a Type not found error.");
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPST0051 "
                + "Type is not defined in the in-scope schema types as an " + "atomic type."));
        }

        xtx.close();
        rtx.close();
        session.close();
        database.close();

    }
}
