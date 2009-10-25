/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: FastStack.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.utils;

/**
 * <h1>FastStack</h1>
 * 
 * <p>
 * Unsynchronized stack optimized for generic type. Is significantly faster than
 * Stack.
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
     * 
     */
    @SuppressWarnings("unchecked")
    public FastStack() {
        mStack = (E[]) new Object[16];
        mSize = 0;
    }

    /**
     * Place new element on top of stack. This might require to double the size
     * of the internal array.
     * 
     * @param element
     *            Element to push.
     */
    @SuppressWarnings("unchecked")
    public final void push(final E element) {
        if (mStack.length == mSize) {
            E[] biggerStack = (E[]) new Object[mStack.length << 1];
            System.arraycopy(mStack, 0, biggerStack, 0, mStack.length);
            mStack = biggerStack;
        }
        mStack[mSize++] = element;
    }

    /**
     * Get the element on top of the stack. The internal array performs boundary
     * checks.
     * 
     * @return Topmost stack element.
     */
    public final E peek() {
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
    public final E get(final int position) {
        return mStack[position];
    }

    /**
     * Remove topmost element from stack.
     * 
     * @return Removed topmost element of stack.
     */
    public final E pop() {
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

    /**
     * Is the stack empty?
     * 
     * @return True if there are no elements anymore. False else.
     */
    public final boolean empty() {
        return (mSize == 0);
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
