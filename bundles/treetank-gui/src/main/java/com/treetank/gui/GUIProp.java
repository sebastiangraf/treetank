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
package com.treetank.gui;

/**
 * <h1>GUIProps</h1>
 * 
 * <p>GUI properties.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class GUIProp {
    /** Show views enum. */
    public enum EShowViews {
        /** Show tree view. */
        SHOWTREE(true), 
        
        /** Show text view. */
        SHOWTEXT(true), 
        
        /** Show treemap view. */
        SHOWTREEMAP(false),
        
        /** Show sunburst view. */
        SHOWSUNBURST(false);

        /** Determines if view should be shown. */
        private boolean mShow;
        
        /**
         * Constructor.
         * 
         * @param paramShow
         *             determines if view should be shown
         */
        EShowViews(final boolean paramShow) {
            mShow = paramShow;
        }
        
        /**
         * Set show value.
         * 
         * @param paramShow
         *             determines if view should be shown
         * @return 
         */
        public void setValue(final boolean paramShow) {
            mShow = paramShow;
        }
        
        /** 
         * Get show value. 
         * 
         * @return the show value. 
         */
        public boolean getValue() {
            return mShow;
        }
    }
    
    /** Indent spaces. */
    public static final int INDENT_SPACES = 2;
    
    /** Newline string representation. */
    public static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Constructor.
     */
    GUIProp() {

    }
}
