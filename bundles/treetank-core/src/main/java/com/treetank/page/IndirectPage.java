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

package com.treetank.page;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * <h1>IndirectPage</h1>
 * 
 * <p>
 * Indirect page holds a set of references to build a reference tree.
 * </p>
 */
public final class IndirectPage extends AbstractPage {

    /**
     * Create indirect page.
     * 
     * @param mRevision
     *            Revision Number
     */
    public IndirectPage(final long mRevision) {
        super(IConstants.INP_REFERENCE_COUNT, mRevision);
    }

    /**
     * Read indirect page.
     * 
     * @param mIn
     *            Input bytes.
     */
    protected IndirectPage(final ITTSource mIn) {
        super(IConstants.INP_REFERENCE_COUNT, mIn);
    }

    /**
     * Clone indirect page.
     * 
     * @param page
     *            Page to clone.
     * @param revisionToUse
     *            Revision number to use
     */
    public IndirectPage(final IndirectPage page, final long revisionToUse) {
        super(IConstants.INP_REFERENCE_COUNT, page, revisionToUse);
    }

    @Override
    protected void serialize(final ITTSink mOut) {
        super.serialize(mOut);
    }

}
