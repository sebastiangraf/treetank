/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.gui.view.sunburst;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.treetank.api.IReadTransaction;

/**
 * Interface which models of the {@link SunburstView} have to implement.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
interface IModel extends Iterable<SunburstItem> {
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
}
