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
 *     * Neither the name of the University of Konstanz nor the
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

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * XPath enum to determine if current item is found by an XPath expression or not.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
enum EXPathState {
    /** Item is found. */
    ISFOUND {
        /**
         * {@inheritDoc}
         */
        @Override
        void setStroke(final PApplet paramParent, final int paramColor) {
            paramParent.stroke(1);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        void setStrokeBuffer(final PGraphics paramBuffer, final int paramColor) {
            paramBuffer.stroke(1);
        }
    },

    /** Default: Item is not found. */
    ISNOTFOUND {
        /**
         * {@inheritDoc}
         */
        @Override
        void setStroke(final PApplet paramParent, final int paramColor) {
            paramParent.stroke(paramColor);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        void setStrokeBuffer(final PGraphics paramBuffer, final int paramColor) {
            paramBuffer.stroke(paramColor);
        }
    };

    /**
     * Set stroke.
     * 
     * @param paramParent
     *            parent processing buffer
     * @param paramColor
     *            the color to use
     */
    abstract void setStroke(final PApplet paramParent, final int paramColor);
    
    /**
     * Set stroke buffer.
     * 
     * @param paramBuffer
     *            Processing {@link PGraphics}
     * @param paramColor
     *            the color to use
     */
    abstract void setStrokeBuffer(final PGraphics paramBuffer, final int paramColor);
}
