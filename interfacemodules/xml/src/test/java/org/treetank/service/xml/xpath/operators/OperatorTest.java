package org.treetank.service.xml.xpath.operators;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.Holder;
import org.treetank.ModuleFactory;
import org.treetank.NodeTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.TTException;
import org.treetank.exception.TTXPathException;
import org.treetank.node.AtomicValue;
import org.treetank.node.Type;
import org.treetank.node.interfaces.IValNode;
import org.treetank.service.xml.xpath.XPathError;
import org.treetank.service.xml.xpath.axis.SequenceAxis;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.utils.NamePageHash;

import com.google.inject.Inject;

@Guice(moduleFactory = ModuleFactory.class)
public class OperatorTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeClass
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        NodeTestHelper.createTestDocument(mResource);
        this.holder = Holder.generateRtx(holder, mResource);
    }

    @AfterClass
    public void tearDown() throws TTException {
        holder.close();
        CoreTestHelper.deleteEverything();
    }

    @Test(dataProvider = "instantiateOperator")
    public void testOperator(Class<IOperatorChecker> pOperatorCheckerClass,
        IOperatorChecker[] pOperatorChecker) throws Exception {

        for (int i = 0; i < pOperatorChecker.length; i++) {
            pOperatorChecker[i].checkOperator(holder.getNRtx());

        }

    }

    @DataProvider(name = "instantiateOperator")
    public Object[][] instantiateOperator() {

        Object[][] returnVal = {
            {
                IOperatorChecker.class, new IOperatorChecker[] {
                    // AbsObFilter
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AtomicValue item1 = new AtomicValue(1.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new DivOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode()
                                .getTypeKey());
                            assertEquals(Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())), 0.5);
                            assertEquals(false, axis.hasNext());

                        }
                    }, // Add Op Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AtomicValue item1 = new AtomicValue(1.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new AddOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(3.0, Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())));
                            assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }
                    }, // Add Op Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new AddOpAxis(pRtx, op1, op2);

                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(NamePageHash.generateHashForString("xs:integer"),
                            // NamePageHash.generateHashForString("xs:integer")));

                            assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:yearMonthDuration"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dayTimeDuration"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));

                            assertEquals(Type.DATE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:date"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DATE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:date"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));
                            assertEquals(Type.TIME, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:time"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));
                            assertEquals(Type.DATE_TIME, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dateTime"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DATE_TIME, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dateTime"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:dateTime"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (final TTXPathException e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules. ");
                            }

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (final TTXPathException e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules. ");
                            }

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (final TTXPathException e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules. ");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:IDREF"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (final TTXPathException e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules. ");
                            }
                        }
                    }, // Div Op Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AtomicValue item1 = new AtomicValue(1.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new DivOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(0.5, Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())));
                            assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }
                    }, // Div Op Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new DivOpAxis(pRtx, op1, op2);

                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(holder.getRtx().keyForName("xs:integer"),
                            // holder.getRtx().keyForName("xs:integer")));
                            assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:yearMonthDuration"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dayTimeDuration"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:yearMonthDuration"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dayTimeDuration"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:IDREF"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }
                        }
                    }, // IDivOp Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AtomicValue item1 = new AtomicValue(3.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new IDivOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            // note: although getRawValue() returns [1], parseString returns ""
                            // assertEquals(1,
                            // Integer.parseInt(TypedValue.parseString(holder.getRtx().getRawValue())));
                            assertEquals(NamePageHash.generateHashForString("xs:integer"), axis.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }
                    }, // IDivOp Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {

                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new IDivOpAxis(pRtx, op1, op2);

                            assertEquals(Type.INTEGER, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.INTEGER, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.INTEGER, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.INTEGER, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(NamePageHash.generateHashForString("xs:integer"),
                            // NamePageHash.generateHashForString("xs:integer")));

                            try {
                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:IDREF"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                        }
                    }, // ModOp Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {

                            AtomicValue item1 = new AtomicValue(3.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new ModOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())), 1.0);
                            assertEquals(NamePageHash.generateHashForString("xs:double"), op1.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }
                    }, // ModOp Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new ModOpAxis(pRtx, op1, op2);

                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(NamePageHash.generateHashForString("xs:integer"),
                            // NamePageHash.generateHashForString("xs:integer")));

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:IDREF"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                        }
                    }, // MulOp Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {

                            AtomicValue item1 = new AtomicValue(3.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new MulOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(6.0, Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())));
                            assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }

                    }, // MulOp Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {

                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new MulOpAxis(pRtx, op1, op2);

                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(NamePageHash.generateHashForString("xs:integer"),
                            // NamePageHash.generateHashForString("xs:integer")));
                            assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:yearMonthDuration"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:integer"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dayTimeDuration"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:integer"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"), NamePageHash
                                        .generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                        }
                    }, // SubOpAxis Test 1
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {
                            AtomicValue item1 = new AtomicValue(1.0, Type.DOUBLE);
                            AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

                            final int key1 = AbsAxis.addAtomicToItemList(pRtx, item1);
                            final int key2 = AbsAxis.addAtomicToItemList(pRtx, item2);

                            AbsAxis op1 = new LiteralExpr(pRtx, key1);
                            AbsAxis op2 = new LiteralExpr(pRtx, key2);
                            AbsObAxis axis = new SubOpAxis(pRtx, op1, op2);

                            assertEquals(true, axis.hasNext());
                            assertEquals(-1.0, Double.parseDouble(new String(((IValNode)axis.getNode())
                                .getRawValue())));
                            assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode()
                                .getTypeKey());
                            assertEquals(false, axis.hasNext());
                        }
                    }, // SubOpAxis Test 2
                    new IOperatorChecker() {

                        @Override
                        public void checkOperator(INodeReadTrx pRtx) throws TTXPathException {

                            AbsAxis op1 = new SequenceAxis(pRtx);
                            AbsAxis op2 = new SequenceAxis(pRtx);
                            AbsObAxis axis = new SubOpAxis(pRtx, op1, op2);

                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:double"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:double")));
                            assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:float"), NamePageHash
                                .generateHashForString("xs:decimal")));
                            assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:decimal"), NamePageHash
                                .generateHashForString("xs:integer")));
                            // assertEquals(Type.INTEGER,
                            // axis.getReturnType(holder.getRtx().keyForName("xs:integer"),
                            // holder.getRtx().keyForName("xs:integer")));

                            assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:yearMonthDuration"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dayTimeDuration"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:date"), NamePageHash
                                .generateHashForString("xs:date")));
                            assertEquals(Type.DATE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:date"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DATE, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:date"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:time"), NamePageHash
                                .generateHashForString("xs:time")));
                            assertEquals(Type.DATE_TIME, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dateTime"), NamePageHash
                                .generateHashForString("xs:yearMonthDuration")));
                            assertEquals(Type.DATE_TIME, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dateTime"), NamePageHash
                                .generateHashForString("xs:dayTimeDuration")));
                            assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
                                .generateHashForString("xs:dateTime"), NamePageHash
                                .generateHashForString("xs:dateTime")));

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:double"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:string"),
                                    NamePageHash.generateHashForString("xs:yearMonthDuration"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

                            try {

                                axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"),
                                    NamePageHash.generateHashForString("xs:IDREF"));
                                Assert.fail("Expected an XPathError-Exception.");
                            } catch (XPathError e) {
                                assertEquals(
                                    e.getMessage(),
                                    "err:XPTY0004 The type is not appropriate the expression or the "
                                        + "typedoes not match a required type as specified by the matching rules.");
                            }

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
    interface IOperatorChecker {
        void checkOperator(INodeReadTrx pRtx) throws TTXPathException;

    }

}
