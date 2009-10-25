/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: IndirectPage.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.page;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.io.PagePersistenter;
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
     */
    public IndirectPage() {
        super(IConstants.INP_REFERENCE_COUNT);
    }

    /**
     * Read indirect page.
     * 
     * @param in
     *            Input bytes.
     */
    public IndirectPage(final ITTSource in) {
        super(IConstants.INP_REFERENCE_COUNT, in);
    }

    /**
     * Clone indirect page.
     * 
     * @param page
     *            Page to clone.
     */
    public IndirectPage(final IndirectPage page) {
        super(IConstants.INP_REFERENCE_COUNT, page);
    }

    @Override
    public void serialize(final ITTSink out) {
        out.writeInt(PagePersistenter.INDIRCTPAGE);
        super.serialize(out);
    }

}
