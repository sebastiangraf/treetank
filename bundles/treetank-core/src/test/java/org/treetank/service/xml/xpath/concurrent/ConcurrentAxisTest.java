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
 *     * Neither the name of the <organization> nor the
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

package org.treetank.service.xml.xpath.concurrent;

import java.io.File;


import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.SkipBench;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.ChildAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.NestedAxis;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.comparators.CompKind;
import org.treetank.service.xml.xpath.comparators.GeneralComp;
import org.treetank.service.xml.xpath.concurrent.ConcurrentAxis;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.filter.PredicateFilterAxis;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentAxisTest {

    /** XML file name to test. */
    private static final String XMLFILE = "10mb.xml";
    /** Path to XML file. */
    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + XMLFILE;
    /** IDatabase instance. */
    private IDatabase mDatabase;
    /** ISession instance. */
    private ISession mSession;
    /** IReadTranscation instance. */
    private IReadTransaction mRtx;

    /**
     * Constructor, just to meet checkstyle requirements.
     */
    public ConcurrentAxisTest() {

    }

    /**
     * Method is called once before each test. It deletes all states, shreds XML file to database and
     * initializes the required variables.
     */
    @Ignore
    @BeforeEachRun
    @Before
    public final void setUp() {
        try {
            TestHelper.deleteEverything();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            mDatabase = TestHelper.getDatabase(PATHS.PATH1.getFile());
            mSession = mDatabase.getSession();
            mRtx = mSession.beginReadTransaction();
        } catch (final Exception mExe) {
            mExe.printStackTrace();
        }
    }
    
    @Test
    public void test() {
        assertEquals(true, true);
    }

    /**
     * Test seriell.
     */
    @Ignore
    @SkipBench
    @Bench
    @Test
    public final void testSeriellOld() {
        // final String query = "//people/person[@id=\"person3\"]/name";
        // final String query = "count(//location[text() = \"United States\"])";
        final String query = "//regions/africa//location";
        // final String result = "<name>Limor Simone</name>";
        final int resultNumber = 550;
        AbsAxis axis = null;
        try {
            axis = new XPathAxis(mRtx, query);
        } catch (final TTXPathException ttExp) {
            ttExp.printStackTrace();
        }
        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test seriell.
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testSeriellNew() {

        /* query: //regions/africa//location */
        final int resultNumber = 550;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                "regions")), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "africa"))),
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "location")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);
    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Ignore
    @SkipBench
    @Bench
    @Test
    public final void testConcurrent() {
        /* query: //regions/africa//location */
        final int resultNumber = 550;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "regions"))), new ConcurrentAxis(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "africa")))), new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "name"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testPartConcurrentDescAxis1() {
        /* query: //regions/africa//location */
        final int resultNumber = 550;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "regions"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(
                mRtx, "africa"))), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "name")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testPartConcurrentDescAxis2() {
        /* query: //regions/africa//location */
        final int resultNumber = 550;
        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                "regions")), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "africa"))),
                new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                    "name"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Test seriell.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testSeriellNew2() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))),
                new PredicateFilterAxis(mRtx, new NestedAxis(new FilterAxis(new ChildAxis(mRtx),
                    new NameFilter(mRtx, "date")), new GeneralComp(mRtx, new FilterAxis(new ChildAxis(mRtx),
                    new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent2() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                    "item")))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx), new NameFilter(
                mRtx, "mailbox")))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mail")))), new PredicateFilterAxis(mRtx, new NestedAxis(new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "date")), new GeneralComp(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testPartConcurrent2() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                    "item")))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox"))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))), new PredicateFilterAxis(
                mRtx, new NestedAxis(new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "date")),
                    new GeneralComp(mRtx, new FilterAxis(new ChildAxis(mRtx), new TextFilter(mRtx)), literal,
                        CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Test seriell.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testSeriellNew3() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "regions")), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testCompleteConcurrent3() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(
                new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(
                    mRtx, true), new NameFilter(mRtx, "regions"))), new ConcurrentAxis(mRtx, new FilterAxis(
                    new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))), new ConcurrentAxis(mRtx,
                    new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox")))),
                new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testPartConcurrent3Axis1() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))), new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(
                mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(
                mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testPartConcurrent3Axis2() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "regions")), new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "item")))), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testPartConcurrent3Axis1and2() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))), new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
        @Test
        public final
        void testPartConcurrent3Axis1and3() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))), new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item"))), new ConcurrentAxis(mRtx,
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox")))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
     @Ignore
     @SkipBench
        @Test
        public final
        void testPartConcurrent3Axis2and4() {
        /* query: //regions//item/mailbox/mail */
        final int resultNumber = 20946; // 100mb xmark
        // final int resultNumber = 208497; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "regions")), new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "item")))), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mailbox"))), new ConcurrentAxis(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Test seriell.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testSeriellNew4() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))),
                new PredicateFilterAxis(mRtx, new NestedAxis(new FilterAxis(new ChildAxis(mRtx),
                    new NameFilter(mRtx, "date")), new GeneralComp(mRtx, new FilterAxis(new ChildAxis(mRtx),
                    new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent4() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                    "item")))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx), new NameFilter(
                mRtx, "mailbox")))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mail")))), new PredicateFilterAxis(mRtx, new NestedAxis(new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "date")), new GeneralComp(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent4ChildAxis() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "item"))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mailbox")))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(
                mRtx), new NameFilter(mRtx, "mail")))), new PredicateFilterAxis(mRtx, new NestedAxis(
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "date")), new GeneralComp(mRtx,
                    new FilterAxis(new ChildAxis(mRtx), new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent4DescAxis1() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item"))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))), new PredicateFilterAxis(mRtx,
                new NestedAxis(new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "date")),
                    new GeneralComp(mRtx, new FilterAxis(new ChildAxis(mRtx), new TextFilter(mRtx)), literal,
                        CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent4DescAxis2() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mail"))), new PredicateFilterAxis(mRtx, new NestedAxis(new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "date")), new GeneralComp(mRtx, new FilterAxis(
                new ChildAxis(mRtx), new TextFilter(mRtx)), literal, CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @SkipBench
    @Ignore
    @Test
    public final void testConcurrent4DescAxises() {
        /* query: //regions//item/mailbox/mail[date="02/24/2000"] */
        final int resultNumber = 22;

        long date =
            mRtx.getItemList().addItem(
                new AtomicValue(TypedValue.getBytes("02/24/2000"), mRtx.keyForName("xs:string")));
        AbsAxis literal = new LiteralExpr(mRtx, date);

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))), new ConcurrentAxis(mRtx,
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox")))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))), new PredicateFilterAxis(mRtx,
                new NestedAxis(new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "date")),
                    new GeneralComp(mRtx, new FilterAxis(new ChildAxis(mRtx), new TextFilter(mRtx)), literal,
                        CompKind.EQ))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Test seriell.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testSeriellNew5() {
        /* query: //description//listitem/text */
        final int resultNumber = 5363;

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                "description")), new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx,
                "listitem"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "text")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testConcurrent5() {
        /* query: //description//listitem/text */
        final int resultNumber = 5363;

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "description"))), new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "listitem")))), new ConcurrentAxis(mRtx,
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "text"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testConcurrentPart5Axis1() {
        /* query: //description//listitem/text */
        final int resultNumber = 5363;

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "description"))), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "listitem"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "text")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testConcurrentPart5Axis2() {
        /* query: //description//listitem/text */
        final int resultNumber = 5363;

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx,
                true), new NameFilter(mRtx, "description"))), new ConcurrentAxis(mRtx, new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "listitem")))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "text")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Test seriell.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testSeriellNew6() {
        /* query: //regions//item/mailbox/mail */
        // final int resultNumber = 20946; //100mb xmark
        final int resultNumber = 544; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "africa"))), new FilterAxis(new DescendantAxis(mRtx, true),
                new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testConcurrent6() {
        /* query: //regions//item/mailbox/mail */
        // final int resultNumber = 20946; //100mb xmark
        final int resultNumber = 544; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(
                new NestedAxis(new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(mRtx, true),
                    new NameFilter(mRtx, "regions"))), new ConcurrentAxis(mRtx, new FilterAxis(new ChildAxis(
                    mRtx), new NameFilter(mRtx, "africa")))), new ConcurrentAxis(mRtx, new FilterAxis(
                    new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))), new ConcurrentAxis(mRtx,
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox")))), new ConcurrentAxis(
                mRtx, new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mail"))));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testPartConcurrent6Axis1() {
        /* query: //regions//item/mailbox/mail */
        // final int resultNumber = 20946; //100mb xmark
        final int resultNumber = 544; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "africa"))), new FilterAxis(
                new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item"))), new FilterAxis(new ChildAxis(
                mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(
                mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testPartConcurrent6Axis2() {
        /* query: //regions//item/mailbox/mail */
        // final int resultNumber = 20946; //100mb xmark
        final int resultNumber = 544; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "regions")), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "africa"))), new ConcurrentAxis(mRtx, new FilterAxis(new DescendantAxis(
                mRtx, true), new NameFilter(mRtx, "item")))), new FilterAxis(new ChildAxis(mRtx),
                new NameFilter(mRtx, "mailbox"))), new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx,
                "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /**
     * Test concurrent.
     * 
     * @throws TTXPathException
     */
    @Bench
    @Ignore
    @SkipBench
    @Test
    public final void testPartConcurrent6Axis1and2() {
        /* query: //regions//item/mailbox/mail */
        // final int resultNumber = 20946; //100mb xmark
        final int resultNumber = 544; // 1000mb xmark

        final AbsAxis axis =
            new NestedAxis(new NestedAxis(new NestedAxis(new NestedAxis(new ConcurrentAxis(mRtx,
                new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "regions"))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "africa"))), new ConcurrentAxis(
                mRtx, new FilterAxis(new DescendantAxis(mRtx, true), new NameFilter(mRtx, "item")))),
                new FilterAxis(new ChildAxis(mRtx), new NameFilter(mRtx, "mailbox"))), new FilterAxis(
                new ChildAxis(mRtx), new NameFilter(mRtx, "mail")));

        for (int i = 0; i < resultNumber; i++) {
            assertEquals(true, axis.hasNext());
        }
        assertEquals(axis.hasNext(), false);

    }

    /* ######################################################################################### */

    /**
     * Close all connections.
     */
    @AfterEachRun
    @After
    public final void tearDown() {
        try {
            mRtx.close();
            mSession.close();
            mDatabase.close();
            TestHelper.closeEverything();
        } catch (final Exception mExe) {
            mExe.printStackTrace();
        }

    }

}
