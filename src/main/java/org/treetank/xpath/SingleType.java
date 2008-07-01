
package org.treetank.xpath;

import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>SingleType</h1>
 * <p>
 * A single type defines a type the a single item can have. It consists of an
 * atomic type and a optional interrogation that, when present indicates that
 * the item can also be the empty sequence.
 * </p>
 * 
 * @author Tina Scherer
 */
public class SingleType {

  private Type mAtomicType;

  private final boolean mhasInterogation;

  /**
   * Constructor.
   * 
   * @param atomic
   *          string representation of the atomic value
   * @param intero
   *          true, if interrogation sign is present
   */
  public SingleType(final String atomic, final boolean intero) {

    // get atomic type
    mAtomicType = null; // TODO. = null is not good style
    for (Type type : Type.values()) {
      if (type.getStringRepr().equals(atomic)) {
        mAtomicType = type;
        break;
      }
    }

    if (mAtomicType == null) {
      throw new XPathError(ErrorType.XPST0051);
    }

    mhasInterogation = intero;
  }

  /**
   * Gets the atomic type.
   * 
   * @return atomic type.
   */
  public Type getAtomic() {

    return mAtomicType;
  }

  /**
   * Specifies, whether interrogation sign is present and therefore the empty
   * sequence is valid too.
   * 
   * @return true, if interrogation sign is present.
   */
  public boolean hasInterogation() {

    return mhasInterogation;
  }

}
