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
package com.treetank.gui.view.sunburst;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.treetank.api.IReadTransaction;

/**
 * Interface which models of the {@link SunburstView} have to implement.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
interface IModel extends Iterable<SunburstItem> {
    
    /**
     * Get a list of descendants per node.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction} over which to iterate.
     * @return List of {@link Future}s.
     * @throws ExecutionException
     *             if execution fails
     * @throws InterruptedException
     *             if task gets interrupted
     */
    List<Future<Integer>> getDescendants(final IReadTransaction paramRtx) throws InterruptedException,
        ExecutionException;

    /**
     * Get the {@link SunburstItem} at the specified index.
     * 
     * @param paramIndex
     *            the index
     * @return the {@link SunburstItem} at the specified index
     * @throws IndexOutOfBoundsException
     *             if index > mItems.size() - 1 or < 0
     */
    SunburstItem getItem(final int paramIndex) throws IndexOutOfBoundsException;

    /**
     * Traverse the tree and create a {@link List} of {@link SunburstItem}s.
     * 
     * @param paramContainer
     *            simple container
     */
    void traverseTree(final SunburstContainer paramContainer);

    /** Undo operation. */
    void undo();

    /**
     * Update root of the tree with the node currently clicked.
     * 
     * @param paramContainer
     *            simple container
     */
    void update(final SunburstContainer paramContainer);

    /**
     * XPath evaluation.
     * 
     * @param paramXPathExpression
     *            XPath expression to evaluate.
     */
    void evaluateXPath(final String paramXPathExpression);
    
    /** 
     * Create a {@link SunburstItem} used as a callback method in {@link SunburstDescendantAxis}. 
     * 
     * @param paramItem
     *          {@link Item} reference
     * @param paramDepth
     *          current depth in the tree
     * @param mIndex
     *          index of the current item
     * @return child extension
     */
    float createSunburstItem(final Item paramItem, final int paramDepth, final int mIndex);
}
