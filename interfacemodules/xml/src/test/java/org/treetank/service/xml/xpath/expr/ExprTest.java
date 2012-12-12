package org.treetank.service.xml.xpath.expr;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AxisTest;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTXPathException;
import org.treetank.node.AtomicValue;
import org.treetank.node.Type;
import org.treetank.node.interfaces.IValNode;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathError;
import org.treetank.service.xml.xpath.axis.VariableAxis;
import org.treetank.utils.NamePageHash;

import com.google.inject.Inject;

/**
 * Test Cases for all AbsAxis-implementations
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class ExprTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeClass
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        Properties props =
        StandardSettings.getStandardProperties(TestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
            TestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        NodeHelper.createTestDocument(mResource);
        holder = Holder.generateRtx(mResource);
    }


    @AfterClass
    public void tearDown() throws TTException {
        holder.close();
        TestHelper.deleteEverything();
    }

    @Test(dataProvider = "instantiateExpr")
    public void testExpr(Class<IExprChecker> pExprCheckerClass, IExprChecker[] pExprChecker) throws Exception {

        for (int i = 0; i < pExprChecker.length; i++) {
            pExprChecker[i].checkExpr(holder.getNRtx());
            holder.getNRtx().moveTo(org.treetank.node.IConstants.ROOT_NODE);
        }

    }

    @DataProvider(name = "instantiateExpr")
    public Object[][] instantiateExpr() throws TTByteHandleException {

        Object[][] returnVal = {
            {
                IExprChecker.class, new IExprChecker[] {
                    // And Expr Test 1
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) {
                            long iTrue = AbsAxis.addAtomicToItemList(pRtx, new AtomicValue(true));
                            long iFalse = AbsAxis.addAtomicToItemList(pRtx, new AtomicValue(false));

                            AbsAxis trueLit1 = new LiteralExpr(pRtx, iTrue);
                            AbsAxis trueLit2 = new LiteralExpr(pRtx, iTrue);
                            AbsAxis falseLit1 = new LiteralExpr(pRtx, iFalse);
                            AbsAxis falseLit2 = new LiteralExpr(pRtx, iFalse);

                            AbsAxis axis1 = new AndExpr(pRtx, trueLit1, trueLit2);
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            AbsAxis axis2 = new AndExpr(pRtx, trueLit1, falseLit1);
                            assertEquals(true, axis2.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            AbsAxis axis3 = new AndExpr(pRtx, falseLit1, trueLit1);
                            assertEquals(true, axis3.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            AbsAxis axis4 = new AndExpr(pRtx, falseLit1, falseLit2);
                            assertEquals(true, axis4.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                        }
                    },// And Expr Test 2
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            pRtx.moveTo(1L);

                            final AbsAxis axis1 = new XPathAxis(pRtx, "text() and node()");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "comment() and node()");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            final AbsAxis axis3 = new XPathAxis(pRtx, "1 eq 1 and 2 eq 2");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            final AbsAxis axis4 = new XPathAxis(pRtx, "1 eq 1 and 2 eq 3");
                            assertEquals(true, axis4.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                            // is never evaluated.
                            final AbsAxis axis5 = new XPathAxis(pRtx, "1 eq 2 and (3 idiv 0 = 1)");
                            assertEquals(true, axis5.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis5.getNode())
                                .getRawValue())));
                            assertEquals(false, axis5.hasNext());

                            final AbsAxis axis6 = new XPathAxis(pRtx, "1 eq 1 and 3 idiv 0 = 1");
                            try {
                                assertEquals(true, axis6.hasNext());
                                Assert.fail("Expected XPath exception, because of division by zero");
                            } catch (XPathError e) {
                                assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
                            }

                        }
                    },// Castable Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            final AbsAxis axis1 = new XPathAxis(pRtx, "1 castable as xs:decimal");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis1.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "10.0 castable as xs:anyAtomicType");
                            try {
                                assertEquals(true, axis2.hasNext());
                            } catch (XPathError e) {
                                assertEquals(e.getMessage(), "err:XPST0080 "
                                    + "Target type of a cast or castable expression must not be "
                                    + "xs:NOTATION or xs:anyAtomicType.");
                            }

                            // Token is not implemented yet.
                            // final IAxis axis3 = new XPathAxis(holder.getRtx(),
                            // "\"hello\" castable as xs:token");
                            // assertEquals(true, axis3.hasNext());
                            // assertEquals(Type.BOOLEAN, holder.getRtx().getValueTypeAsType());
                            // assertEquals(true, holder.getRtx().getValueAsBoolean());
                            // assertEquals(false, axis3.hasNext());

                            final AbsAxis axis4 = new XPathAxis(pRtx, "\"hello\" castable as xs:string");
                            assertEquals(true, axis4.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis4.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                            // final IAxis axis5 = new XPathAxis(holder.getRtx(),
                            // "\"hello\" castable as xs:decimal");
                            // assertEquals(true, axis5.hasNext());
                            // assertEquals(holder.getRtx().keyForName("xs:boolean"),
                            // holder.getRtx().getTypeKey());
                            // assertEquals(true, Boolean.parseBoolean(holder.getRtx().getValue()));
                            // assertEquals(false, axis5.hasNext());

                        }
                    },// Comp Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            final AbsAxis axis1 = new XPathAxis(pRtx, "1.0 = 1.0");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "(1, 2, 3) < (2, 3)");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            final AbsAxis axis3 = new XPathAxis(pRtx, "(1, 2, 3) > (3, 4)");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                        }
                    }, // Every Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            final AbsAxis axis1 =
                                new XPathAxis(pRtx, "every $child in child::node()" + "satisfies $child/@i");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 =
                                new XPathAxis(pRtx, "every $child in child::node()" + "satisfies $child/@abc");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            pRtx.moveTo(1L);
                            final AbsAxis axis3 =
                                new XPathAxis(pRtx, "every $child in child::element()"
                                    + " satisfies $child/attribute::attribute()");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            pRtx.moveTo(1L);
                            final AbsAxis axis4 =
                                new XPathAxis(pRtx,
                                    "every $child in child::element() satisfies $child/child::c");
                            assertEquals(true, axis4.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                        }
                    }, // Except Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "child::node() except b"),
                                new long[] {
                                    4L, 8L, 13L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node() except child::node()[attribute::p:x]"), new long[] {
                                4L, 5L, 8L, 13L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() except self::node()"), new long[] {});

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "//node() except //text()"),
                                new long[] {
                                    1L, 5L, 9L, 7L, 11L
                                });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "b/preceding::node() except text()"), new long[] {
                                7L, 6L, 5L
                            });

                        }
                    }, // For Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "for $a in child::text() return child::node()"), new long[] {
                                4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "for $a in child::node() return $a/node()"), new long[] {
                                6L, 7L, 11L, 12L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "for $a in child::node() return $a/text()"), new long[] {
                                6L, 12L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "for $a in child::node() return $a/c"), new long[] {
                                7L, 11L
                            });

                            // IAxisTest.testIAxisConventions(new XPathAxis(
                            // pRtx,
                            // "for $a in child::node(), $b in /node(), $c in ., $d in /c return $a/c"),
                            // new long[] {7L, 11L});

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "for $a in child::node() return $a[@p:x]"), new long[] {
                                9L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "for $a in . return $a"),
                                new long[] {
                                    1L
                                });

                            final AbsAxis axis =
                                new XPathAxis(pRtx, "for $i in (10, 20), $j in (1, 2) return ($i + $j)");
                            assertEquals(true, axis.hasNext());

                            assertEquals("11.0", new String(((IValNode)axis.getNode()).getRawValue()));
                            assertEquals(true, axis.hasNext());
                            assertEquals("12.0", new String(((IValNode)axis.getNode()).getRawValue()));
                            assertEquals(true, axis.hasNext());
                            assertEquals("21.0", new String(((IValNode)axis.getNode()).getRawValue()));
                            assertEquals(true, axis.hasNext());
                            assertEquals("22.0", new String(((IValNode)axis.getNode()).getRawValue()));
                            assertEquals(false, axis.hasNext());

                        }
                    },// Function Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            pRtx.moveTo(1L);

                            final AbsAxis axis1 = new XPathAxis(pRtx, "fn:count(text())");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(3, Integer.parseInt(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "fn:count(//node())");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(10, Integer.parseInt(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            final AbsAxis axis3 = new XPathAxis(pRtx, "fn:string(//node())");
                            assertEquals(true, axis3.hasNext());
                            assertEquals("oops1 foo oops2 bar oops3 oops1 foo oops2 bar oops3 foo bar",
                                new String(((IValNode)axis3.getNode()).getRawValue()));
                            ;
                            assertEquals(false, axis3.hasNext());

                            final AbsAxis axis4 = new XPathAxis(pRtx, "fn:string()");
                            assertEquals(true, axis4.hasNext());
                            assertEquals("oops1 foo oops2 bar oops3", new String(((IValNode)axis4.getNode())
                                .getRawValue()));
                            assertEquals(false, axis4.hasNext());

                            final AbsAxis axis5 = new XPathAxis(pRtx, "fn:string(./attribute::attribute())");
                            assertEquals(true, axis5.hasNext());
                            assertEquals("j", new String(((IValNode)axis5.getNode()).getRawValue()));
                            assertEquals(false, axis5.hasNext());

                            pRtx.moveToAttribute(0);
                            final AbsAxis axis6 = new XPathAxis(pRtx, "fn:string()");
                            assertEquals(true, axis6.hasNext());
                            assertEquals("j", new String(((IValNode)axis6.getNode()).getRawValue()));
                            assertEquals(false, axis6.hasNext());

                        }
                    }, // If Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "if (text()) then . else child::node()"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "if (node()) then . else child::node()"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "if (processing-instruction()) then . else child::node()"), new long[] {
                                4L, 5L, 8L, 9L, 13L
                            });

                        }
                    }, // Instance Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            final AbsAxis axis1 = new XPathAxis(pRtx, "1 instance of xs:integer");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis1.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "\"hallo\" instance of xs:integer");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis2.getNode()
                                .getTypeKey());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            final AbsAxis axis3 = new XPathAxis(pRtx, "\"hallo\" instance of xs:string ?");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis3.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            final AbsAxis axis4 = new XPathAxis(pRtx, "\"hallo\" instance of xs:string +");
                            assertEquals(true, axis4.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis4.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                            final AbsAxis axis5 = new XPathAxis(pRtx, "\"hallo\" instance of xs:string *");
                            assertEquals(true, axis5.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis5.getNode()
                                .getTypeKey());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis5.getNode())
                                .getRawValue())));
                            assertEquals(false, axis5.hasNext());

                        }
                    }, // Intersect Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "child::node() intersect b"),
                                new long[] {
                                    5L, 9L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node() intersect b intersect child::node()[@p:x]"), new long[] {
                                9L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node() intersect child::node()[attribute::p:x]"), new long[] {
                                9L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() intersect self::node()"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "//node() intersect //text()"),
                                new long[] {
                                    4L, 8L, 13L, 6L, 12L
                                });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "b/preceding::node() intersect text()"), new long[] {
                                4L, 8L
                            });

                        }
                    }, // Literal Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) {

                            final AtomicValue item1 = new AtomicValue(false);
                            final AtomicValue item2 = new AtomicValue(14, Type.INTEGER);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            final AbsAxis axis1 = new LiteralExpr(pRtx, key1);

                            assertEquals(true, axis1.hasNext());
                            assertEquals(key1, axis1.getNode().getNodeKey());
                            assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis1.getNode()
                                .getTypeKey());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new LiteralExpr(pRtx, key2);
                            assertEquals(true, axis2.hasNext());
                            assertEquals(key2, axis2.getNode().getNodeKey());
                            assertEquals(NamePageHash.generateHashForString("xs:integer"), axis2.getNode()
                                .getTypeKey());
                            assertEquals(14, Integer.parseInt(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                        }
                    }, // Or Expr Test 1
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) {
                            long iTrue = AbsAxis.addAtomicToItemList(pRtx, new AtomicValue(true));
                            long iFalse = AbsAxis.addAtomicToItemList(pRtx, new AtomicValue(false));

                            AbsAxis trueLit1 = new LiteralExpr(pRtx, iTrue);
                            AbsAxis trueLit2 = new LiteralExpr(pRtx, iTrue);
                            AbsAxis falseLit1 = new LiteralExpr(pRtx, iFalse);
                            AbsAxis falseLit2 = new LiteralExpr(pRtx, iFalse);

                            AbsAxis axis1 = new OrExpr(pRtx, trueLit1, trueLit2);
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            AbsAxis axis2 = new OrExpr(pRtx, trueLit1, falseLit1);
                            assertEquals(true, axis2.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            AbsAxis axis3 = new OrExpr(pRtx, falseLit1, trueLit1);
                            assertEquals(true, axis3.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            AbsAxis axis4 = new OrExpr(pRtx, falseLit1, falseLit2);
                            assertEquals(true, axis4.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                        }
                    }, // Or Expr test 2
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {
                            pRtx.moveTo(1L);

                            final AbsAxis axis1 = new XPathAxis(pRtx, "text() or node()");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 = new XPathAxis(pRtx, "comment() or node()");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            final AbsAxis axis3 = new XPathAxis(pRtx, "1 eq 1 or 2 eq 2");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());

                            final AbsAxis axis4 = new XPathAxis(pRtx, "1 eq 1 or 2 eq 3");
                            assertEquals(true, axis4.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis4.getNode())
                                .getRawValue())));
                            assertEquals(false, axis4.hasNext());

                            final AbsAxis axis5 = new XPathAxis(pRtx, "1 eq 2 or (3 idiv 0 = 1)");
                            try {
                                assertEquals(true, axis5.hasNext());
                                assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis5
                                    .getNode()).getRawValue())));
                                assertEquals(false, axis5.hasNext());
                                Assert.fail("Exprected XPathError");
                            } catch (XPathError e) {
                                assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
                            }

                            final AbsAxis axis6 = new XPathAxis(pRtx, "1 eq 1 or (3 idiv 0 = 1)");
                            assertEquals(true, axis6.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis6.getNode())
                                .getRawValue())));

                        }
                    },// Some Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            final AbsAxis axis1 =
                                new XPathAxis(pRtx, "some $child in child::node() satisfies $child/@i");
                            assertEquals(true, axis1.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode())
                                .getRawValue())));
                            assertEquals(false, axis1.hasNext());

                            final AbsAxis axis2 =
                                new XPathAxis(pRtx, "some $child in child::node() satisfies $child/@abc");
                            assertEquals(true, axis2.hasNext());
                            assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode())
                                .getRawValue())));
                            assertEquals(false, axis2.hasNext());

                            pRtx.moveTo(1L);
                            final AbsAxis axis3 =
                                new XPathAxis(pRtx,
                                    "some $child in child::node() satisfies $child/attribute::attribute()");
                            assertEquals(true, axis3.hasNext());
                            assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode())
                                .getRawValue())));
                            assertEquals(false, axis3.hasNext());
                        }
                    }, // Union Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() union child::node()"), new long[] {
                                1L, 4L, 5L, 8L, 9L, 13L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() | child::node()"), new long[] {
                                1L, 4L, 5L, 8L, 9L, 13L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() | child::node() | self::node()"), new long[] {
                                1L, 4L, 5L, 8L, 9L, 13L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::node()/parent::node() | child::node() | self::node()"
                                    + "union parent::node()"), new long[] {
                                1L, 4L, 5L, 8L, 9L, 13L, 0L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "b/preceding::node() union text() | descendant::node()"), new long[] {
                                4L, 8L, 7L, 6L, 5L, 13L, 9L, 11L, 12L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "//c/ancestor::node() | //node()"), new long[] {
                                5L, 1L, 9L, 4L, 8L, 13L, 6L, 7L, 11L, 12L
                            });

                        }
                    }, // VarRef Expr Test
                    new IExprChecker() {

                        @Override
                        public void checkExpr(INodeReadTrx pRtx) throws TTXPathException {

                            final AbsAxis axis = new XPathAxis(pRtx, "for $a in b return $a");

                            final VariableAxis variable = new VariableAxis(pRtx, axis);

                            final VarRefExpr axis1 = new VarRefExpr(pRtx, variable);
                            // assertEquals(false, axis1.hasNext());
                            axis1.update(5L);
                            assertEquals(true, axis1.hasNext());
                            assertEquals(5L, pRtx.getNode().getNodeKey());
                            axis1.update(13L);
                            assertEquals(true, axis1.hasNext());
                            assertEquals(13L, pRtx.getNode().getNodeKey());
                            axis1.update(1L);
                            assertEquals(true, axis1.hasNext());
                            assertEquals(1L, pRtx.getNode().getNodeKey());
                            assertEquals(false, axis1.hasNext());

                            final VarRefExpr axis2 = new VarRefExpr(pRtx, variable);
                            // assertEquals(false, axis2.hasNext());
                            axis2.update(13L);
                            assertEquals(true, axis2.hasNext());
                            assertEquals(13L, pRtx.getNode().getNodeKey());
                            assertEquals(false, axis2.hasNext());
                            axis2.update(12L);
                            assertEquals(true, axis2.hasNext());
                            assertEquals(12L, pRtx.getNode().getNodeKey());
                            assertEquals(false, axis2.hasNext());
                        }
                    }
                }
            }
        };
        return returnVal;
    }

    /**
     * Interface to check axis.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IExprChecker {
        void checkExpr(INodeReadTrx pRtx) throws TTException;

    }

}
