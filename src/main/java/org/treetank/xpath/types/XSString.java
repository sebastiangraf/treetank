
package org.treetank.xpath.types;

import org.treetank.api.IAxis;

/**
 * The String class provides all functions that are used for values of the XML
 * Schema type 'xs:string'. The class implemented as a singleton.
 * 
 * @author Tina Scherer
 */
public final class XSString {

  /** Single instance of the class XSString. */
  private static XSString instance;

  /**
   * Private Constructor. Can only be called by the class itself.
   */
  private XSString() {

  }

  /**
   * Returns the only instance of this class. Returns reference the ONLY
   * instance, if already instantiated, otherwise first instantiates class and
   * returns the reference. Note: This is not thread safe! But because we don't
   * use threads here (at least at the moment), it is ok like that. In case
   * multithreading will be used for this, add e.g. DoubleLock. See a
   * description of the singleton pattern for more details.
   * 
   * @return the instance of the Float class
   */
  public static XSString getInstance() {

    if (instance == null) {
      instance = new XSString();
    }
    return instance;
  }

  /**
   * Creates a xs:string from a sequence of [The Unicode Standard] code points.
   * Returns the zero-length string if $arg is the empty sequence. If any of the
   * code points in $arg is not a legal XML character, an error is raised
   * [err:FOCH0001].
   * 
   * @param args
   *          sequence of unicode codepoints
   * @return string translation of the given codepoints
   */
  public String codepointsToString(final int... args) {

    final StringBuilder s = new StringBuilder();

    for (int i = 0; i < args.length; i++) {
      s.appendCodePoint(args[i]);
    }
    return s.toString();

  }

 

  
  /**
   * Returns -1, 0, or 1, depending on whether the value of the $operator1 is
   * respectively less than, equal to, or greater than the value of $operator2,
   * according to the rules of the collation that is used. If either argument is
   * the empty sequence, the result is the empty sequence
   * 
   * @param a
   *          the first operator
   * @param b
   *          the second operator
   * @param collation
   *          the rules for the comparison
   * @return result of the comparison of a and b
   */
  public int compare(final IAxis a, final IAxis b, final String collation) {

    final String s1 = a.getTransaction().getValue();

    final String s2 = b.getTransaction().getValue();
    s1.compareTo(s2);
    throw new IllegalStateException("not implemented yet.");

  }

  /**
   * Returns true or false depending on whether the value of $operator1 is equal
   * to the value of $operator2, according to the Unicode code point collation
   * (http://www.w3.org/2005/xpath-functions/collation/codepoint). If either
   * argument is the empty sequence, the result is the empty sequence Note: This
   * function allows xs:anyURI values to be compared without having to specify
   * the Unicode code point collation.
   * 
   * @param a
   *          the first operator
   * @param b
   *          the second operator
   * @return true if the value of the both operators is the same, false
   *         otherwise
   */
  public boolean codepointEqual(final IAxis a, final IAxis b) {

    final String s1 = a.getTransaction().getValue();
    final String s2 = b.getTransaction().getValue();

    throw new IllegalStateException("not implemented yet.");
  }

  /**
   * Accepts two or more xs:anyAtomicType arguments and casts them to xs:string.
   * Returns the xs:string that is the concatenation of the values of its
   * arguments after conversion. If any of the arguments is the empty sequence,
   * the argument is treated as the zero-length string.
   * 
   * @param args
   *          strings to concatenate
   * @return concatenated string
   */
  public String concat(final IAxis... args) {

    final StringBuilder concat = new StringBuilder();
    for (IAxis s : args) {
      concat.append(s.getTransaction().getValue());
    }
    return concat.toString();
  }

  /**
   * Returns a xs:string created by concatenating the members of the $arg1
   * sequence using $arg2 as a separator. If the value of $arg2 is the
   * zero-length string, then the members of $arg1 are concatenated without a
   * separator. If the value of $arg1 is the empty sequence, the zero-length
   * string is returned.
   * 
   * @param seperator
   *          string that should seperate the parts
   * @param args
   *          strings to concatenate
   * @return concatenated string
   */
  public String stringJoin(final String seperator, final IAxis... args) {

    final StringBuilder concat = new StringBuilder();
    for (IAxis s : args) {
      concat.append(s.getTransaction().getValue());
      concat.append(seperator);
    }
    return concat.toString();
  }

  /**
   * Returns the portion of the value of $sourceString beginning at the position
   * indicated by the value of $startingLoc. The characters returned do not
   * extend beyond $sourceString. If $startingLoc is zero or negative, only
   * those characters in positions greater than zero are returned. The two
   * argument version of the function assumes that $length is infinite and
   * returns the characters in $sourceString whose position $p obeys:
   * fn:round($startingLoc) <= $p < fn:round(INF) In the above computations, the
   * rules for op:numeric-less-than() and op:numeric-greater-than() apply. If
   * the value of $sourceString is the empty sequence, the zero-length string is
   * returned. Note: The first character of a string is located at position 1,
   * not position 0.
   * 
   * @param sourceString
   *          the source string to get the substring from
   * @param startingLoc
   *          the position, where the substring should begin
   * @return a substring of the source string beginning at the startingLoc
   */
  public String substring(final IAxis sourceString, final double startingLoc) {

    final int start = (int) Math.round(startingLoc);
    final int begin = start > 0 ? start - 1 : 0;
    return sourceString.getTransaction().getValue().substring(begin);
  }

  /**
   * Returns the portion of the value of $sourceString beginning at the position
   * indicated by the value of $startingLoc and continuing for the number of
   * characters indicated by the value of $length. The characters returned do
   * not extend beyond $sourceString. If $startingLoc is zero or negative, only
   * those characters in positions greater than zero are returned. The two
   * argument version of the function assumes that $length is infinite and
   * returns the characters in $sourceString whose position $p obeys:
   * fn:round($startingLoc) <= $p < fn:round(INF) In the above computations, the
   * rules for op:numeric-less-than() and op:numeric-greater-than() apply. If
   * the value of $sourceString is the empty sequence, the zero-length string is
   * returned. Note: The first character of a string is located at position 1,
   * not position 0.
   * 
   * @param sourceString
   *          the source string to get the substring from
   * @param startingLoc
   *          the position, where the substring should begin
   * @param length
   *          the length of the substring starting from startingLoc.
   * @return a substring of the source string beginning at the startingLoc
   */
  public String substring(final IAxis sourceString, final double startingLoc,
      final double length) {

    final int start = (int) Math.round(startingLoc);
    final int begin = start > 0 ? start - 1 : 0;
    final int end = start + (int) Math.round(length);
    return sourceString.getTransaction().getValue().substring(begin,
        end);
  }

 

  /**
   * Returns an xs:integer equal to the length in characters of the value of
   * $arg. If the value of $arg is the empty sequence, the xs:integer 0 is
   * returned.
   * 
   * @param arg
   *          a given string
   * @return the length of the given string
   */
  public int stringLength(final IAxis arg) {

    return arg.getTransaction().getValue().length();
  }

  /**
   * Returns the value of string value (calculated using fn:string()) of the
   * context item (.) with whitespace normalized by stripping leading and
   * trailing whitespace and replacing sequences of one or more than one
   * whitespace character with a single space, #x20. If the value of arg is the
   * empty sequence, returns the zero-length string. If the context item is
   * undefined an error is raised: [err:XPDY0002]XP.
   * 
   * @return the whitespace normalized string value of the context node
   */
  public String normalizeSpace() {

    throw new IllegalStateException("not implemented yet.");
  }

  /**
   * Returns the value of $arg with whitespace normalized by stripping leading
   * and trailing whitespace and replacing sequences of one or more than one
   * whitespace character with a single space, #x20. If the value of arg is the
   * empty sequence, returns the zero-length string.
   * 
   * @param arg
   *          the item that's string value should be normalized
   * @return the whitespace normalized string value of the given item
   */
  public String normalizeSpace(final IAxis arg) {

    // leading and trailing WS removed
    final String trim = arg.getTransaction().getValue().trim();

    throw new IllegalStateException("not implemented yet.");
  }

  /**
   * @param axis
   * @return the unicode normalized string value of the given item
   */
  public String normalizeUnicode(final IAxis axis) {

    throw new IllegalStateException("not implemented yet.");
  }

  public String normalizeUnicode(final IAxis arg, final String normalizationForm) {

    // leading and trailing WS removed
    final String trim = arg.getTransaction().getValue().trim();

    throw new IllegalStateException("not implemented yet.");
  }

 
  public boolean contains(final IAxis arg1, final IAxis arg2) {

    return arg1.getTransaction().getValue().contains(
        arg1.getTransaction().getValue());
  }

 
  public boolean startsWith(final IAxis arg1, final IAxis arg2) {

    return arg1.getTransaction().getValue().startsWith(
        arg2.getTransaction().getValue());
  }

  public boolean endsWith(final IAxis arg1, final IAxis arg2) {

    return arg1.getTransaction().getValue().startsWith(
        arg2.getTransaction().getValue());
  }

    /**
   * {@inheritDoc}
   */
  public String getFacet() {

    return ".*"; // stringRep ::= Char* => all characters
  }


}
