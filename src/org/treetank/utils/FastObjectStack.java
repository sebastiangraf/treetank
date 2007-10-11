/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.utils;

/**
 * <h1>FastLongStack</h1>
 * 
 * <p>
 * Unsynchronized stack optimized for long primitive type. Is significantly
 * faster than Stack<Long>.
 * </p>
 */
public final class FastObjectStack {

  /** Internal array to store stack elements. */
  private Object[] mStack;

  /** Current size of stack. */
  private int mSize;

  /**
   * Constructor.
   *
   */
  public FastObjectStack() {
    mStack = new Object[32];
    mSize = 0;
  }

  /**
   * Place new element on top of stack. This might require to double the
   * size of the internal array.
   * 
   * @param element Element to push.
   */
  public final void push(final Object element) {
    if (mStack.length == mSize) {
      Object[] biggerStack = new Object[mStack.length << 1];
      System.arraycopy(mStack, 0, biggerStack, 0, mStack.length);
      mStack = biggerStack;
    }
    mStack[mSize++] = element;
  }

  /**
   * Get the element on top of the stack. The internal array performs
   * boundary checks.
   * 
   * @return Topmost stack element.
   */
  public final Object peek() {
    return mStack[mSize - 1];
  }

  /**
   * Get element at given position in stack. The internal array performs
   * boundary checks.
   * 
   * @param position Position in stack from where to get the element.
   * @return Stack element at given position.
   */
  public final Object get(final int position) {
    return mStack[position];
  }

  /**
   * Remove topmost element from stack.
   * 
   * @return Removed topmost element of stack.
   */
  public final Object pop() {
    return mStack[--mSize];
  }

  /**
   * Reset the stack.
   * 
   */
  public final void clear() {
    mSize = 0;
  }

  /**
   * Get the current size of the stack.
   * 
   * @return Current size of stack.
   */
  public final int size() {
    return mSize;
  }

}
