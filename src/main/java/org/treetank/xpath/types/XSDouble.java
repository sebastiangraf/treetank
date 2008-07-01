
package org.treetank.xpath.types;

import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * The Double class provides all functions that are used for values of the XML
 * Schema type 'double'. The class implemented as a singleton.
 * 
 * @author Tina Scherer
 */
public final class XSDouble {

  /** Single instance of the class Double. */
  private static XSDouble instance;

  /**
   * Private Constructor. Can only be called by the class itself.
   */
  private XSDouble() {

  }

  /**
   * Returns the only instance of this class. Returns reference the ONLY
   * instance, if already instantiated, otherwise first instantiates class and
   * returns the reference. Note: This is not threadsafe! But because we don't
   * use threads here (at least at the moment), it is ok like that. In case
   * multithreading will be used for this, add e.g. DoubleLock. See a
   * description of the singleton pattern for more details.
   * 
   * @return the instance of the Double class.
   */
  public static XSDouble getInstance() {

    if (instance == null) {
      instance = new XSDouble();
    }
    return instance;
  }

  /**
   * {@inheritDoc}
   */

  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryMinus(final AtomicValue a) {

    final double value = Double.parseDouble(TypedValue.parseString(a.getRawValue())) * (-1d);
    return new AtomicValue(value, Type.DOUBLE);
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
    return new AtomicValue(value, Type.DOUBLE);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue ceiling(final AtomicValue arg) {

    final double value = Math.ceil(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DOUBLE);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue floor(final AtomicValue arg) {

    final double value = Math.floor(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DOUBLE);
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
    
    return new AtomicValue(value, Type.DOUBLE);
  }

  /**
   * {@inheritDoc}
   */
  public AtomicValue roundHalfToEven(final AtomicValue arg) {

    // TODO
    final double value = Math.round(Double.parseDouble(TypedValue.parseString(arg.getRawValue())));
    return new AtomicValue(value, Type.DOUBLE);
  }

  
  /**
   * {@inheritDoc}
   */
  public AtomicValue numericUnaryPlus(final AtomicValue arg) {

    return arg;
  }

  


}
