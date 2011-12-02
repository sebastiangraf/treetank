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
/**
 * 
 */
package org.treetank.gui.view.sunburst;

import org.treetank.gui.ReadDB;
import org.treetank.gui.view.smallmultiples.SmallMultiplesGUI;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;

import processing.core.PApplet;

/**
 * GUI Factory.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public enum EGUIFactory {
    /** Sunburst GUI. */
    SUNBURSTGUI {
        /** {@inheritDoc} */
        @Override
        public AbsSunburstGUI getInstance(final PApplet paramApplet, final AbsSunburstControl paramControl, final ReadDB paramReadDB) {
            checkParams(paramApplet, paramControl, paramReadDB);
            return SunburstGUI.getInstance(paramApplet, paramControl, paramReadDB);
        }
    },
    
    /** Small multiples GUI. */
    SMALLMULTIPLESGUI {
        /** {@inheritDoc} */
        @Override
        public AbsSunburstGUI getInstance(final PApplet paramApplet, final AbsSunburstControl paramControl, final ReadDB paramReadDB) {
            checkParams(paramApplet, paramControl, paramReadDB);
            return SmallMultiplesGUI.getInstance(paramApplet, paramControl, paramReadDB);
        }
    };
    
    /**
     * Get an instance of a GUI which extends {@link AbsSunburstGUI}. The classes itself have to implement
     * a singleton mechanism if it's necessary.
     * 
     * @param paramApplet
     *            parent processing applet
     * @param paramControl
     *            associated controller
     * @param paramReadDB
     *            read database
     * @return instance of a GUI which extends {@link AbsSunburstGUI}
     */
    public abstract AbsSunburstGUI getInstance(final PApplet paramApplet, final AbsSunburstControl paramControl, final ReadDB paramReadDB);

    /**
     * Check parameters.
     * 
     * @param paramApplet
     *            parent processing applet
     * @param paramControl
     *            associated controller
     * @param paramReadDB
     *            read database
     */
    private static void checkParams(PApplet paramApplet, AbsSunburstControl paramControl, ReadDB paramReadDB) {
        if (paramApplet == null || paramControl == null || paramReadDB == null) {
            throw new IllegalArgumentException("Non of the parameters can be null!");
        }
    }
}
