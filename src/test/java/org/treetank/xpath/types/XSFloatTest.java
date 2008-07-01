
package org.treetank.xpath.types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;

public class XSFloatTest {

  private AtomicValue a, b, c, d, e, f, g, h, i, zero, n_zero, NaN, pInf, nInf, result;

  private XSFloat dInstance;
  
  private boolean boolResult;

  @Before
  public void setUp() {

    dInstance = XSFloat.getInstance();

    a = new AtomicValue(1.0f, Type.FLOAT);
    b = new AtomicValue(2.0f, Type.FLOAT);
    c = new AtomicValue(-3.0f, Type.FLOAT);
    d = new AtomicValue(1.5f, Type.FLOAT);
    e = new AtomicValue(10.2f, Type.FLOAT);
    f = new AtomicValue(5.02f, Type.FLOAT);
    g = new AtomicValue(2.5f, Type.FLOAT);
    h = new AtomicValue(-2.5f, Type.FLOAT);
    i = new AtomicValue(2.4999f, Type.FLOAT);

    zero = new AtomicValue(+0.0f, Type.FLOAT);
    n_zero = new AtomicValue(-0.0f, Type.FLOAT);
    NaN = new AtomicValue(Float.NaN, Type.FLOAT);
    pInf = new AtomicValue(Float.POSITIVE_INFINITY, Type.FLOAT);
    nInf = new AtomicValue(Float.NEGATIVE_INFINITY, Type.FLOAT);

  }

//  @Test
//  public final void testNumericAdd() {
//
//    result = dInstance.add(a, b);
//    assertThat(result.getFLT(), is(1.0f + 2.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(a, c);
//    assertThat(result.getFLT(), is(-3.0f + 1.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(b, d);
//    assertThat(result.getFLT(), is(2.0f + 1.5f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(c, f);
//    assertThat(result.getFLT(), is(-3.0f + 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(d, c);
//    assertThat(result.getFLT(), is(1.5f + (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(e, f);
//    assertThat(result.getFLT(), is(10.2f + 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(pInf, pInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(nInf, nInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(nInf, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(pInf, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(zero, pInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.add(zero, nInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//  }
//
//  @Test
//  public final void testNumericMod() {
//
//    result = dInstance.mod(a, b);
//    assertThat(result.getFLT(), is(1.0f % 2.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(a, c);
//    assertThat(result.getFLT(), is(1.0f % (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(b, d);
//    assertThat(result.getFLT(), is(2.0f % 1.5f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(c, f);
//    assertThat(result.getFLT(), is(-3.0f % 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(d, c);
//    assertThat(result.getFLT(), is(1.5f % (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(e, f);
//    assertThat(result.getFLT(), is(10.2f % 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(NaN, NaN);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(a, NaN);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(NaN, c);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(pInf, a);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(nInf, c);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(pInf, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(nInf, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(a, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(c, n_zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(pInf, n_zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(nInf, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(a, nInf);
//    assertThat(result.getFLT(), is(1.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(c, pInf);
//    assertThat(result.getFLT(), is(-3.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(zero, a);
//    assertThat(result.getFLT(), is(0.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.mod(n_zero, c);
//    assertThat(result.getFLT(), is(-0.0f));
//    assertEquals(result.getType(), "xs:float");
//  }
//
//  @Test
//  public final void testNumericMultiply() {
//
//    result = dInstance.multiply(a, b);
//    assertThat(result.getFLT(), is(1.0f * 2.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(a, c);
//    assertThat(result.getFLT(), is(1.0f * (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(b, d);
//    assertThat(result.getFLT(), is(2.0f * 1.5f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(c, f);
//    assertThat(result.getFLT(), is(-3.0f * 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(d, c);
//    assertThat(result.getFLT(), is(1.5f * (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(e, f);
//    assertThat(result.getFLT(), is(10.2f * 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(pInf, pInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(nInf, nInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(nInf, pInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(pInf, nInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(zero, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(b, pInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(c, pInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(zero, a);
//    assertThat(result.getFLT(), is(0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(zero, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(pInf, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.multiply(nInf, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//  }
//
//  @Test
//  public final void testNumericDivision() {
//
//    result = dInstance.divide(a, b);
//    assertThat(result.getFLT(), is(1.0f / 2.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(a, c);
//    assertThat(result.getFLT(), is(1.0f / (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(b, d);
//    assertThat(result.getFLT(), is(2.0f / 1.5f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(c, f);
//    assertThat(result.getFLT(), is(-3.0f / 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(d, c);
//    assertThat(result.getFLT(), is(1.5f / (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(e, f);
//    assertThat(result.getFLT(), is(10.2f / 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(pInf, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(nInf, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(nInf, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(pInf, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(a, zero);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(c, zero);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(a, n_zero);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(c, n_zero);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(zero, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(zero, n_zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(n_zero, zero);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(zero, a);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(zero, c);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.divide(zero, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//  }
//
//  @Test
//  public final void testNumericSubstract() {
//
//    result = dInstance.substract(a, b);
//    assertThat(result.getFLT(), is(1.0f - 2.0f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(a, c);
//    assertThat(result.getFLT(), is(1.0f - (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(b, d);
//    assertThat(result.getFLT(), is(2.0f - 1.5f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(c, f);
//    assertThat(result.getFLT(), is(-3.0f - 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(d, c);
//    assertThat(result.getFLT(), is(1.5f - (-3.0f)));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(e, f);
//    assertThat(result.getFLT(), is(10.2f - 5.02f));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(pInf, pInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(nInf, nInf);
//    assertThat(result.getFLT(), is(Float.NaN));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(nInf, pInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(pInf, nInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(zero, pInf);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(zero, nInf);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(pInf, zero);
//    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//
//    result = dInstance.substract(nInf, zero);
//    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:float");
//  }

  @Test
   public final void testNumericUnaryMinus() {
     result = dInstance.numericUnaryMinus(a);
     assertThat(result.getFLT(), is(-1.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(c);
     assertThat(result.getFLT(), is(3.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(d);
     assertThat(result.getFLT(), is(-1.5f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(f);
     assertThat(result.getFLT(), is(-5.02f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(e);
     assertThat(result.getFLT(), is(-10.2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(pInf);
     assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(nInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(n_zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.numericUnaryMinus(zero);
     assertThat(result.getFLT(), is(-0f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.numericUnaryMinus(NaN);
     assertThat(result.getFLT(), is(Float.NaN));
     assertEquals(result.getType(), "xs:float");
   }

  //
  @Test
  public final void testNumericEqual() {

    
    boolResult = dInstance.equals(a, b).getBool();
    assertEquals(boolResult, false);

    boolResult = dInstance.equals(a, c).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(b, d).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(c, f).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(d, c).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(e, f).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(pInf, pInf).getBool();
    assertEquals(boolResult, true);
    

    boolResult = dInstance.equals(nInf, nInf).getBool();
    assertEquals(boolResult, true);
    

    boolResult = dInstance.equals(nInf, pInf).getBool();
    assertEquals(boolResult, false);
    

   boolResult = dInstance.equals(zero, pInf).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(zero, nInf).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(pInf, zero).getBool();
    assertEquals(boolResult, false);
    

    boolResult = dInstance.equals(zero, zero).getBool();
    assertEquals(boolResult, true);
    
    
    boolResult = dInstance.equals(zero, n_zero).getBool();
    assertEquals(boolResult, true);
    
    boolResult = dInstance.equals(NaN, NaN).getBool();
    assertEquals(boolResult, false);

  }

  
   @Test
   public final void testNumericGreaterThan() {
     boolResult = dInstance.greaterThan(a, b).getBool();
     assertEquals(boolResult, false);

     boolResult = dInstance.greaterThan(a, c).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(b, d).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(c, f).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.greaterThan(d, c).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(e, f).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(pInf, pInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.greaterThan(nInf, nInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.greaterThan(nInf, pInf).getBool();
     assertEquals(boolResult, false);
     

    boolResult = dInstance.greaterThan(zero, pInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.greaterThan(zero, nInf).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(pInf, zero).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.greaterThan(zero, zero).getBool();
     assertEquals(boolResult, false);
     
     
     boolResult = dInstance.greaterThan(n_zero, zero).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.greaterThan(NaN, NaN).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.greaterThan(NaN, a).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.greaterThan(pInf, NaN).getBool();
     assertEquals(boolResult, false);
   }
  
   @Test
   public final void testNumericLessThan() {
     boolResult = dInstance.lessThan(a, b).getBool();
     assertEquals(boolResult, true);

     boolResult = dInstance.lessThan(a, c).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(b, d).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(c, f).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.lessThan(d, c).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(e, f).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(pInf, pInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(nInf, nInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(nInf, pInf).getBool();
     assertEquals(boolResult, true);
     

    boolResult = dInstance.lessThan(zero, pInf).getBool();
     assertEquals(boolResult, true);
     

     boolResult = dInstance.lessThan(zero, nInf).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(pInf, zero).getBool();
     assertEquals(boolResult, false);
     

     boolResult = dInstance.lessThan(zero, zero).getBool();
     assertEquals(boolResult, false);
     
     
     boolResult = dInstance.lessThan(n_zero, zero).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.lessThan(NaN, NaN).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.lessThan(NaN, a).getBool();
     assertEquals(boolResult, false);
     
     boolResult = dInstance.lessThan(pInf, NaN).getBool();
     assertEquals(boolResult, false);
   }
  
  @Test
  public final void testNumericIntegerDivision() {

    result = dInstance.numericIntegerDivision(a, b);
    assertThat(result.getInt(), is((int) (1.0f / 2.0f)));
    assertThat(result.getInt(), is(0));
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(a, c);
    assertThat(result.getInt(), is((int) (1.0f / (-3.0f))));
    assertThat(result.getInt(), is(0));
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(b, d);
    assertThat(result.getInt(), is((int) (2.0f / 1.5f)));
    assertThat(result.getInt(), is(1));
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(c, f);
    assertThat(result.getInt(), is((int) (-3.0f / 5.02f)));
    assertThat(result.getInt(), is(0));
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(d, c);
    assertThat(result.getInt(), is((int) (1.5f / (-3.0f))));
    assertThat(result.getInt(), is(0));
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(e, f);
    assertThat(result.getInt(), is((int) (10.2f / 5.02f)));
    assertThat(result.getInt(), is(2));
    assertEquals(result.getType(), "xs:integer");

    try {
      result = dInstance.numericIntegerDivision(a, zero);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0001: Division by zero."));
    }

    try {
      result = dInstance.numericIntegerDivision(c, n_zero);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0001: Division by zero."));
    }

    try {
      result = dInstance.numericIntegerDivision(nInf, a);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0002: Numeric operation " +
      		"overflow/underflow."));
    }

    try {
      result = dInstance.numericIntegerDivision(pInf, c);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0002: Numeric operation " +
      		"overflow/underflow."));
    }

    try {
      result = dInstance.numericIntegerDivision(NaN, a);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0002: Numeric operation " +
      		"overflow/underflow."));
    }

    try {
      result = dInstance.numericIntegerDivision(a, NaN);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:FOAR0002: Numeric operation " +
      		"overflow/underflow."));
    }

  }

  
   @Test
   public final void testAbs() {
     result = dInstance.abs(a);
     assertThat(result.getFLT(), is(1.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(c);
     assertThat(result.getFLT(), is(3.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(d);
     assertThat(result.getFLT(), is(1.5f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(f);
     assertThat(result.getFLT(), is(5.02f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(e);
     assertThat(result.getFLT(), is(10.2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(pInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(nInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(n_zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.abs(zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");
   }
  
   @Test
   public final void testCeiling() {

     result = dInstance.ceiling(a);
     assertThat(result.getFLT(), is(1.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(c);
     assertThat(result.getFLT(), is(-3.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(d);
     assertThat(result.getFLT(), is(2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(f);
     assertThat(result.getFLT(), is(6f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(e);
     assertThat(result.getFLT(), is(11f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.ceiling(g);
     assertThat(result.getFLT(), is(3f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.ceiling(h);
     assertThat(result.getFLT(), is(-2f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.ceiling(i);
     assertThat(result.getFLT(), is(3f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(pInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(nInf);
     assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(n_zero);
     assertThat(result.getFLT(), is(-0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.ceiling(zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");
   }
  
   @Test
   public final void testFloor() {

     result = dInstance.floor(a);
     assertThat(result.getFLT(), is(1.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(c);
     assertThat(result.getFLT(), is(-3.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(d);
     assertThat(result.getFLT(), is(1f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(f);
     assertThat(result.getFLT(), is(5.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(e);
     assertThat(result.getFLT(), is(10f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.floor(g);
     assertThat(result.getFLT(), is(2f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.floor(h);
     assertThat(result.getFLT(), is(-3f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.floor(i);
     assertThat(result.getFLT(), is(2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(pInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(nInf);
     assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(n_zero);
     assertThat(result.getFLT(), is(-0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.floor(zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");
   }
  
   @Test
   public final void testRound() {
     result = dInstance.round(a);
     assertThat(result.getFLT(), is(1.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(c);
     assertThat(result.getFLT(), is(-3.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(d);
     assertThat(result.getFLT(), is(2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(f);
     assertThat(result.getFLT(), is(5.0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(e);
     assertThat(result.getFLT(), is(10f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.round(g);
     assertThat(result.getFLT(), is(3f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.round(h);
     assertThat(result.getFLT(), is(-2f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.round(i);
     assertThat(result.getFLT(), is(2f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(n_zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");

     result = dInstance.round(zero);
     assertThat(result.getFLT(), is(0f));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.round(nInf);
     assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");
     
     result = dInstance.round(pInf);
     assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
     assertEquals(result.getType(), "xs:float");


   }
  
//   @Test
//   public final void testRoundHalfToEven() {
//     result = dInstance.roundHalfToEven(a);
//     assertThat(result.getFLT(), is(1.0f));
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(c);
//     assertThat(result.getFLT(), -3.is(0f));
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(d);
//     assertThat(result.getFLT(), 2f);
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(f);
//     assertThat(result.getFLT(), 5.is(0f));
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(e);
//     assertThat(result.getFLT(), 10);
//     assertEquals(result.getType(), "xs:float");
//     
//     result = dInstance.roundHalfToEven(g);
//     assertThat(result.getFLT(), 2f);
//     assertEquals(result.getType(), "xs:float");
//     
//     result = dInstance.roundHalfToEven(h);
//     assertThat(result.getFLT(), -2f);
//     assertEquals(result.getType(), "xs:float");
//     
//     result = dInstance.roundHalfToEven(i);
//     assertThat(result.getFLT(), 2f);
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(n_zero);
//     assertThat(result.getFLT(), -is(0f));
//     assertEquals(result.getType(), "xs:float");
//
//     result = dInstance.roundHalfToEven(zero);
//     assertThat(result.getFLT(), is(0f));
//     assertEquals(result.getType(), "xs:float");
//   }
  //
  // @Test
  // public final void testRoundHalfToEvenPrec() {
  //
  // fail("Not yet implemented"); // TODO
  // }
  //
  @Test
  public final void testNumericUnaryPlus() {

    result = dInstance.numericUnaryPlus(a);
    assertThat(result.getFLT(), is(1.0f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(c);
    assertThat(result.getFLT(), is(-3.0f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(d);
    assertThat(result.getFLT(), is(1.5f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(f);
    assertThat(result.getFLT(), is(5.02f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(e);
    assertThat(result.getFLT(), is(10.2f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(pInf);
    assertThat(result.getFLT(), is(Float.POSITIVE_INFINITY));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(nInf);
    assertThat(result.getFLT(), is(Float.NEGATIVE_INFINITY));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(n_zero);
    assertThat(result.getFLT(), is(-0f));
    assertEquals(result.getType(), "xs:float");

    result = dInstance.numericUnaryPlus(zero);
    assertThat(result.getFLT(), is(0f));
    assertEquals(result.getType(), "xs:float");

  }

}
