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

package org.treetank.gui.view.sunburst;

import org.treetank.gui.view.model.IContainer;

/**
 * Contains settings used for updating the model.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstContainer implements IContainer {

    /** Index of currently clicked {@link SunburstItem}. */
    public transient int mHitTestIndex;
    
    /** Revision to compare. */
    public transient long mRevision;

    /** Max depth in the tree. */
    public transient int mDepth;

    /** Modification weight. */
    public transient float mModWeight;

    /** Node key to start from. */
    public transient long mKey;

    /** Determines if pruning should be enabled or not. */
    public transient EPruning mPruning;

    /** {@inheritDoc} */
    @Override
    public SunburstContainer setStartKey(final long paramKey) {
        assert paramKey >= 0;
        mKey = paramKey;
        return this;
    }

    /**
     * Set revision to compare.
     * 
     * @param paramRevision
     *            the Revision to set
     * @return this
     */
    public SunburstContainer setRevision(final long paramRevision) {
        assert paramRevision > 0;
        mRevision = paramRevision;
        return this;
    }
    
    /**
     * Set hit test item.
     * 
     * @param paramHitTestIndex
     *            the index of the {@link SunburstItem} currently selected
     * @return this
     */
    public SunburstContainer setHitItem(final int paramHitTestIndex) {
        assert paramHitTestIndex >= 0;
        mHitTestIndex = paramHitTestIndex;
        return this;
    }

    /**
     * Set modification weight.
     * 
     * @param paramModWeight
     *            the modWeight to set
     * @return this
     */
    public SunburstContainer setModWeight(final float paramModWeight) {
        assert paramModWeight >= 0;
        mModWeight = paramModWeight;
        return this;
    }

    /**
     * Set all remaining member variables.
     * 
     * @param paramRevision
     *            revision to compare
     * @param paramDepth
     *            Depth in the tree
     * @param paramModificationWeight
     *            weighting of modifications
     * @return this
     */
    public SunburstContainer setAll(final long paramRevision, final int paramDepth,
        final float paramModificationWeight) {
        assert paramRevision >= 0;
        assert paramDepth >= 0;
        assert paramModificationWeight >= 0;
        mRevision = paramRevision;
        mDepth = paramDepth;
        mModWeight = paramModificationWeight;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SunburstContainer setPruning(final EPruning paramPruning) {
        assert paramPruning != null;
        mPruning = paramPruning;
        return this;
    }
}
