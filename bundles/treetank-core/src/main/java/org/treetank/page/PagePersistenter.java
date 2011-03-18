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
package org.treetank.page;

import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;

public final class PagePersistenter {

    public final static int NODEPAGE = 1;
    public final static int NAMEPAGE = 2;
    public final static int UBERPAGE = 3;
    public final static int INDIRCTPAGE = 4;
    public final static int REVISIONROOTPAGE = 5;

    /**
     * Empty constructor, not needed since access occures with static methods.
     */
    private PagePersistenter() {
        // Not needed over here
    }

    public static AbsPage createPage(final ITTSource mSource) {
        final int kind = mSource.readInt();
        AbsPage returnVal = null;
        switch (kind) {
        case NODEPAGE:
            returnVal = new NodePage(mSource);
            break;
        case NAMEPAGE:
            returnVal = new NamePage(mSource);
            break;
        case UBERPAGE:
            returnVal = new UberPage(mSource);
            break;
        case INDIRCTPAGE:
            returnVal = new IndirectPage(mSource);
            break;
        case REVISIONROOTPAGE:
            returnVal = new RevisionRootPage(mSource);
            break;
        default:
            throw new IllegalStateException(
                "Invalid Kind of Page. Something went wrong in the serialization/deserialization");
        }
        return returnVal;
    }

    public static void serializePage(final ITTSink mSink, final AbsPage mPage) {

        if (mPage instanceof NodePage) {
            mSink.writeInt(PagePersistenter.NODEPAGE);

        } else if (mPage instanceof IndirectPage) {
            mSink.writeInt(PagePersistenter.INDIRCTPAGE);

        } else if (mPage instanceof NamePage) {
            mSink.writeInt(PagePersistenter.NAMEPAGE);

        } else if (mPage instanceof RevisionRootPage) {
            mSink.writeInt(PagePersistenter.REVISIONROOTPAGE);

        } else if (mPage instanceof UberPage) {
            mSink.writeInt(PagePersistenter.UBERPAGE);

        } else {
            throw new IllegalStateException(new StringBuilder("Page ").append(mPage.getClass()).append(
                " cannot be serialized").toString());
        }
        mPage.serialize(mSink);
    }

}
