
package org.treetank.xpath;

import org.treetank.api.IFilter;

/**
 * <h1>SequenceType</h1>
 * <p>
 * A sequence type defines a type a the items in a sequnce can have. It consists
 * of either an empty-sequence-test, or an ItemType(kind test, item() or atomic
 * value) and an optional wildcard (*, ?, +)
 * </p>
 * 
 * @author Tina Scherer
 */
public class SequenceType {

  private final boolean mIsEmptySequence;

  private final IFilter mFilter;

  private final boolean mHasWildcard;

  private final char mWildcard;

  /**
   * Constructor with no arguments means, the sequence type is the empty
   * sequence.
   */
  public SequenceType() {

    mIsEmptySequence = true;
    mHasWildcard = false;
    mWildcard = ' ';
    mFilter = null;
  }

  /**
   * Constructor. Sequence type is an ItemType.
   * 
   * @param filter
   *          item type filter
   */
  public SequenceType(final IFilter filter) {

    mIsEmptySequence = false;
    mFilter = filter;
    mHasWildcard = false;
    mWildcard = ' ';
  }

  /**
   * Constructor. Sequence type is an ItemType with an wildcard.
   * <li>'ItemType ?' means the sequence has zero or one items that are of the
   * ItemType</li>
   * <li>'ItemType +' means the sequence one or more items that are of the
   * ItemType</li>
   * <li>'ItemType *' means the sequence has zero or more items that are of the
   * ItemType</li>
   * 
   * @param filter
   *          item type filter
   * @param wildcard
   *          either '*', '?' or '+'
   */
  public SequenceType(final IFilter filter, final char wildcard) {

    mIsEmptySequence = false;
    mFilter = filter;
    mHasWildcard = true;
    mWildcard = wildcard;
  }

  /**
   * 
   * @return true, if sequence is the empty sequence
   */
  public boolean isEmptySequence() {

    return mIsEmptySequence;
  }

  /**
   * @return the ItemType test
   */
  public IFilter getFilter() {

    return mFilter;
  }

  /**
   * @return true, if a wildcard is present
   */
  public boolean hasWildcard() {

    return mHasWildcard;
  }

  /**
   * Returns the wildcard's char representation.
   * 
   * @return wildcard sign
   */
  public char getWildcard() {

    return mWildcard;
  }

}
