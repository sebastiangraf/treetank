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
    private transient int mHitTestIndex;

    /** Revision to compare. */
    private transient long mRevision;

    /** Max depth in the tree. */
    private transient int mDepth;

    /** Modification weight. */
    private transient float mModWeight;

    /** Node key to start from. */
    private transient long mKey;

    /** Determines if pruning should be enabled or not. */
    private transient EPruning mPruning;

    /** GUI which extends {@link AbsSunburstGUI}. */
    private transient AbsSunburstGUI mGUI;

    /**
     * Constructor.
     * 
     * @param paramGUI
     *            GUI which extends {@link AbsSunburstGUI}
     */
    public SunburstContainer(final AbsSunburstGUI paramGUI) {
        assert paramGUI != null;
        mGUI = paramGUI;
    }

    /**
     * Set the GUI.
     * 
     * @param paramGUI
     *            GUI which extends {@link AbsSunburstGUI}
     * @return instance of this class
     * */
    public SunburstContainer setGUI(final AbsSunburstGUI paramGUI) {
        assert paramGUI != null;
        mGUI = paramGUI;
        return this;
    }

    /**
     * Get GUI.
     * 
     * @return GUI which extends {@link AbsSunburstGUI}
     */
    public AbsSunburstGUI getGUI() {
        return mGUI;
    }

    /** {@inheritDoc} */
    @Override
    public SunburstContainer setStartKey(final long paramKey) {
        assert paramKey >= 0;
        mKey = paramKey;
        return this;
    }

    /**
     * Get start key.
     * 
     * @return the key
     */
    public long getStartKey() {
        return mKey;
    }

    /**
     * Set revision to compare.
     * 
     * @param paramRevision
     *            the Revision to set
     * @return instance of this class
     */
    public SunburstContainer setRevision(final long paramRevision) {
        assert paramRevision > 0;
        mRevision = paramRevision;
        return this;
    }

    /**
     * @return the mRevision
     */
    public long getRevision() {
        return mRevision;
    }

    /**
     * Set modification weight.
     * 
     * @param paramModWeight
     *            the modWeight to set
     * @return instance of this class
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
     * @return instance of this class
     */
    public SunburstContainer setAll(final long paramRevision, final int paramDepth,
        final float paramModificationWeight) {
        setRevision(paramRevision);
        setDepth(paramDepth);
        setModWeight(paramModificationWeight);
        return this;
    }

    /**
     * Set depth.
     * 
     * @param paramDepth
     *            the depth to set
     * @return instance of this class
     */
    public SunburstContainer setDepth(final int paramDepth) {
        assert paramDepth >= 0;
        mDepth = paramDepth;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SunburstContainer setPruning(final EPruning paramPruning) {
        assert paramPruning != null;
        mPruning = paramPruning;
        return this;
    }

    /**
     * Get pruning.
     * 
     * @return pruning method
     */
    public EPruning getPruning() {
        return mPruning;
    }

    /**
     * Get the depth.
     * 
     * @return the depth
     */
    public int getDepth() {
        return mDepth;
    }

    /**
     * Get modification weight.
     * 
     * @return the modification weight
     */
    public float getModWeight() {
        return mModWeight;
    }

    /**
     * Set hit test index.
     * 
     * @param paramHitTestIndex
     *            the hitTestIndex to set
     * @return instance of this class
     */
    public SunburstContainer setHitTestIndex(final int paramHitTestIndex) {
        assert paramHitTestIndex > -1;
        mHitTestIndex = paramHitTestIndex;
        return this;
    }

    /**
     * @return the mHitTestIndex
     */
    public int getHitTestIndex() {
        return mHitTestIndex;
    }
}
