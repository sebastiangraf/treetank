package org.treetank.xpath.types;

import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;


/**
 * The Integer class provides all functions that are used for values of the
 * XML Schema type 'integer'.
 * 
 *  The class implemented as a singleton. 
 *  
 * @author Tina Scherer
 *
 */
public final class XSInteger {
  
  /** Single instance of the class Integer. */
  private static XSInteger instance;
  
  /**
   * Private Constructor. Can only be called by the class itself. 
   */
  private XSInteger() { }

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
   * @return the instance of the Integer class
   */
  public static XSInteger getInstance() {
    if (instance == null) {
      instance = new XSInteger();
    }
    return instance;
  }

// 

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryMinus(final AtomicValue a) {
    final int value = Integer.parseInt(TypedValue.parseString(a.getRawValue())) * -1;
    return new AtomicValue(value, Type.INTEGER);
  }

  

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericIntegerDivision(
      final AtomicValue a, final AtomicValue b) {
    try {
      final int value =  Integer.parseInt(TypedValue.parseString(a.getRawValue())) / Integer.parseInt(TypedValue.parseString(b.getRawValue()));
      return new AtomicValue(value, Type.INTEGER);
    } catch (ArithmeticException e) {
      throw new XPathError(ErrorType.FOAR0001);
    }
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue abs(final AtomicValue arg) {
    final int value = Math.abs(Integer.parseInt(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.INTEGER);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue ceiling(final AtomicValue arg) {
    //if it is already an integer, the result is the same value 
    return new AtomicValue(Integer.parseInt(TypedValue.parseString(arg.getRawValue())), Type.INTEGER);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue floor(final AtomicValue arg) {
  //if it is already an integer, the result is the same value 
    return new AtomicValue(Integer.parseInt(TypedValue.parseString(arg.getRawValue())), Type.INTEGER);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue round(final AtomicValue arg) {
  //if it is already an integer, the result is the same value 
    return new AtomicValue(Integer.parseInt(TypedValue.parseString(arg.getRawValue())), Type.INTEGER);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue roundHalfToEven(final AtomicValue arg) {
  //if it is already an integer, the result is the same value 
    return new AtomicValue(Integer.parseInt(TypedValue.parseString(arg.getRawValue())), Type.INTEGER);
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

   

}
