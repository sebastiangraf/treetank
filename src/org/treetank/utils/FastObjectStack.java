/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
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
  private Object[] stack;

  /** Current size of stack. */
  private int size;

  /**
   * Constructor.
   *
   */
  public FastObjectStack() {
    stack = new Object[32];
    size = 0;
  }

  /**
   * Place new element on top of stack. This might require to double the
   * size of the internal array.
   * 
   * @param element Element to push.
   */
  public final void push(final Object element) {
    if (stack.length == size) {
      Object[] biggerStack = new Object[stack.length << 1];
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
  public final Object peek() {
    return stack[size - 1];
  }

  /**
   * Get element at given position in stack. The internal array performs
   * boundary checks.
   * 
   * @param position Position in stack from where to get the element.
   * @return Stack element at given position.
   */
  public final Object get(final int position) {
    return stack[position];
  }

  /**
   * Remove topmost element from stack.
   * 
   * @return Removed topmost element of stack.
   */
  public final Object pop() {
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
