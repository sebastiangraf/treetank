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

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.IOException;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;
import org.treetank.page.NodePage.DeletedNode;
import org.treetank.page.interfaces.IPage;

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
     * @param pInput
     *            source to read from
     * @return the created page
     * @throws TTIOException
     */
    public IPage deserializePage(final DataInput pInput) throws TTIOException {
        try {
            final int kind = pInput.readInt();
            switch (kind) {
            case IConstants.NODEPAGE:
                NodePage nodePage = new NodePage(pInput.readLong(), pInput.readLong());
                for (int offset = 0; offset < IConstants.CONTENT_COUNT; offset++) {
                    int nodeKind = pInput.readInt();
                    if (nodeKind != IConstants.NULL_NODE) {
                        if (nodeKind == IConstants.DELETEDNODE) {
                            nodePage.getNodes()[offset] = new DeletedNode(pInput.readLong());
                        } else {
                            nodePage.getNodes()[offset] = mNodeFac.deserializeNode(pInput);
                        }
                    }
                }
                return nodePage;
            case IConstants.METAPAGE:
                MetaPage metaPage = new MetaPage(pInput.readLong());
                final int mapSize = pInput.readInt();
                IMetaEntry key;
                IMetaEntry value;
                for (int i = 0; i < mapSize; i++) {
                    key = mEntryFac.deserializeEntry(pInput);
                    value = mEntryFac.deserializeEntry(pInput);
                    metaPage.setEntry(key, value);
                }
                return metaPage;
            case IConstants.UBERPAGE:
                UberPage uberPage = new UberPage(pInput.readLong(), pInput.readLong(), pInput.readLong());
                uberPage.setReferenceKey(0, pInput.readLong());
                return uberPage;
            case IConstants.INDIRCTPAGE:
                IndirectPage indirectPage = new IndirectPage(pInput.readLong());
                for (int offset = 0; offset < indirectPage.getReferenceKeys().length; offset++) {
                    indirectPage.setReferenceKey(offset, pInput.readLong());
                }
                return indirectPage;
            case IConstants.REVISIONROOTPAGE:
                RevisionRootPage revRootPage =
                    new RevisionRootPage(pInput.readLong(), pInput.readLong(), pInput.readLong());
                for (int offset = 0; offset < revRootPage.getReferenceKeys().length; offset++) {
                    revRootPage.setReferenceKey(offset, pInput.readLong());
                }
                return revRootPage;
            default:
                throw new IllegalStateException(
                    "Invalid Kind of Page. Something went wrong in the serialization/deserialization");
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mNodeFac", mNodeFac).add("mEntryFac", mEntryFac).toString();
    }

}
