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

import java.util.List;

/**
 * Encapsulates stuff which has to be fired from the model.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SunburstFireContainer {
    /** {@link List} of {@link SunburstItem}s. */
    transient List<SunburstItem> mItems;

    /** Maximum depth in old revision. */
    transient int mOldDepthMax;

    /** Maximum depth in current revision. */
    transient int mDepthMax;

    /**
     * Constructor.
     * 
     * @param paramItems
     *            {@link List} of {@link SunburstItem}s
     * 
     * @param paramDepthMax
     *            maximum depth in current revision
     */
    SunburstFireContainer(final List<SunburstItem> paramItems, final int paramDepthMax) {
        assert paramItems != null;
        assert paramDepthMax >= 0;
        mItems = paramItems;
        mDepthMax = paramDepthMax;
    }

    /**
     * Set old maximum depth.
     * 
     * @param paramOldDepthMax
     *            maximum depth in old revision
     * @return SunburstFireContainer instance
     */
    SunburstFireContainer setOldDepthMax(final int paramOldDepthMax) {
        assert paramOldDepthMax >= 0;
        mOldDepthMax = paramOldDepthMax;
        return this;
    }
}
