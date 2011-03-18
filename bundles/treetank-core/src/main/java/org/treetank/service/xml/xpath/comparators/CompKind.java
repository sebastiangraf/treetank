/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.service.xml.xpath.comparators;

import java.util.HashMap;
import java.util.Map;

import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.EXPathError;
import org.treetank.service.xml.xpath.types.Type;

/**
 * <h1>CompKind</h1>
 * <p>
 * Enumeration for all comparison kinds.
 * </p>
 */
public enum CompKind {

    /** comparison type 'equal'. */
    EQ("eq", "=") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) == Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) == Double.parseDouble(mOperand2));

            case INTEGER:
                // return (Integer.getInteger(operand1) ==
                // Integer.getInteger(operand2));
                return ((int)Double.parseDouble(mOperand1) == (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                return (Boolean.parseBoolean(mOperand1) == Boolean.parseBoolean(mOperand2));

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) == 0;

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
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }
        }

    },

    /** comparison type 'not equal'. */
    NE("ne", "!=") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) != Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) != Double.parseDouble(mOperand2));

            case INTEGER:
                return ((int)Double.parseDouble(mOperand1) != (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                return (Boolean.parseBoolean(mOperand1) != Boolean.parseBoolean(mOperand2));

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) != 0;

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
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }
        }

    },

    /** comparison type 'less than'. */
    LT("lt", "<") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) < Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) < Double.parseDouble(mOperand2));

            case INTEGER:
                return ((int)Double.parseDouble(mOperand1) < (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                // true, if operand1 == false and operand2 == true
                return (!Boolean.parseBoolean(mOperand1) && Boolean.parseBoolean(mOperand2));

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) < 0;

            case DATE:
            case DATE_TIME:
            case TIME:
            case YEAR_MONTH_DURATION:
            case DAY_TIME_DURATION:

                throw new IllegalStateException("Not implemented for this type yet");
            default:
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }

        }

    },

    /** comparison type 'less or equal than'. */
    LE("le", "<=") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) <= Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) <= Double.parseDouble(mOperand2));

            case INTEGER:
                return ((int)Double.parseDouble(mOperand1) <= (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                return !Boolean.parseBoolean(mOperand1) || Boolean.parseBoolean(mOperand2);

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) < 1;

            case DATE:
            case DATE_TIME:
            case TIME:
            case YEAR_MONTH_DURATION:
            case DAY_TIME_DURATION:

                throw new IllegalStateException("Not implemented for this type yet");
            default:
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }

        }

    },

    /** comparison type 'greater than'. */
    GT("gt", ">") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) > Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) > Double.parseDouble(mOperand2));

            case INTEGER:
                return ((int)Double.parseDouble(mOperand1) > (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                // true, if operand1 == true and operand2 == false
                return (Boolean.parseBoolean(mOperand1) && !Boolean.parseBoolean(mOperand2));

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) > 0;

            case DATE:
            case DATE_TIME:
            case TIME:
            case YEAR_MONTH_DURATION:
            case DAY_TIME_DURATION:

                throw new IllegalStateException("Not implemented for this type yet");
            default:
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }
        }
    },

    /** value comparison type 'greater or equal than'. */
    GE("ge", ">=") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            switch (mType) {
            case FLOAT:
                return (Float.parseFloat(mOperand1) >= Float.parseFloat(mOperand2));

            case DECIMAL:
            case DOUBLE:
                return (Double.parseDouble(mOperand1) >= Double.parseDouble(mOperand2));

            case INTEGER:
                return ((int)Double.parseDouble(mOperand1) >= (int)Double.parseDouble(mOperand2));

            case BOOLEAN:
                return (Boolean.parseBoolean(mOperand1) || !Boolean.parseBoolean(mOperand2));

            case STRING:
            case ANY_URI:
                return mOperand1.compareTo(mOperand2) > -1;

            case DATE:
            case DATE_TIME:
            case TIME:
            case YEAR_MONTH_DURATION:
            case DAY_TIME_DURATION:

                throw new IllegalStateException("Not implemented for this type yet");
            default:
                throw EXPathError.XPTY0004.getEncapsulatedException();
            }

        }
    },

    /** node comparison type. */
    FO(">>") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            throw new IllegalStateException("Evaluation of node comparisons not possible");
        }

    },
    /** node comparison type . */
    PRE("<<") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            throw new IllegalStateException("Evaluation of node comparisons not possible");
        }

    },
    /** node comparison type . */
    IS("is") {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean compare(final String mOperand1, final String mOperand2, final Type mType)
            throws TTXPathException {

            return (int)Double.parseDouble(mOperand1) == (int)Double.parseDouble(mOperand2);
        }

    };

    /** String representation of comp. */
    private final String[] mCompAsString;

    /** Private mapping for easy retrieval of enums. */
    private static final Map<String, CompKind> STRINGTOENUM = new HashMap<String, CompKind>();

    static {
        for (final CompKind kind : values()) {
            for (final String compAsString : kind.mCompAsString) {
                STRINGTOENUM.put(compAsString, kind);
            }
        }
    }

    /**
     * Private Constructor.
     * 
     * @param paramCompAsString
     *            String to be set.
     */
    private CompKind(final String... paramCompAsString) {
        mCompAsString = paramCompAsString;
    }

    /**
     * Compares the two input values.
     * 
     * @param mOperand1
     *            string value of first comparison operand
     * @param mOperand2
     *            string value of second comparison operand
     * @param mType
     *            comparison type
     * @return result of the boolean comparison
     * @throws TTXPathException
     *             if anything weird happens while comparison.
     */
    public abstract boolean compare(final String mOperand1, final String mOperand2, final Type mType)
        throws TTXPathException;

    /**
     * Public method to easy retrieve the Function-Class for a name.
     * 
     * @param paramName
     *            the name of the function to be retrieved.
     * @return the Function
     */
    public static CompKind fromString(final String paramName) {
        return STRINGTOENUM.get(paramName);
    }

}
