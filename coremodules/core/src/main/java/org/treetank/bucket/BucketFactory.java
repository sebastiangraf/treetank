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

package org.treetank.bucket;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.IOException;

import org.treetank.api.IDataFactory;
import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.bucket.DataBucket.DeletedData;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTIOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Factory to deserialize buckets out of a chunk of bytes.
 * This factory needs a {@link IDataFactory}-reference to perform inlying data-serializations as well.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Singleton
public final class BucketFactory {

    /** Data Factory to be initialized. */
    private final IDataFactory mDataFac;

    /** MetaEntry Factory to be initialized. */
    private final IMetaEntryFactory mEntryFac;

    /**
     * Constructor.
     * 
     * @param pDataFac
     *            to be set
     */
    @Inject
    public BucketFactory(final IDataFactory pDataFac, final IMetaEntryFactory pMetaFac) {
        mDataFac = pDataFac;
        mEntryFac = pMetaFac;
    }

    /**
     * Create bucket.
     * 
     * @param pInput
     *            source to read from
     * @return the created bucket
     * @throws TTIOException
     */
    public IBucket deserializeBucket(final DataInput pInput) throws TTIOException {
        try {
            final int kind = pInput.readInt();
            byte[] hash;
            switch (kind) {
            case IConstants.DATABUCKET:
                DataBucket dataBucket = new DataBucket(pInput.readLong(), pInput.readLong());
                for (int offset = 0; offset < IConstants.CONTENT_COUNT; offset++) {
                    int dataKind = pInput.readInt();
                    if (dataKind != IConstants.NULLDATA) {
                        if (dataKind == IConstants.DELETEDDATA) {
                            dataBucket.getDatas()[offset] = new DeletedData(pInput.readLong());
                        } else {
                            dataBucket.getDatas()[offset] = mDataFac.deserializeData(pInput);
                        }
                    }
                }
                return dataBucket;
            case IConstants.METABUCKET:
                MetaBucket metaBucket = new MetaBucket(pInput.readLong());
                final int mapSize = pInput.readInt();
                IMetaEntry key;
                IMetaEntry value;
                for (int i = 0; i < mapSize; i++) {
                    key = mEntryFac.deserializeEntry(pInput);
                    value = mEntryFac.deserializeEntry(pInput);
                    metaBucket.put(key, value);
                }
                return metaBucket;
            case IConstants.UBERBUCKET:
                UberBucket uberBucket =
                    new UberBucket(pInput.readLong(), pInput.readLong(), pInput.readLong());
                uberBucket.setReferenceKey(0, pInput.readLong());
                hash = new byte[pInput.readInt()];
                pInput.readFully(hash);
                uberBucket.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, hash);
                return uberBucket;
            case IConstants.INDIRCTBUCKET:
                IndirectBucket indirectBucket = new IndirectBucket(pInput.readLong());
                for (int offset = 0; offset < indirectBucket.getReferenceKeys().length; offset++) {
                    indirectBucket.setReferenceKey(offset, pInput.readLong());
                }
                for (int offset = 0; offset < indirectBucket.getReferenceHashs().length; offset++) {
                    hash = new byte[pInput.readInt()];
                    pInput.readFully(hash);
                    indirectBucket.setReferenceHash(offset, hash);
                }
                return indirectBucket;
            case IConstants.REVISIONROOTBUCKET:
                RevisionRootBucket revRootBucket =
                    new RevisionRootBucket(pInput.readLong(), pInput.readLong(), pInput.readLong());
                for (int offset = 0; offset < revRootBucket.getReferenceKeys().length; offset++) {
                    revRootBucket.setReferenceKey(offset, pInput.readLong());
                }
                for (int offset = 0; offset < revRootBucket.getReferenceHashs().length; offset++) {
                    hash = new byte[pInput.readInt()];
                    pInput.readFully(hash);
                    revRootBucket.setReferenceHash(offset, hash);
                }
                return revRootBucket;
            default:
                throw new IllegalStateException(
                    "Invalid Kind of Bucket. Something went wrong in the serialization/deserialization");
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
        return toStringHelper(this).add("mDataFac", mDataFac).add("mEntryFac", mEntryFac).toString();
    }

}
