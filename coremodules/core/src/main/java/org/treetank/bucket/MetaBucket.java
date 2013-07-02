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

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntry;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTIOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

/**
 * <h1>MetaBucket</h1>
 * 
 * <p>
 * This bucket stored variable key -> value mappings, whereas elements must implement the {@link IMetaEntry}s.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class MetaBucket implements IBucket {

    /** Map the hash of a name to its name. */
    private final ConcurrentHashMap<IMetaEntry, IMetaEntry> mMetaMap;

    /** Key of this bucket. */
    private final long mBucketKey;

    /**
     * Create name bucket.
     * 
     * @param pBucketKey
     *            key of this bucket
     */
    public MetaBucket(final long pBucketKey) {
        mMetaMap = new ConcurrentHashMap<IMetaEntry, IMetaEntry>();
        mBucketKey = pBucketKey;
    }

    /**
     * Putting an entry to the map.
     * 
     * @param pKey
     *            to be stored.
     * @param pVal
     *            to be stored.
     * @return if entry already existing, return that one.
     * @see ConcurrentHashMap#put(Object, Object)
     */
    public IMetaEntry put(final IMetaEntry pKey, final IMetaEntry pVal) {
        return mMetaMap.put(pKey, pVal);
    }

    /**
     * Getting an entry.
     * 
     * @param pKey
     *            to be retrieved
     * @return the suitable value, if present. Null otherwise
     * @see ConcurrentHashMap#get(Object)
     */
    public IMetaEntry get(final IMetaEntry pKey) {
        return mMetaMap.get(pKey);
    }

    /**
     * Getting the size.
     * 
     * @return the number of entries in this bucket
     * @see ConcurrentHashMap#size()
     */
    public int size() {
        return mMetaMap.size();
    }

    /**
     * Getting the entry set of the bucket.
     * 
     * @return the entry set of this bucket
     * @see ConcurrentHashMap#entrySet()
     */
    public Set<Map.Entry<IMetaEntry, IMetaEntry>> entrySet() {
        return mMetaMap.entrySet();
    }

    /**
     * Removing an element within this bucket
     * 
     * @param pKey
     *            to be removed
     * @return the element removed, null otherwise
     * @see ConcurrentHashMap#remove(Object)
     */
    public IMetaEntry remove(final IMetaEntry pKey) {
        return mMetaMap.remove(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.METABUCKET);
            pOutput.writeLong(mBucketKey);
            pOutput.writeInt(mMetaMap.size());
            for (final Map.Entry<IMetaEntry, IMetaEntry> key : mMetaMap.entrySet()) {
                key.getKey().serialize(pOutput);
                key.getValue().serialize(pOutput);
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBucketKey() {
        return mBucketKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mBucketKey", mBucketKey).add("mMetaMap", mMetaMap).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 42677;
        int result = 1;
        result = prime * result + (int)(mBucketKey ^ (mBucketKey >>> 32));
        result = prime * result + ((mMetaMap == null) ? 0 : mMetaMap.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
      return obj.hashCode()==this.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HashCode  secureHash() {
        final Hasher code = StandardSettings.HASHFUNC.newHasher().putLong(mBucketKey);
        for (final IMetaEntry key : mMetaMap.keySet()) {
            final IMetaEntry val = mMetaMap.get(key);
            code.putObject(key, key.getFunnel());
            code.putObject(val, val.getFunnel());
        }
        return code.hash();
    }

}
