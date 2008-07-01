
package org.treetank.xpath.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;

public class XSIntegerTest {

  private AtomicValue a, b, c, e, f, zero, n_zero, result;

  private XSInteger dInstance;

  private boolean boolResult;

  @Before
  public void setUp() {

    dInstance = XSInteger.getInstance();

    a = new AtomicValue(1, Type.INTEGER);
    b = new AtomicValue(2, Type.INTEGER);
    c = new AtomicValue(-3, Type.INTEGER);
    e = new AtomicValue(10, Type.INTEGER);
    f = new AtomicValue(5, Type.INTEGER);

    zero = new AtomicValue(+0, Type.INTEGER);
    n_zero = new AtomicValue(-0, Type.INTEGER);

  }

//  @Test
//  public final void testNumericAdd() {
//
//    result = dInstance.add(a, b);
//    assertEquals(result.getInt(), 1 + 2);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.add(a, c);
//    assertEquals(result.getInt(), -3 + 1);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.add(c, f);
//    assertEquals(result.getInt(), -3 + 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.add(e, f);
//    assertEquals(result.getInt(), 10 + 5);
//    assertEquals(result.getType(), "xs:integer");
//
//  }
//
//  @Test
//  public final void testNumericMod() {
//
//    result = dInstance.mod(a, b);
//    assertEquals(result.getInt(), 1 % 2);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.mod(a, c);
//    assertEquals(result.getInt(), 1 % (-3));
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.mod(c, f);
//    assertEquals(result.getInt(), -3 % 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.mod(e, f);
//    assertEquals(result.getInt(), 10 % 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.mod(zero, a);
//    assertEquals(result.getInt(), 0);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.mod(n_zero, c);
//    assertEquals(result.getInt(), -0);
//    assertEquals(result.getType(), "xs:integer");
//  }
//
//  @Test
//  public final void testNumericMultiply() {
//
//    result = dInstance.multiply(a, b);
//    assertEquals(result.getInt(), 1 * 2);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.multiply(a, c);
//    assertEquals(result.getInt(), 1 * (-3));
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.multiply(c, f);
//    assertEquals(result.getInt(), -3 * 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.multiply(e, f);
//    assertEquals(result.getInt(), 10 * 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.multiply(zero, a);
//    assertEquals(result.getInt(), 0);
//    assertEquals(result.getType(), "xs:integer");
//
//  }
//
//  @Test
//  public final void testNumericDivision() {
//
//    result = dInstance.divide(a, b);
//    assertEquals(result.getInt(), 1 / 2);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.divide(a, c);
//    assertEquals(result.getInt(), 1 / (-3));
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.divide(c, f);
//    assertEquals(result.getInt(), -3 / 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.divide(e, f);
//    assertEquals(result.getInt(), 10 / 5);
//    assertEquals(result.getType(), "xs:integer");
//    
//    
//    try {
//      result = dInstance.divide(a, zero);
//      fail("Expected an XPathError-Exception.");
//    } catch (XPathError e) {
//      assertEquals(e.getMessage(), "err:FOAR0001: Division by zero.");
//    }
//    
//    try {
//      result = dInstance.divide(a, n_zero);
//      fail("Expected an XPathError-Exception.");
//    } catch (XPathError e) {
//      assertEquals(e.getMessage(), "err:FOAR0001: Division by zero.");
//    }
//
//
//  }
//
//  @Test
//  public final void testNumericSubstract() {
//
//    result = dInstance.substract(a, b);
//    assertEquals(result.getInt(), 1 - 2);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.substract(a, c);
//    assertEquals(result.getInt(), 1 - (-3));
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.substract(c, f);
//    assertEquals(result.getInt(), -3 - 5);
//    assertEquals(result.getType(), "xs:integer");
//
//    result = dInstance.substract(e, f);
//    assertEquals(result.getInt(), 10 - 5);
//    assertEquals(result.getType(), "xs:integer");
//  }

  @Test
  public final void testNumericUnaryMinus() {

    result = dInstance.numericUnaryMinus(a);
    assertEquals(result.getInt(), -1);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryMinus(c);
    assertEquals(result.getInt(), 3);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryMinus(f);
    assertEquals(result.getInt(), -5);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryMinus(e);
    assertEquals(result.getInt(), -10);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryMinus(n_zero);
    assertEquals(result.getInt(), 0);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryMinus(zero);
    assertEquals(result.getInt(), +0);
    assertEquals(result.getType(), "xs:integer");

  }

  
//  @Test
//  public final void testNumericEqual() {
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.equals(a, b).getRawValue()));
//    assertEquals(boolResult, false);
//
//   boolResult = Boolean.parseBoolean(TypedValue.parseString(
//       dInstance.equals(a, c).getRawValue()));
//    assertEquals(boolResult, false);
//
//   boolResult = Boolean.parseBoolean(TypedValue.parseString(
//       dInstance.equals(c, f).getRawValue()));
//    assertEquals(boolResult, false);
//
//   boolResult = Boolean.parseBoolean(TypedValue.parseString(
//       dInstance.equals(e, f).getRawValue()));
//    assertEquals(boolResult, false);
//
//   boolResult = Boolean.parseBoolean(TypedValue.parseString(
//       dInstance.equals(zero, zero).getRawValue()));
//    assertEquals(boolResult, true);
//
//   boolResult = Boolean.parseBoolean(TypedValue.parseString(
//       dInstance.equals(zero, n_zero).getRawValue()));
//    assertEquals(boolResult, true);
//
//  }
//
//  @Test
//  public final void testNumericGreaterThan() {
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(a, b).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(a, c).getRawValue()));
//    assertEquals(boolResult, true);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(c, f).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(e, f).getRawValue()));
//    assertEquals(boolResult, true);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(zero, zero).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.greaterThan(n_zero, zero).getRawValue()));
//    assertEquals(boolResult, false);
//
//  }
//
//  @Test
//  public final void testNumericLessThan() {
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(a, b).getRawValue()));
//    assertEquals(boolResult, true);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(a, c).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(c, f).getRawValue()));
//    assertEquals(boolResult, true);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(e, f).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(zero, zero).getRawValue()));
//    assertEquals(boolResult, false);
//
//    boolResult = Boolean.parseBoolean(TypedValue.parseString(
//        dInstance.lessThan(n_zero, zero).getRawValue()));
//    assertEquals(boolResult, false);
//
//  }

  @Test
  public final void testNumericIntegerDivision() {

    result = dInstance.numericIntegerDivision(a, b);
    assertEquals(result.getInt(), (int) (1 / 2));
    assertEquals(result.getInt(), 0);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(a, c);
    assertEquals(result.getInt(), (int) (1 / (-3)));
    assertEquals(result.getInt(), 0);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(c, f);
    assertEquals(result.getInt(), (int) (-3 / 5));
    assertEquals(result.getInt(), 0);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericIntegerDivision(e, f);
    assertEquals(result.getInt(), 10 / 5);
    assertEquals(result.getInt(), 2);
    assertEquals(result.getType(), "xs:integer");

    try {
      result = dInstance.numericIntegerDivision(a, zero);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertEquals(e.getMessage(), "err:FOAR0001: Division by zero.");
    }

    try {
      result = dInstance.numericIntegerDivision(c, n_zero);
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertEquals(e.getMessage(), "err:FOAR0001: Division by zero.");
    }

  }

  
   @Test
   public final void testAbs() {
     result = dInstance.abs(a);
     assertEquals(result.getInt(), 1);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.abs(c);
     assertEquals(result.getInt(), 3);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.abs(f);
     assertEquals(result.getInt(), 5);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.abs(e);
     assertEquals(result.getInt(), 10);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.abs(n_zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.abs(zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");
   }
  
   @Test
   public final void testCeiling() {
     result = dInstance.ceiling(a);
     assertEquals(result.getInt(), 1);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.ceiling(c);
     assertEquals(result.getInt(), -3);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.ceiling(f);
     assertEquals(result.getInt(), 5);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.ceiling(e);
     assertEquals(result.getInt(), 10);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.ceiling(n_zero);
     assertEquals(result.getInt(), -0);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.ceiling(zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");
   }
  
   @Test
   public final void testFloor() {
     result = dInstance.floor(a);
     assertEquals(result.getInt(), 1);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.floor(c);
     assertEquals(result.getInt(), -3);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.floor(f);
     assertEquals(result.getInt(), 5);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.floor(e);
     assertEquals(result.getInt(), 10);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.floor(n_zero);
     assertEquals(result.getInt(), -0);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.floor(zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");
   }
  
   @Test
   public final void testRound() {
     result = dInstance.round(a);
     assertEquals(result.getInt(), 1);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.round(c);
     assertEquals(result.getInt(), -3);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.round(f);
     assertEquals(result.getInt(), 5);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.round(e);
     assertEquals(result.getInt(), 10);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.round(n_zero);
     assertEquals(result.getInt(), -0);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.round(zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");
   }
  
   @Test
   public final void testRoundHalfToEven() {
     result = dInstance.roundHalfToEven(a);
     assertEquals(result.getInt(), 1);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.roundHalfToEven(c);
     assertEquals(result.getInt(), -3);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.roundHalfToEven(f);
     assertEquals(result.getInt(), 5);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.roundHalfToEven(e);
     assertEquals(result.getInt(), 10);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.roundHalfToEven(n_zero);
     assertEquals(result.getInt(), -0);
     assertEquals(result.getType(), "xs:integer");

     result = dInstance.roundHalfToEven(zero);
     assertEquals(result.getInt(), 0);
     assertEquals(result.getType(), "xs:integer");
   }
  
//   @Test
//   public final void testRoundHalfToEvenAtomicValueInt() {
//  
//   fail("Not yet implemented");  TODO
//   }
//  
  @Test
  public final void testNumericUnaryPlus() {

    result = dInstance.numericUnaryPlus(a);
    assertEquals(result.getInt(), 1);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryPlus(c);
    assertEquals(result.getInt(), -3);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryPlus(f);
    assertEquals(result.getInt(), 5);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryPlus(e);
    assertEquals(result.getInt(), 10);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryPlus(n_zero);
    //assertEquals(result.getInt(), -0);
    assertEquals(result.getType(), "xs:integer");

    result = dInstance.numericUnaryPlus(zero);
    assertEquals(result.getInt(), 0);
    assertEquals(result.getType(), "xs:integer");

  }

}
