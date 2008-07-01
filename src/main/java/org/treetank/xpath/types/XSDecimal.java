
package org.treetank.xpath.types;

import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * The Double class provides all functions that are used for values of the XML
 * Schema type 'decimal'. The class implemented as a singleton.
 * 
 * @author Tina Scherer
 */
public final class XSDecimal {

  /** Single instance of the class decimal. */
  private static XSDecimal instance;

  /**
   * Private Constructor. Can only be called by the class itself.
   */
  private XSDecimal() {

  }

  /**
   * Returns the only instance of this class. Returns reference the ONLY
   * instance, if already instantiated, otherwise first instantiates class and
   * returns the reference. Note: This is not threadsafe! But because we don't
   * use threads here (at least at the moment), it is ok like that. In case
   * multithreading will be used for this, add e.g. DoubleLock. See a
   * description of the singleton pattern for more details.
   * 
   * @return the instance of the decimal class.
   */
  public static XSDecimal getInstance() {

    if (instance == null) {
      instance = new XSDecimal();
    }
    return instance;
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue divide(final AtomicValue a, final AtomicValue b) {

    final double aD = Double.parseDouble(TypedValue.parseString(a.getRawValue()));
    final double value;
    
    if (aD == 0.0 || aD == -0.0) {
      value = Double.NaN;
    } else {
      value = aD / Double.parseDouble(TypedValue.parseString(b.getRawValue()));
    }
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryMinus(final AtomicValue a) {

    final double value = Double.parseDouble(TypedValue.parseString(a.getRawValue())) * (-1d);
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue equals(final AtomicValue a, final AtomicValue b) {

    return new AtomicValue(Double.parseDouble(TypedValue.parseString(a.getRawValue())) == Double.parseDouble(TypedValue.parseString(b.getRawValue())));
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue greaterThan(final AtomicValue a, final AtomicValue b) {
    double aValue = Double.parseDouble(TypedValue.parseString(a.getRawValue()));
    double bValue = Double.parseDouble(TypedValue.parseString(b.getRawValue())); 
    boolean value = aValue > bValue;
    return new AtomicValue(value);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue lessThan(final AtomicValue a, final AtomicValue b) {

    return new AtomicValue(Double.parseDouble(TypedValue.parseString(a.getRawValue())) < Double.parseDouble(TypedValue.parseString(b.getRawValue())));
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericIntegerDivision(final AtomicValue a,
      final AtomicValue b) {

    final double aDBL = Double.parseDouble(TypedValue.parseString(a.getRawValue()));
    final double bDBL = Double.parseDouble(TypedValue.parseString(b.getRawValue()));

    if (Double.isNaN(bDBL) || Double.isNaN(aDBL)
        || aDBL == Double.POSITIVE_INFINITY 
        || aDBL == Double.NEGATIVE_INFINITY) {
      throw new XPathError(ErrorType.FOAR0002);
    }

    if (bDBL == 0.0d || bDBL == (-0.0d)) {
      throw new XPathError(ErrorType.FOAR0001);
    }

    final int value = (int) (aDBL / bDBL);
    return new AtomicValue(value, Type.INTEGER);

  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue abs(final AtomicValue arg) {

    final double value = Math.abs(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue ceiling(final AtomicValue arg) {

    final double value = Math.ceil(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue floor(final AtomicValue arg) {

    final double value = Math.floor(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue round(final AtomicValue arg) {
    
    final double value;
    final Double a = Double.parseDouble(TypedValue.parseString(arg.getRawValue()));
    
    //Java returns MAX_VALUE or MIN_VALUE for INF or -INF
    if(a.isInfinite()) {
      value = a;
    } else {
      value = Math.round(a);
    }
    
    return new AtomicValue(value, Type.DECIMAL);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue roundHalfToEven(final AtomicValue arg) {

    // TODO
    final double value = Math.round(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DECIMAL);
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
