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

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.exception.TTIOException;
import org.treetank.page.interfaces.IPage;

/**
 * <h1>MetaPage</h1>
 * 
 * <p>
 * This page stored variable key -> value mappings, whereas elements must implement the {@link IMetaEntry}s.
 * </p>
 */
public final class MetaPage implements IPage {

    /** Map the hash of a name to its name. */
    private final Map<IMetaEntry, IMetaEntry> mMetaMap;

    /** Key of this page. */
    private final long mPageKey;

    /**
     * Create name page.
     * 
     * @param pPageKey
     *            key of this page
     */
    public MetaPage(final long pPageKey) {
        mMetaMap = new HashMap<IMetaEntry, IMetaEntry>();
        mPageKey = pPageKey;
    }

    /**
     * Get name belonging to name key.
     * 
     * @param pKey
     *            key identifying value.
     * @return value of this key
     */
    public IMetaEntry getValue(final IMetaEntry pKey) {
        return mMetaMap.get(pKey);
    }

    /**
     * Create name key given a name.
     * 
     * @param pKey
     *            Key to be set
     * @param pValue
     *            related value to be set
     */
    public void setEntry(final IMetaEntry pKey, final IMetaEntry pValue) {
        mMetaMap.put(pKey, pValue);
    }

    /**
     * Get name map.
     * 
     * @return name map
     */
    public Map<IMetaEntry, IMetaEntry> getMetaMap() {
        return mMetaMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.METAPAGE);
            pOutput.writeLong(mPageKey);
            pOutput.writeInt(mMetaMap.size());
            for (final IMetaEntry key : mMetaMap.keySet()) {
                key.serialize(pOutput);
                mMetaMap.get(key).serialize(pOutput);
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPageKey() {
        return mPageKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPageKey", mPageKey).add("mMetaMap", mMetaMap).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mPageKey, mMetaMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

}
