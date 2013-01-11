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

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Factory to deserialize pages out of a chunk of bytes.
 * This factory needs a {@link INodeFactory}-reference to perform inlying node-serializations as well.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Singleton
public final class PageFactory {

    /** Node Factory to be initialized. */
    private final INodeFactory mNodeFac;

    /** MetaEntry Factory to be initialized. */
    private final IMetaEntryFactory mEntryFac;

    /**
     * Constructor.
     * 
     * @param pNodeFac
     *            to be set
     */
    @Inject
    public PageFactory(final INodeFactory pNodeFac, final IMetaEntryFactory pMetaFac) {
        mNodeFac = pNodeFac;
        mEntryFac = pMetaFac;
    }

    /**
     * Create page.
     * 
     * @param paramSource
     *            source to read from
     * @return the created page
     */
    public IPage deserializePage(final byte[] pSource) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pSource);
        final int kind = input.readInt();
        switch (kind) {
        case IConstants.NODEPAGE:
            NodePage nodePage = new NodePage(input.readLong());
            for (int offset = 0; offset < IConstants.CONTENT_COUNT; offset++) {
                int length = input.readInt();
                if (length != IConstants.NULL_NODE) {
                    byte[] toread = new byte[length];
                    input.readFully(toread);
                    nodePage.getNodes()[offset] = mNodeFac.deserializeNode(toread);
                }
            }
            return nodePage;
        case IConstants.METAPAGE:
            MetaPage metaPage = new MetaPage(input.readLong());
            final int mapSize = input.readInt();
            int size = -1;
            byte[] bytes;
            IMetaEntry key;
            IMetaEntry value;
            for (int i = 0; i < mapSize; i++) {
                size = input.readInt();
                bytes = new byte[size];
                input.readFully(bytes);
                key = mEntryFac.deserializeEntry(bytes);
                size = input.readInt();
                bytes = new byte[size];
                input.readFully(bytes);
                value = mEntryFac.deserializeEntry(bytes);
                metaPage.setEntry(key, value);
            }
            return metaPage;
        case IConstants.UBERPAGE:
            UberPage uberPage = new UberPage(input.readLong(), input.readLong(), input.readLong());
            uberPage.setReferenceKey(0, input.readLong());
            return uberPage;
        case IConstants.INDIRCTPAGE:
            IndirectPage indirectPage = new IndirectPage(input.readLong());
            for (int offset = 0; offset < indirectPage.getReferenceKeys().length; offset++) {
                indirectPage.setReferenceKey(offset, input.readLong());
            }
            return indirectPage;
        case IConstants.REVISIONROOTPAGE:
            RevisionRootPage revRootPage =
                new RevisionRootPage(input.readLong(), input.readLong(), input.readLong());
            for (int offset = 0; offset < revRootPage.getReferenceKeys().length; offset++) {
                revRootPage.setReferenceKey(offset, input.readLong());
            }
            return revRootPage;
        default:
            throw new IllegalStateException(
                "Invalid Kind of Page. Something went wrong in the serialization/deserialization");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mNodeFac == null) ? 0 : mNodeFac.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PageFactory [mNodeFac=");
        builder.append(mNodeFac);
        builder.append("]");
        return builder.toString();
    }
}
