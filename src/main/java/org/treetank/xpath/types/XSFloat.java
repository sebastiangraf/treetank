package org.treetank.xpath.types;

import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * The Float class provides all functions that are used for values of the
 * XML Schema type 'float'.
 * 
 *  The class implemented as a singleton. 
 *  
 * @author Tina Scherer
 *
 */
public final class XSFloat {
  
 /** Single instance of the class Float. */ 
 private static XSFloat instance;
  
 /**
  * Private Constructor. Can only be called by the class itself. 
  */
  private XSFloat() { }

  /**
   * Returns the only instance of this class. 
   * Returns reference the ONLY instance, if already instantiated, otherwise 
   * first instantiates class and returns the reference.
   * 
   * Note: This is not threadsafe! But because we don't use threads here 
   * (at least at the moment), it is ok like that.
   * In case multithreading will be used for this, add e.g. DoubleLock. See a
   * description of the singleton pattern for more details.
   * 
   * @return the instance of the Float class
   */
  public static XSFloat getInstance() {
    if (instance == null) {
      instance = new XSFloat();
    }
    return instance;
  }


  /**
   * {@inheritDoc}
   */
  public AtomicValue multiply(final AtomicValue a, final AtomicValue b) {
    final float value = Float.parseFloat(TypedValue.parseString(a.getRawValue())) * Float.parseFloat(TypedValue.parseString(b.getRawValue()));
    return new AtomicValue(value, Type.FLOAT);
  }


  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryMinus(final AtomicValue a) {
    final float value = Float.parseFloat(TypedValue.parseString(a.getRawValue())) * (-1f);
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue equals(final AtomicValue a, final AtomicValue b) {
    return new AtomicValue(Float.parseFloat(TypedValue.parseString(a.getRawValue())) == Float.parseFloat(TypedValue.parseString(b.getRawValue()))); 
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue greaterThan(final AtomicValue a, final AtomicValue b) {
    return new AtomicValue(Float.parseFloat(TypedValue.parseString(a.getRawValue())) > Float.parseFloat(TypedValue.parseString(b.getRawValue())));
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue lessThan(final AtomicValue a, final AtomicValue b) {
    return new AtomicValue(Float.parseFloat(TypedValue.parseString(a.getRawValue())) < Float.parseFloat(TypedValue.parseString(b.getRawValue())));
  }


  /**
   * {@inheritDoc}
   */
  public AtomicValue numericIntegerDivision(
      final AtomicValue a, final AtomicValue b) {
    
    final float aFLT = Float.parseFloat(TypedValue.parseString(a.getRawValue()));
    final float bFLT = Float.parseFloat(TypedValue.parseString(b.getRawValue()));

    if (Float.isNaN(bFLT) || Float.isNaN(aFLT)
        || aFLT == Float.POSITIVE_INFINITY 
        || aFLT == Float.NEGATIVE_INFINITY) {
      throw new XPathError(ErrorType.FOAR0002);
    }

    if (bFLT == 0.0f || bFLT == (-0.0f)) {
      throw new XPathError(ErrorType.FOAR0001);
    }

    final int value = (int) (aFLT / bFLT);
    return new AtomicValue(value, Type.INTEGER);

  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue abs(final AtomicValue arg) {
    final float value = Math.abs(Float.parseFloat(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue ceiling(final AtomicValue arg) {
    final float value = (float) Math.ceil(Float.parseFloat(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue floor(final AtomicValue arg) {
    final float value = (float) Math.floor(Float.parseFloat(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue round(final AtomicValue arg) {
    
    final float value;
    final Float a = Float.parseFloat(TypedValue.parseString(arg.getRawValue()));
    
    //Java returns MAX_VALUE or MIN_VALUE for INF or -INF
    if(a.isInfinite()) {
      value = a;
    } else {
      value = Math.round(a);
    }
    
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue roundHalfToEven(final AtomicValue arg) {
    //TODO!
    final float value = Math.round(Float.parseFloat(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.FLOAT);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue roundHalfToEven(final AtomicValue arg, final int precision) {

    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryPlus(final AtomicValue arg) {

    return arg;
  }

  
  
  /**
   * {@inheritDoc}
   */
  public AtomicValue greaterEquals(final AtomicValue arg1, 
      final AtomicValue arg2) {

    final boolean value = Boolean.parseBoolean(TypedValue.parseString(greaterThan(arg1, arg2).getRawValue()))
       || Boolean.parseBoolean(TypedValue.parseString(equals(arg1, arg2).getRawValue()));
    return new AtomicValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue lessEquals(final AtomicValue arg1, 
      final AtomicValue arg2) {

    final boolean value = Boolean.parseBoolean(TypedValue.parseString(lessThan(arg1, arg2).getRawValue()))
       || Boolean.parseBoolean(TypedValue.parseString(equals(arg1, arg2).getRawValue()));
    return new AtomicValue(value);
  }
}
