/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xpath.comparators;

import java.util.ArrayList;
import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.Function;
import org.treetank.xpath.types.Type;

/**
 * <h1>GeneralComp</h1>
 * <p>
 * General comparisons are existentially quantified comparisons that may be
 * applied to operand sequences of any length.
 * </p>
 */
public class GeneralComp extends AbstractComparator {

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
  public GeneralComp(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2, final CompKind comp) {

    super(rtx, operand1, operand2, comp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean compare(final AtomicValue[] operand1,
      final AtomicValue[] operand2) {

    assert operand1.length >= 1 && operand2.length >= 1;

    for (AtomicValue op1 : operand1) {
      for (AtomicValue op2 : operand2) {
        String value1 = TypedValue.parseString(op1.getRawValue());
        String value2 = TypedValue.parseString(op2.getRawValue());
        if (getCompKind().compare(value1, value2,
            getType(op1.getTypeKey(), op2.getTypeKey()))) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  protected AtomicValue[] atomize(final IAxis operand) {

    final IReadTransaction rtx = getTransaction();
    final List<AtomicValue> op = new ArrayList<AtomicValue>();
    AtomicValue atomized;
    // cast to double, if compatible with XPath 1.0 and <, >, >=, <=
    final boolean convert = !(!XPATH_10_COMP || getCompKind() == CompKind.EQ || getCompKind() == CompKind.EQ);

    do {
      if (convert) { // cast to double
        Function.fnnumber(rtx);
      }
      atomized = new AtomicValue(rtx.getRawValue(), rtx.getTypeKey());
      op.add(atomized);
    } while (operand.hasNext());

    return op.toArray(new AtomicValue[op.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Type getType(final int key1, final int key2) {

    final Type type1 = Type.getType(key1).getPrimitiveBaseType();
    final Type type2 = Type.getType(key2).getPrimitiveBaseType();

    if (XPATH_10_COMP) {
      if (type1.isNumericType() || type2.isNumericType()) {
        return Type.DOUBLE;
      }

      if (type1 == Type.STRING || type2 == Type.STRING
          || (type1 == Type.UNTYPED_ATOMIC && type2 == Type.UNTYPED_ATOMIC)) {
        return Type.STRING;
      }

      if (type1 == Type.UNTYPED_ATOMIC || type2 == Type.UNTYPED_ATOMIC) {
        return Type.UNTYPED_ATOMIC;

      }

    }

    if (!XPATH_10_COMP) {
      if (type1 == Type.UNTYPED_ATOMIC) {
        switch (type2) {
          case UNTYPED_ATOMIC:
          case STRING:
            return Type.STRING;
          case INTEGER:
          case DECIMAL:
          case FLOAT:
          case DOUBLE:
            return Type.DOUBLE;

          default:
            return type2;
        }
      }

      if (type2 == Type.UNTYPED_ATOMIC) {
        switch (type1) {
          case UNTYPED_ATOMIC:
          case STRING:
            return Type.STRING;
          case INTEGER:
          case DECIMAL:
          case FLOAT:
          case DOUBLE:
            return Type.DOUBLE;

          default:
            return type1;
        }
      }

    }

    return Type.getLeastCommonType(type1, type2);

  }

//  protected void hook(final AtomicValue[] operand1, final AtomicValue[] operand2) {
//
//    if (operand1.length == 1
//        && operand1[0].getTypeKey() == getTransaction()
//            .keyForName("xs:boolean")) {
//      operand2 = new AtomicValue[1];
//      getOperand2().reset(startKey);
//      operand2[0] = new AtomicValue(Function.ebv(getOperand1()));
//    } else {
//      if (operand2.length == 1
//          && operand2[0].getTypeKey() == getTransaction().keyForName(
//              "xs:boolean")) {
//        operand1 = new AtomicValue[1];
//        getOperand1().reset(startKey);
//        operand1[0] = new AtomicValue(Function.ebv(getOperand2()));
//      }
//    }
//  }

}
