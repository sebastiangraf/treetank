
package org.treetank.xpath.types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;

public class XSDoubleTest {

  private AtomicValue a, b, c, d, e, f, g, h, i, zero, n_zero, NaN, pInf, nInf,
      result;

  private XSDouble dInstance;

  private boolean boolResult;

  @Before
  public void setUp() {

    dInstance = XSDouble.getInstance();

    a = new AtomicValue(1.0d, Type.DOUBLE);
    b = new AtomicValue(2.0d, Type.DOUBLE);
    c = new AtomicValue(-3.0d, Type.DOUBLE);
    d = new AtomicValue(1.5d, Type.DOUBLE);
    e = new AtomicValue(10.2d, Type.DOUBLE);
    f = new AtomicValue(5.02d, Type.DOUBLE);

    g = new AtomicValue(2.5d, Type.DOUBLE);
    h = new AtomicValue(-2.5d, Type.DOUBLE);
    i = new AtomicValue(2.4999d, Type.DOUBLE);

    zero = new AtomicValue(+0.0d, Type.DOUBLE);
    n_zero = new AtomicValue(-0.0d, Type.DOUBLE);
    NaN = new AtomicValue(Double.NaN, Type.DOUBLE);
    pInf = new AtomicValue(Double.POSITIVE_INFINITY, Type.DOUBLE);
    nInf = new AtomicValue(Double.NEGATIVE_INFINITY, Type.DOUBLE);

  }

//  @Test
//  public final void testNumericAdd() {
//
//    result = dInstance.add(a, b);
//    assertThat(result.getDBL(), is(1.0d + 2.0d));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(a, c);
//    assertThat(result.getDBL(), is(-3.0 + 1.0));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(b, d);
//    assertThat(result.getDBL(), is(2.0d + 1.5));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(c, f);
//    assertThat(result.getDBL(), is(-3.0d + 5.02d));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(d, c);
//    assertThat(result.getDBL(), is(1.5d + (-3.0d)));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(e, f);
//    assertThat(result.getDBL(), is(10.2 + 5.02));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(pInf, pInf);
//    assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(nInf, nInf);
//    assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(nInf, pInf);
//    assertThat(result.getDBL(), is(Double.NaN));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(pInf, nInf);
//    assertThat(result.getDBL(), is(Double.NaN));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(zero, pInf);
//    assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//    assertEquals(result.getType(), "xs:double");
//
//    result = dInstance.add(zero, nInf);
//    assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//    assertEquals(result.getType(), "xs:double");
//
//  }
  
//   @Test
//   public final void testNumericMod() {
//  
//   result = dInstance.mod(a, b);
//   assertThat(result.getDBL(), is(1.0d % 2.0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(a, c);
//   assertThat(result.getDBL(), is(1.0d % (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(b, d);
//   assertThat(result.getDBL(), is(2.0d % 1.5));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(c, f);
//   assertThat(result.getDBL(), is(-3.0d % 5.02d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(d, c);
//   assertThat(result.getDBL(), is(1.5d % (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(e, f);
//   assertThat(result.getDBL(), is(10.2 % 5.02));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(NaN, NaN);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(a, NaN);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(NaN, c);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(pInf, a);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(nInf, c);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(pInf, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(nInf, nInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(a, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(c, n_zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(pInf, n_zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(nInf, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(a, nInf);
//   assertThat(result.getDBL(), is(1.0));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(c, pInf);
//   assertThat(result.getDBL(), is(-3.0));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(zero, a);
//   assertThat(result.getDBL(), is(0.0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.mod(n_zero, c);
//   assertThat(result.getDBL(), is(-0.0d));
//   assertEquals(result.getType(), "xs:double");
//   }
//  
//   @Test
//   public final void testNumericMultiply() {
//  
//   result = dInstance.multiply(a, b);
//   assertThat(result.getDBL(), is(1.0d * 2.0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(a, c);
//   assertThat(result.getDBL(), is(1.0d * (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(b, d);
//   assertThat(result.getDBL(), is(2.0d * 1.5));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(c, f);
//   assertThat(result.getDBL(), is(-3.0d * 5.02d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(d, c);
//   assertThat(result.getDBL(), is(1.5d * (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(e, f);
//   assertThat(result.getDBL(), is(10.2 * 5.02));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(pInf, pInf);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(nInf, nInf);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(nInf, pInf);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(pInf, nInf);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(zero, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(b, pInf);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(c, pInf);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(zero, a);
//   assertThat(result.getDBL(), is(0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(zero, nInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(pInf, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.multiply(nInf, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//   }
//  
//   @Test
//   public final void testNumericDivision() {
//  
//   result = dInstance.divide(a, b);
//   assertThat(result.getDBL(), is(1.0d / 2.0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(a, c);
//   assertThat(result.getDBL(), is(1.0d / (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(b, d);
//   assertThat(result.getDBL(), is(2.0d / 1.5));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(c, f);
//   assertThat(result.getDBL(), is(-3.0d / 5.02d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(d, c);
//   assertThat(result.getDBL(), is(1.5d / (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(e, f);
//   assertThat(result.getDBL(), is(10.2 / 5.02));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(pInf, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(nInf, nInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(nInf, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(pInf, nInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(a, zero);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(c, zero);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(a, n_zero);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(c, n_zero);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(zero, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(zero, n_zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(n_zero, zero);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   //TODO
//   result = dInstance.divide(zero, a);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(zero, c);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.divide(zero, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   }
//  
//   @Test
//   public final void testNumericSubstract() {
//  
//   result = dInstance.substract(a, b);
//   assertThat(result.getDBL(), is(1.0d - 2.0d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(a, c);
//   assertThat(result.getDBL(), is(1.0d - (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(b, d);
//   assertThat(result.getDBL(), is(2.0d - 1.5));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(c, f);
//   assertThat(result.getDBL(), is(-3.0d - 5.02d));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(d, c);
//   assertThat(result.getDBL(), is(1.5d - (-3.0d)));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(e, f);
//   assertThat(result.getDBL(), is(10.2 - 5.02));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(pInf, pInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(nInf, nInf);
//   assertThat(result.getDBL(), is(Double.NaN));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(nInf, pInf);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(pInf, nInf);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(zero, pInf);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(zero, nInf);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(pInf, zero);
//   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//  
//   result = dInstance.substract(nInf, zero);
//   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
//   assertEquals(result.getType(), "xs:double");
//   }
  
   @Test
   public final void testNumericUnaryMinus() {
   result = dInstance.numericUnaryMinus(a);
   assertThat(result.getDBL(), is(-1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(c);
   assertThat(result.getDBL(), is(3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(d);
   assertThat(result.getDBL(), is(-1.5));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(f);
   assertThat(result.getDBL(), is(-5.02d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(e);
   assertThat(result.getDBL(), is(-10.2));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(pInf);
   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(nInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(n_zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryMinus(zero);
   assertThat(result.getDBL(), is(-0.0));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.numericUnaryMinus(NaN);
   assertThat(result.getDBL(), is(Double.NaN));
   assertEquals(result.getType(), "xs:double");
   }
  
//   //
//   @Test
//   public final void testNumericEqual() {
//  
//      
//   boolResult = dInstance.equals(a, b).getBool();
//   assertFalse(boolResult);
//  
//   boolResult = dInstance.equals(a, c).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(b, d).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(c, f).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(d, c).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(e, f).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(pInf, pInf).getBool();
//   assertTrue(boolResult);
//      
//  
//   boolResult = dInstance.equals(nInf, nInf).getBool();
//   assertTrue(boolResult);
//      
//  
//   boolResult = dInstance.equals(nInf, pInf).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(zero, pInf).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(zero, nInf).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(pInf, zero).getBool();
//   assertFalse(boolResult);
//      
//  
//   boolResult = dInstance.equals(zero, zero).getBool();
//   assertTrue(boolResult);
//      
//      
//   boolResult = dInstance.equals(zero, n_zero).getBool();
//   assertTrue(boolResult);
//      
//   boolResult = dInstance.equals(NaN, NaN).getBool();
//   assertFalse(boolResult);
//  
//   }
//  
//    
//   @Test
//   public final void testNumericGreaterThan() {
//   boolResult = dInstance.greaterThan(a, b).getBool();
//   assertFalse(boolResult);
//  
//   boolResult = dInstance.greaterThan(a, c).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(b, d).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(c, f).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(d, c).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(e, f).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(pInf, pInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(nInf, nInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(nInf, pInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(zero, pInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(zero, nInf).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(pInf, zero).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.greaterThan(zero, zero).getBool();
//   assertFalse(boolResult);
//       
//       
//   boolResult = dInstance.greaterThan(n_zero, zero).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.greaterThan(NaN, NaN).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.greaterThan(NaN, a).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.greaterThan(pInf, NaN).getBool();
//   assertFalse(boolResult);
//   }
//    
//   @Test
//   public final void testNumericLessThan() {
//   boolResult = dInstance.lessThan(a, b).getBool();
//   assertTrue(boolResult);
//  
//   boolResult = dInstance.lessThan(a, c).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(b, d).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(c, f).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(d, c).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(e, f).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(pInf, pInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(nInf, nInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(nInf, pInf).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(zero, pInf).getBool();
//   assertTrue(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(zero, nInf).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(pInf, zero).getBool();
//   assertFalse(boolResult);
//       
//  
//   boolResult = dInstance.lessThan(zero, zero).getBool();
//   assertFalse(boolResult);
//       
//       
//   boolResult = dInstance.lessThan(n_zero, zero).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.lessThan(NaN, NaN).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.lessThan(NaN, a).getBool();
//   assertFalse(boolResult);
//       
//   boolResult = dInstance.lessThan(pInf, NaN).getBool();
//   assertFalse(boolResult);
//   }
    
   @Test
   public final void testNumericIntegerDivision() {
  
   result = dInstance.numericIntegerDivision(a, b);
   assertThat(result.getInt(), is((int) (1.0d / 2.0d)));
   assertThat(result.getInt(), is(0));
   assertEquals(result.getType(), "xs:integer");
  
   result = dInstance.numericIntegerDivision(a, c);
   assertThat(result.getInt(), is((int) (1.0d / (-3.0d))));
   assertThat(result.getInt(), is(0));
   assertEquals(result.getType(), "xs:integer");
  
   result = dInstance.numericIntegerDivision(b, d);
   assertThat(result.getInt(), is((int) (2.0d / 1.5)));
   assertThat(result.getInt(), is(1));
   assertEquals(result.getType(), "xs:integer");
  
   result = dInstance.numericIntegerDivision(c, f);
   assertThat(result.getInt(), is((int) (-3.0d / 5.02d)));
   assertThat(result.getInt(), is(0));
   assertEquals(result.getType(), "xs:integer");
  
   result = dInstance.numericIntegerDivision(d, c);
   assertThat(result.getInt(), is((int) (1.5d / (-3.0d))));
   assertThat(result.getInt(), is(0));
   assertEquals(result.getType(), "xs:integer");
  
   result = dInstance.numericIntegerDivision(e, f);
   assertThat(result.getInt(), is((int) (10.2 / 5.02)));
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
   assertThat(result.getDBL(), is(1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(c);
   assertThat(result.getDBL(), is(3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(d);
   assertThat(result.getDBL(), is(1.5));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(f);
   assertThat(result.getDBL(), is(5.02d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(e);
   assertThat(result.getDBL(), is(10.2));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(pInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(nInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(n_zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.abs(zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
   }
    
   @Test
   public final void testCeiling() {
  
   result = dInstance.ceiling(a);
   assertThat(result.getDBL(), is(1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(c);
   assertThat(result.getDBL(), is(-3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(d);
   assertThat(result.getDBL(), is(2.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(f);
   assertThat(result.getDBL(), is(6d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(e);
   assertThat(result.getDBL(), is(11.0));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.ceiling(g);
   assertThat(result.getDBL(), is(3d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.ceiling(h);
   assertThat(result.getDBL(), is(-2d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.ceiling(i);
   assertThat(result.getDBL(), is(3d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(pInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(nInf);
   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(n_zero);
   assertThat(result.getDBL(), is(-0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.ceiling(zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
   }
    
   @Test
   public final void testFloor() {
   result = dInstance.floor(a);
   assertThat(result.getDBL(), is(1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(c);
   assertThat(result.getDBL(), is(-3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(d);
   assertThat(result.getDBL(), is(1.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(f);
   assertThat(result.getDBL(), is(5d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(e);
   assertThat(result.getDBL(), is(10d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.floor(g);
   assertThat(result.getDBL(), is(2d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.floor(h);
   assertThat(result.getDBL(), is(-3d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.floor(i);
   assertThat(result.getDBL(), is(2d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(pInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(nInf);
   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(n_zero);
   assertThat(result.getDBL(), is(-0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.floor(zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
   }
    
   @Test
   public final void testRound() {
  
   result = dInstance.round(a);
   assertThat(result.getDBL(), is(1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(c);
   assertThat(result.getDBL(), is(-3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(d);
   assertThat(result.getDBL(), is(2d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(f);
   assertThat(result.getDBL(), is(5d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(e);
   assertThat(result.getDBL(), is(10d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.round(g);
   assertThat(result.getDBL(), is(3d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.round(h);
   assertThat(result.getDBL(), is(-2d));
   assertEquals(result.getType(), "xs:double");
       
   result = dInstance.round(i);
   assertThat(result.getDBL(), is(2d));
   assertEquals(result.getType(), "xs:double");
   
   result = dInstance.round(nInf);
   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(pInf);
   assertEquals(result.getDBL(), Double.POSITIVE_INFINITY, 1e-8);
   assertEquals(result.getType(), "xs:double");
  
   
  
   result = dInstance.round(n_zero);
   assertThat(result.getDBL(),is(0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.round(zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
   }
   //
   // @Test
   // public final void testRoundHalfToEvenAtomicValue() {
   //
   // fail("Not yet implemented"); // TODO
   // }
   //
   // @Test
   // public final void testRoundHalfToEvenAtomicValueInt() {
   //
   // fail("Not yet implemented"); // TODO
   // }
   //
   @Test
   public final void testNumericUnaryPlus() {
  
   result = dInstance.numericUnaryPlus(a);
   assertThat(result.getDBL(), is(1.0d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(c);
   assertThat(result.getDBL(), is(-3.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(d);
   assertThat(result.getDBL(), is(1.5));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(f);
   assertThat(result.getDBL(), is(5.02d));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(e);
   assertThat(result.getDBL(), is(10.2));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(pInf);
   assertThat(result.getDBL(), is(Double.POSITIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(nInf);
   assertThat(result.getDBL(), is(Double.NEGATIVE_INFINITY));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(n_zero);
   assertThat(result.getDBL(), is(-0.0));
   assertEquals(result.getType(), "xs:double");
  
   result = dInstance.numericUnaryPlus(zero);
   assertThat(result.getDBL(), is(0.0));
   assertEquals(result.getType(), "xs:double");
  
   }

}
