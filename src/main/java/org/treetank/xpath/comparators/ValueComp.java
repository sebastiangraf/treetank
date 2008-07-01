
package org.treetank.xpath.comparators;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>ValueComp</h1>
 * <p>
 * Value comparisons are used for comparing single values.
 * </p>
 * @author Tina Scherer
 *
 */
public class ValueComp extends AbstractComparator {
  
  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param operand1
   *          First value of the comparison
   * @param operand2
   *          Second value of the comparison
   * @param comp
   *          comparison kind         
   */
  public ValueComp(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2, final CompKind comp) {

    super(rtx, operand1, operand2, comp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean compare(final AtomicValue[] operand1,
      final AtomicValue[] operand2) {
    final Type type = getType(operand1[0].getTypeKey(), operand2[0].getTypeKey());
    final String op1 = TypedValue.parseString(operand1[0].getRawValue());
    final String op2 = TypedValue.parseString(operand2[0].getRawValue());

    return getCompKind().compare(op1, op2, type);
  }

  /**
   * {@inheritDoc}
   */
  protected AtomicValue[] atomize(final IAxis operand) {

    final IReadTransaction trx = getTransaction();

    int type = trx.getTypeKey();

    // (3.) if type is untypedAtomic, cast to string
    if (type == trx.keyForName("xs:unytpedAtomic")) {
      type = trx.keyForName("xs:string");
    }

    final AtomicValue atomized = new AtomicValue(operand.getTransaction()
        .getRawValue(), type);
    AtomicValue[] op = { atomized };

    // (4.) the operands must be singletons in case of a value comparison
    if (operand.hasNext()) {
      throw new XPathError(ErrorType.XPTY0004);
    } else {
      return op;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Type getType(final int key1, final int key2) {

    Type type1 = Type.getType(key1).getPrimitiveBaseType();
    Type type2 = Type.getType(key2).getPrimitiveBaseType();
    return Type.getLeastCommonType(type1, type2);

  }

}
