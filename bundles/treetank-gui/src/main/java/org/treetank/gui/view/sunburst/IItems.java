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
package org.treetank.gui.view.sunburst;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.treetank.api.IReadTransaction;

/**
 * Interface which has to be implemented from {@link Callable} implementations inside the model.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public interface IItems {
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
}
