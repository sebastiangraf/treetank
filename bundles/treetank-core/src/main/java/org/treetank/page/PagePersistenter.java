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
 *     * Neither the name of the <organization> nor the
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
