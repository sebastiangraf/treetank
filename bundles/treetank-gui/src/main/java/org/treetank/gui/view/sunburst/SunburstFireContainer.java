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
package org.treetank.gui.view.sunburst;

import java.util.List;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class SunburstFireContainer {
    transient List<SunburstItem> mItems;
    
    transient int mOldDepthMax;
    
    transient int mDepthMax;
    
    SunburstFireContainer(final List<SunburstItem> paramItems, final int paramDepthMax) {
        assert paramItems != null;
        assert paramDepthMax >= 0;
        mItems = paramItems;
        mDepthMax = paramDepthMax;
    }
    
    SunburstFireContainer setOldDepthMax(final int paramOldDepthMax) {
        assert paramOldDepthMax >= 0;
        mOldDepthMax = paramOldDepthMax;
        return this;
    }
}
