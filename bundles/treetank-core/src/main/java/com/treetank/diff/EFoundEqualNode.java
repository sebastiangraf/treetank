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
package com.treetank.diff;

/**
 * Determines if an equal node is found.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
enum FoundEqualNode {
    /** Node found. */
    TRUE {
        /** {@inheritDoc} */
        @Override
        EDiff kindOfDiff(final int paramRightSibls) {
            EDiff mod = EDiff.SAME;
            switch (paramRightSibls) {
            case 0:
                throw new AssertionError("May not happen!");
            default:
                // It has been deleted.
                mod = EDiff.DELETED;
                break;
            }
            return mod;
        }
    },

    /** Node not found. */
    FALSE {
        /** {@inheritDoc} */
        @Override
        EDiff kindOfDiff(final int paramRightSibls) {
            EDiff mod = EDiff.SAME;
            switch (paramRightSibls) {
            case 0:
                mod = EDiff.INSERTED;
                break;
            case 1:
                mod = EDiff.RENAMED;
                break;
            default:
                mod = EDiff.INSERTED;     
            }
            
            assert mod != EDiff.SAME;
            return mod;
        }
    };

    /**
     * Kind of difference between two nodes.
     * 
     * @param paramRightSibls
     *            counts right siblings
     * @return kind of difference
     */
    abstract EDiff kindOfDiff(final int paramRightSibls);
}
