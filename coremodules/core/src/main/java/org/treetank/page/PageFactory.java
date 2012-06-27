/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import java.nio.ByteBuffer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public final class PageFactory {

    public final static int NODEPAGE = 1;
    public final static int NAMEPAGE = 2;
    public final static int UBERPAGE = 3;
    public final static int INDIRCTPAGE = 4;
    public final static int REVISIONROOTPAGE = 5;

    /**
     * Create page.
     * 
     * @param paramSource
     *            source to read from
     * @return the created page
     */
    public static IPage createPage(final byte[] pSource) {
        final int kind = ByteBuffer.wrap(pSource).getInt();
        IPage returnVal = null;
        switch (kind) {
        case NODEPAGE:
            returnVal = new NodePage(pSource);
            break;
        case NAMEPAGE:
            returnVal = new NamePage(pSource);
            break;
        case UBERPAGE:
            returnVal = new UberPage(pSource);
            break;
        case INDIRCTPAGE:
            returnVal = new IndirectPage(pSource);
            break;
        case REVISIONROOTPAGE:
            returnVal = new RevisionRootPage(pSource);
            break;
        default:
            throw new IllegalStateException(
                    "Invalid Kind of Page. Something went wrong in the serialization/deserialization");
        }
        return returnVal;
    }

    /**
     * Serialize page.
     * 
     * @param paramSink
     *            output sink
     * @param paramPage
     *            the page to serialize
     */
    public static byte[] serializePage(final IPage paramPage) {
        final ByteArrayDataOutput data = ByteStreams.newDataOutput();

        if (paramPage instanceof NodePage) {
            data.writeInt(PageFactory.NODEPAGE);
        } else if (paramPage instanceof IndirectPage) {
            data.writeInt(PageFactory.INDIRCTPAGE);
        } else if (paramPage instanceof NamePage) {
            data.writeInt(PageFactory.NAMEPAGE);
        } else if (paramPage instanceof RevisionRootPage) {
            data.writeInt(PageFactory.REVISIONROOTPAGE);
        } else if (paramPage instanceof UberPage) {
            data.writeInt(PageFactory.UBERPAGE);
        } else {
            throw new IllegalStateException(new StringBuilder("Page ")
                    .append(paramPage.getClass())
                    .append(" cannot be serialized").toString());
        }
        data.write(paramPage.getByteRepresentation());
        return data.toByteArray();
    }

}
