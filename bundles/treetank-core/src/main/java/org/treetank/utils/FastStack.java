/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.utils;

/**
 * <h1>FastStack</h1>
 * 
 * <p>
 * Unsynchronized stack optimized for generic type. Is significantly faster than Stack.
 * </p>
 * 
 * @param <E>
 *            Generic type.
 */
public final class FastStack<E> {

    /** Internal array to store stack elements. */
    private E[] mStack;

    /** Current size of stack. */
    private int mSize;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public FastStack() {
        mStack = (E[])new Object[16];
        mSize = 0;
    }

    /**
     * Private constructor used for clone method.
     * 
     * @param mObject
     *            The array from which to create a new stack.
     */
    private FastStack(E[] mObject) {
        mStack = mObject;
        mSize = 0;
    }

    /**
     * Place new element on top of stack. This might require to double the size
     * of the internal array.
     * 
     * @param mElement
     *            Element to push.
     */
    @SuppressWarnings("unchecked")
    public void push(final E mElement) {
        if (mStack.length == mSize) {
            final E[] biggerStack = (E[])new Object[mStack.length << 1];
            System.arraycopy(mStack, 0, biggerStack, 0, mStack.length);
            mStack = biggerStack;
        }
        mStack[mSize++] = mElement;
    }

    /**
     * Get the element on top of the stack. The internal array performs boundary
     * checks.
     * 
     * @return Topmost stack element.
     */
    public E peek() {
        return mStack[mSize - 1];
    }

    /**
     * Get element at given position in stack. The internal array performs
     * boundary checks.
     * 
     * @param position
     *            Position in stack from where to get the element.
     * @return Stack element at given position.
     */
    public E get(final int position) {
        return mStack[position];
    }

    /**
     * Remove topmost element from stack.
     * 
     * @return Removed topmost element of stack.
     */
    public E pop() {
        return mStack[--mSize];
    }

    /**
     * Reset the stack.
     * 
     */
    public void clear() {
        mSize = 0;
    }

    /**
     * Get the current size of the stack.
     * 
     * @return Current size of stack.
     */
    public int size() {
        return mSize;
    }

    /**
     * Is the stack empty?
     * 
     * @return True if there are no elements anymore. False else.
     */
    public boolean empty() {
        return (mSize == 0);
    }

    /**
     * Clone a stack.
     * 
     * @return Cloned stack.
     */
    @Override
    @SuppressWarnings("unchecked")
    public FastStack<E> clone() {
        final E[] object = (E[])new Object[mStack.length];
        System.arraycopy(mStack, 0, object, 0, mStack.length);
        return new FastStack(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < mSize; i++) {
            builder.append(mStack[i]);
            if (i < mSize) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

}
