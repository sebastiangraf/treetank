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
 * faster than Stack<Integer>.
 * </p>
 */
public final class FastIntStack {

  /** Internal array to store stack elements. */
  private int[] stack;

  /** Current size of stack. */
  private int size;

  /**
   * Constructor.
   *
   */
  public FastIntStack() {
    stack = new int[32];
    size = 0;
  }

  /**
   * Place new element on top of stack. This might require to double the
   * size of the internal array.
   * 
   * @param element Element to push.
   */
  public final void push(final int element) {
    if (stack.length == size) {
      int[] biggerStack = new int[stack.length << 1];
      System.arraycopy(stack, 0, biggerStack, 0, stack.length);
      stack = biggerStack;
    }
    stack[size++] = element;
  }

  /**
   * Get the element on top of the stack. The internal array performs
   * boundary checks.
   * 
   * @return Topmost stack element.
   */
  public final int peek() {
    return stack[size - 1];
  }

  /**
   * Get element at given position in stack. The internal array performs
   * boundary checks.
   * 
   * @param position Position in stack from where to get the element.
   * @return Stack element at given position.
   */
  public final int get(final int position) {
    return stack[position];
  }

  /**
   * Remove topmost element from stack.
   * 
   * @return Removed topmost element of stack.
   */
  public final int pop() {
    return stack[--size];
  }

  /**
   * Reset the stack.
   * 
   */
  public final void clear() {
    size = 0;
  }

  /**
   * Get the current size of the stack.
   * 
   * @return Current size of stack.
   */
  public final int size() {
    return size;
  }

}
