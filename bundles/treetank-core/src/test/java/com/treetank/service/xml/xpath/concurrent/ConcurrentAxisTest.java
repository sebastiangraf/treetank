package com.treetank.service.xml.xpath.concurrent;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.NestedAxis;
import com.treetank.axis.filter.NameFilter;
import com.treetank.axis.filter.TextFilter;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.comparators.CompKind;
import com.treetank.service.xml.xpath.comparators.GeneralComp;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.filter.PredicateFilterAxis;
import com.treetank.utils.TypedValue;

import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.SkipBench;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentAxisTest {

    /** XML file name to test. */
    private static final String XMLFILE = "100mb.xml";
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

    /**
     * Test seriell.
     */
    //@Ignore
    //@SkipBench
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
