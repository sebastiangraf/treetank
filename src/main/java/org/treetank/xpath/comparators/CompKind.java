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
 * $Id: $
 */

package org.treetank.xpath.comparators;

import org.treetank.xpath.XPathConstants;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>CompKind</h1>
 * <p>
 * Enumeration for all comparison kinds.
 * </p>
 */
public enum CompKind implements XPathConstants {

  /** comparison type 'equal'. */
  EQ {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

     switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) == Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) == Double.parseDouble(operand2));

        case INTEGER:
//          return (Integer.getInteger(operand1) == Integer.getInteger(operand2));
         return ((int) Double.parseDouble(operand1) == (int) Double.parseDouble(operand2));

        case BOOLEAN:
          return (Boolean.parseBoolean(operand1) == Boolean.parseBoolean(operand2));

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) == 0;

        case DATE:
        case DATE_TIME:
        case TIME:
        case DURATION:
        case HEX_BINARY:
        case BASE_64_BINARY:
        case QNAME:
        case NOTATION:
        case G_DAY:
        case G_MONTH_DAY:
        case G_MONTH:
        case G_YEAR:
        case G_YEAR_MONTH:
          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }
    }

  },

  /** comparison type 'not equal'. */
  NE {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

       switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) != Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) != Double.parseDouble(operand2));

        case INTEGER:
          return ((int) Double.parseDouble(operand1) != (int) Double.parseDouble(operand2));

        case BOOLEAN:
          return (Boolean.parseBoolean(operand1) != Boolean.parseBoolean(operand2));

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) != 0;

        case DATE:
        case DATE_TIME:
        case TIME:
        case DURATION:
        case HEX_BINARY:
        case BASE_64_BINARY:
        case QNAME:
        case NOTATION:
        case G_DAY:
        case G_MONTH_DAY:
        case G_MONTH:
        case G_YEAR:
        case G_YEAR_MONTH:
          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }
    }

  },

  /** comparison type 'less than'. */
  LT {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) < Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) < Double.parseDouble(operand2));

        case INTEGER:
          return ((int) Double.parseDouble(operand1) < (int) Double.parseDouble(operand2));

        case BOOLEAN:
          // true, if operand1 == false and operand2 == true
          return (!Boolean.parseBoolean(operand1) && Boolean.parseBoolean(operand2));

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) < 0;

        case DATE:
        case DATE_TIME:
        case TIME:
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:
        
          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }

    }

  },

  /** comparison type 'less or equal than'. */
  LE {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) <= Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) <= Double.parseDouble(operand2));

        case INTEGER:
          return ((int) Double.parseDouble(operand1) <= (int) Double.parseDouble(operand2));

        case BOOLEAN:
          return !Boolean.parseBoolean(operand1) || Boolean.parseBoolean(operand2);

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) < 1;

        case DATE:
        case DATE_TIME:
        case TIME:
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:

          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }

    }

  },

  /** comparison type 'greater than'. */
  GT {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) > Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) > Double.parseDouble(operand2));

        case INTEGER:
          return ((int) Double.parseDouble(operand1) > (int) Double.parseDouble(operand2));

        case BOOLEAN:
          // true, if operand1 == true and operand2 == false
          return (Boolean.parseBoolean(operand1) && !Boolean.parseBoolean(operand2));

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) > 0;

        case DATE:
        case DATE_TIME:
        case TIME:
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:

          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }
    }
  },

  /** value comparison type 'greater or equal than'. */
  GE {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      switch (type) {
        case FLOAT:
          return (Float.parseFloat(operand1) >= Float.parseFloat(operand2));

        case DECIMAL:
        case DOUBLE:
          return (Double.parseDouble(operand1) >= Double.parseDouble(operand2));

        case INTEGER:
          return ((int) Double.parseDouble(operand1) >= (int) Double.parseDouble(operand2));

        case BOOLEAN:
          return (Boolean.parseBoolean(operand1) || !Boolean.parseBoolean(operand2));

        case STRING:
        case ANY_URI:
          return operand1.compareTo(operand2) > -1;

        case DATE:
        case DATE_TIME:
        case TIME:
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:

          throw new IllegalStateException("Not implemented for this type yet");
        default:
          throw new XPathError(ErrorType.XPTY0004);
      }

    }
  },

  /** node comparison type. */
  FO {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      throw new IllegalStateException(
          "Evaluation of node comparisons not possible");
    }

  },
  /** node comparison type . */
  PRE {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {

      throw new IllegalStateException(
          "Evaluation of node comparisons not possible");
    }

  },
  /** node comparison type . */
  IS {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compare(final String operand1,
        final String operand2, final Type type) {
      
      return ((int) Double.parseDouble(operand1) == (int) Double.parseDouble(operand2));
    }

  };

  /**
   * Compares the two input values.
   * 
   * @param operand1
   *          string value of first comparison operand
   * @param operand2
   *          string value of second comparison operand
   * @param type
   *          comparison type
   * @return result of the boolean comparison
   */
  public abstract boolean compare(final String operand1,
      final String operand2, final Type type);

}
