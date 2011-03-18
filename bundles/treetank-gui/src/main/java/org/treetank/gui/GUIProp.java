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
package org.treetank.gui;

/**
 * <h1>GUIProps</h1>
 * 
 * <p>
 * GUI properties.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class GUIProp {
    /** Show views enum. */
    public enum EShowViews {
        /** Show tree view. */
        SHOWTREE(false),

        /** Show text view. */
        SHOWTEXT(false),

        /** Show treemap view. */
        SHOWTREEMAP(false),

        /** Show sunburst view. */
        SHOWSUNBURST(true);

        /** Determines if view should be shown. */
        private boolean mShow;

        /**
         * Constructor.
         * 
         * @param paramShow
         *            determines if view should be shown
         */
        EShowViews(final boolean paramShow) {
            mShow = paramShow;
        }

        /**
         * Invert show value.
         */
        public void invert() {
            mShow = !mShow;
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
    private transient int mIndentSpaces = 2;

    /**
     * Default constructor.
     */
    public GUIProp() {

    }

    // ACCESSORS ==============================================

    /**
     * Set how many spaces should be used per level to indent.
     * 
     * @param paramIndentSpaces
     *            spaces to indent
     */
    public void setIndentSpaces(final int paramIndentSpaces) {
        mIndentSpaces = paramIndentSpaces;
    }

    /**
     * Get spaces to indent.
     * 
     * @return spaces to indent.
     */
    public int getIndentSpaces() {
        return mIndentSpaces;
    }
}
