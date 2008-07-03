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

package org.treetank.xpath;

/**
 * <h1> XPathScanner </h1>
 * <p>Lexical scanner to extract tokens from the query.</p>
 * <p>
 * This scanner is used to interpret the query. It reads the the query string
 * char by char and specifies the type of the input and creates a token for
 * every logic text unit.
 * </p>
 */
public class XPathScanner implements XPathConstants {

  /** The XPath query to scan. */
  private final String mQuery;

  /** The current position of the cursor to the query string. */
  private int mPos;

  /** Start position of the last item. */
  private int mLastPos;

  /** Scanner states. */
  private enum State {
    START, NUMBER, TEXT, SPECIAL, /* Unary special character. */
    SPECIAL2, /* special character with 2 possible characters. */
    COMMENT, E_NUM, UNKNOWN
  }

  /** The state the scanner is currently in. */
  private State mState;

  /** Contains the current content of the token. */
  private StringBuilder mOutput;

  /**
   * Defines if all digits of a token have been read or if the token still can
   * have more digits.
   */
  private boolean mFinnished;

  /** The type of the current token. */
  private Token mType;

  /** The current character. */
  private char mInput;

  /**
   * State with which the next token starts. Sometimes it is not needed to start
   * in the start state for some tokens, as their type is known because of the
   * preceding token.
   */
  private State mStartState;

  /**
   * Counts the number of nested comments. Is needed to distinguish whether the
   * current token is part of a comment, or part of the query. If 
   * mCommentCount > 0, the current token is part of a comment.
   */
  private int mCommentCount;

  /**
   * Constructor. Initializes the internal state. Receives query and adds a end
   * mark to it.
   * 
   * @param query
   *          the query to scan
   */
  public XPathScanner(final String query) {

    mQuery = query + '#'; // end mark to recognize the end
    mPos = 0;
    mLastPos = mPos;
    mStartState = State.START;
    mCommentCount = 0;
  }

  /**
   * Reads the string char by char and returns one token by call. The scanning
   * starts in the start state, if not further specified before, and specifies
   * the next scanner state and the type of the future token according to its
   * first char. As soon as the current char does not fit the conditions for the
   * current token type, the token is generated and returned.
   * 
   * @return token The new token.
   */
  public XPathToken nextToken() {

    // some tokens start in another state than the START state
    mState = mStartState;
    // reset startState
    mStartState = State.START;
    mOutput = new StringBuilder();
    mFinnished = false;
    mType = Token.INVALID;
    mLastPos = mPos;

    do {
      mInput = mQuery.charAt(mPos);

      switch (mState) {
        case START: // specify token type according to first char
          scanStart();
          break;
        case NUMBER: // number
          scanNumber();
          break;
        case TEXT: // some text, could be a name
          scanText();
          break;
        case SPECIAL2: // special character that could have 2 digits
          scanSpecial2();
          break;
        case COMMENT:
          scanComment();
          break;
        case E_NUM:
          scanENum();
          break;
        default:
          mPos++;
          mFinnished = true;
      }
    } while (!mFinnished || mPos >= mQuery.length());

    if (mCommentCount > 0) {
      throw new IllegalStateException("Error in Query. Comment does not end.");
    }

    return new XPathToken(mOutput.toString(), mType);
  }

  /**
   * Scans the first character of a token and decides, what type it is.
   */
  private void scanStart() {

    if (isNumber(mInput)) {
      mState = State.NUMBER;
      mOutput.append(mInput);
      mType = Token.VALUE; // number
    } else if (isFirstLetter(mInput)) {
      mState = State.TEXT; // word
      mOutput.append(mInput);
      mType = Token.TEXT;
    } else if (isSpecialCharacter(mInput)) {
      mState = State.SPECIAL; // special character with only one digit
      mOutput.append(mInput);
      mType = retrieveType(mInput);
      mFinnished = true;
    } else if (isSpecialCharacter2(mInput)) {
      mState = State.SPECIAL2; // 2 digit special character
      mOutput.append(mInput);
      mType = retrieveType(mInput);
    } else if ((mInput == ' ') || (mInput == '\n')) {
      mState = State.START;
      mOutput.append(mInput);
      mFinnished = true;
      mType = Token.SPACE;
    } else if (mInput == '#') {
      mType = Token.END; // end of query
      mFinnished = true;
      mPos--;
    } else {
      mState = State.UNKNOWN; // unknown character
      mOutput.append(mInput);
      mFinnished = true;
    }
    mPos++;
  }

  /**
   * Returns the type of the given character.
   * 
   * @param input
   *          The character the type should be determined
   * @return type of the given character.
   */
  private Token retrieveType(final char input) {

    Token type;
    switch (input) {
      case ',':
        type = Token.COMMA;
        break;
      case '(':
        type = Token.OPEN_BR;
        break;
      case ')':
        type = Token.CLOSE_BR;
        break;
      case '[':
        type = Token.OPEN_SQP;
        break;
      case ']':
        type = Token.CLOSE_SQP;
        break;
      case '@':
        type = Token.AT;
        break;
      case '=':
        type = Token.EQ;
        break;
      case '<':
      case '>':
        type = Token.COMP;
        break;
      case '!':
        type = Token.N_EQ;
        break;
      case '/':
        type = Token.SLASH;
        break;
      case ':':
        type = Token.COLON;
        break;
      case '.':
        type = Token.POINT;
        break;
      case '+':
        type = Token.PLUS;
        break;
      case '-':
        type = Token.MINUS;
        break;
      case '\'':
        type = Token.SINGLE_QUOTE;
        break;
      case '"':
        type = Token.DBL_QUOTE;
        break;
      case '$':
        type = Token.DOLLAR;
        break;
      case '?':
        type = Token.INTERROGATION;
        break;
      case '*':
        type = Token.STAR;
        break;
      case '|':
        type = Token.OR;
        break;
      default:
        type = Token.INVALID;
    }
    return type;

  }

  /**
   * Checks if the given character is a valid first letter.
   * 
   * @param input
   *          The character to check.
   * @return Returns true, if the character is a first letter.
   */
  private boolean isFirstLetter(final char input) {

    return ((input >= 'a' && input <= 'z') 
        || (input >= 'A' && input <= 'Z') || (input == '_'));
  }

  /**
   * Checks if the given character is a number.
   * 
   * @param input
   *          The character to check.
   * @return Returns true, if the character is a number.
   */
  private boolean isNumber(final char input) {

    return (input >= '0' && input <= '9');
  }

  /**
   * Checks if the given character is a special character that can have 2
   * digits.
   * 
   * @param input
   *          The character to check.
   * @return Returns true, if the character is a special character that can have
   *         2 digits.
   */
  private boolean isSpecialCharacter2(final char input) {

    return (input == '<') || (input == '>') || (input == '(') || (input == '!')
        || (input == '/') || (input == '.');
  }

  /**
   * Checks if the given character is a special character.
   * 
   * @param input
   *          The character to check.
   * @return Returns true, if the character is a special character.
   */
  private boolean isSpecialCharacter(final char input) {

    return ((input == ')') || (input == ';') || (input == ',')
        || (input == '@') || (input == '[') || (input == ']') || (input == '=')
        || (input == '"') || (input == '\'') || (input == '$')
        || (input == ':') || (input == '|') || (input == '+') || (input == '-')
        || (input == '?') || (input == '*'));
  }

  /**
   * Scans a number token. A number only consists of digits.
   */
  private void scanNumber() {

    if ((mInput >= '0' && mInput <= '9')) {
      mOutput.append(mInput);
      mPos++;
    } else {
      // could be an e-number
      if (mInput == 'E' || mInput == 'e') {
        mStartState = State.E_NUM;
      }
      mFinnished = true;
    }
  }

  /**
   * Scans text token. A text is everything that with a character. It can
   * contain numbers, all letters in upper or lower case and underscores.
   */
  private void scanText() {

    if (isLetter(mInput)) {
      mOutput.append(mInput);
      mPos++;

    } else {
      mType = Token.TEXT;
      mFinnished = true;
    }
  }

  /**
   * Scans special characters that can have more then one digit. E.g. ==, !=,
   * <=, >=, //, .., (:
   */
  private void scanSpecial2() {

    if ((mInput == '=' 
      && (mType == Token.COMP || mType == Token.EQ || mType == Token.N_EQ))) {
      mOutput.append(mInput);
      mPos++;
    } else if (mInput == '/' && (mType == Token.SLASH)) {
      mOutput.append(mInput);
      mType = Token.DESC_STEP;
      mPos++;
    } else if (mInput == '.' && (mType == Token.POINT)) {
      mOutput.append(mInput);
      mType = Token.PARENT;
      mPos++;
    } else if (mInput == '<' && mOutput.toString().equals("<")) {
      mOutput.append(mInput);
      mType = Token.L_SHIFT;
      mPos++;
    } else if (mInput == '>' && mOutput.toString().equals(">")) {
      mOutput.append(mInput);
      mType = Token.R_SHIFT;
      mPos++;
    } else if (mInput == ':' && mType == Token.OPEN_BR) {
      // could be start of a comment
      mOutput = new StringBuilder();
      mType = Token.COMMENT;
      mCommentCount++;
      mState = State.COMMENT;
      mPos++;
    } else {
      mFinnished = true;
    }
  }

  /**
   * Scans all numbers that contain an e. 
   */
  private void scanENum() {

    if (mInput == 'E' || mInput == 'e') {
      mOutput.append(mInput);
      mState = State.START;
      mType = Token.E_NUMBER;
      mFinnished = true;
      mPos++;
    } else {
      mFinnished = true;
      mState = State.START;
      mType = Token.INVALID;
    }
  }

  private void scanComment() {

    if (mInput == ':') {
      // check if is end of comment, indicated by ':)'
      final char input2 = mQuery.charAt(mPos + 1);
      if (input2 == ')') {
        mCommentCount--;
        if (mCommentCount == 0) {
          mState = State.START;
          // increment position, because next digit has already been processed
          mPos++;
        }

      }
    } else if (mInput == '(') {
      // check if start of new nested comment, indicated by '(:'
      final char input2 = mQuery.charAt(mPos + 1);
      if (input2 == ':') {
        mCommentCount++;

      }
    }
    mPos++;
  }

  /**
   * Checks if the given character is a letter.
   * 
   * @param input
   *          The character to check.
   * @return Returns true, if the character is a letter.
   */
  private boolean isLetter(final char input) {

    return ((input >= '0' && input <= '9') || (input >= 'a' && input <= 'z')
        || (input >= 'A' && input <= 'Z') || (input == '_') || (input == '-') 
        || (input == '.'));

  }

  /**
   * Return the token that will be returned by the scanner after the >next<th
   * call of nextToken(), without changing the internal state of the scanner.
   * 
   * @param next
   *          number of next tokens to be read
   * @return token that will be read after calling nextToken() >next< times
   */
  public XPathToken lookUpTokens(final int next) {

    int nextCount = next;

    // save current position of the scanner, to restore it later
    final int lastPos = mPos;
    XPathToken token = nextToken();

    while (--nextCount > 0) {
      token = nextToken();
      if (token.getType() == Token.SPACE) {
        nextCount++;
      }
    }

    // reset position
    mPos = lastPos;
    return token;
  }

  /**
   * Returns the beginning of a query that has already been scanned. This can be
   * used by the client e.g. for error messages in case of unexpected token
   * occurs.
   * 
   * @return string so far
   */
  public String begin() {

    return mQuery.substring(0, mLastPos);
  }

  /**
   * Return the current cursor position in the query.
   * 
   * @return current position of the cursor
   */
  public int getPos() {

    return mPos;
  }
}
