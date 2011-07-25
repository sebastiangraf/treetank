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

package org.treetank.gui.view;

import org.treetank.api.IItem;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.gui.view.sunburst.EGreyState;
import org.treetank.gui.view.sunburst.EXPathState;

import processing.core.PGraphics;

/**
 * Interface for a visual item.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface IVisualItem extends Comparable<IVisualItem> {
    /**
     * Update an item.
     * 
     * @param paramMappingMode
     *            determines how to normalize
     * @param paramGraphics
     *            the {@link PGraphics} instance to write to
     */
    void update(final int paramMappingMode, final PGraphics paramGraphics);

    /**
     * Item hovered.
     * 
     * @param paramGraphic
     *            {@link PGraphics} instance
     */
    void hover(final PGraphics paramGraphic);

    /**
     * Get node key.
     * 
     * @return node key
     */
    long getNodeKey();

    /**
     * Set XPath state.
     * 
     * @param paramState
     *            {@link EXPathState} value
     */
    void setXPathState(final EXPathState paramState);

    /**
     * Set grey state.
     * 
     * @param paramState
     *            {@link EGreyState} value
     */
    void setGreyState(final EGreyState paramState);

    /**
     * Get type of diff.
     * 
     * @return type of diff
     */
    EDiff getDiff();

    /**
     * Get item.
     * 
     * @return item
     */
    IItem getNode();
}
