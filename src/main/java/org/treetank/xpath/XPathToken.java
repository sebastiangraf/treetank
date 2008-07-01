
package org.treetank.xpath;

/**
 * <h1>XPathToken </h1>
 * <p>
 * Categorized block of text.
 * </p>
 * 
 * @author Tina Scherer
 */
public class XPathToken implements XPathConstants {

  /**
   * The content of the token, a text sequence that represents a text, a number,
   * a special character etc.
   */
  private final String mContent;

  /** Specifies the type that the content of the token has. */
  private final Token mType;

  /**
   * Constructor initializing internal state.
   * 
   * @param str
   *          the content of the token
   * @param type
   *          the type of the token
   */
  public XPathToken(final String str, final Token type) {

    mContent = str;
    mType = type;
  }

  /**
   * Gets the content of the token.
   * 
   * @return the content
   */
  public String getContent() {

    return mContent;
  }

  /**
   * Gets the type of the token.
   * 
   * @return the type
   */
  public Token getType() {

    return mType;
  }

}
