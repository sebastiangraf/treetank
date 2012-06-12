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

import java.util.HashMap;
import java.util.Map;

import org.treetank.access.PageWriteTrx;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.page.delegates.PageDelegate;
import org.treetank.utils.TypedValue;

/**
 * <h1>NamePageBinding</h1>
 * 
 * <p>
 * Name page holds all names and their keys for a revision.
 * </p>
 */
public final class NamePage implements IPage {

    /** Map the hash of a name to its name. */
    private final Map<Integer, String> mNameMap;

    private final PageDelegate mDelegate;

    /**
     * Create name page.
     * 
     * @param paramRevision
     *            Revision number.
     */
    public NamePage(final long paramRevision) {
        mDelegate = new PageDelegate(0, paramRevision);
        mNameMap = new HashMap<Integer, String>();
    }

    /**
     * Read name page.
     * 
     * @param paramIn
     *            Input bytes to read from.
     */
    protected NamePage(final ITTSource paramIn) {
        mDelegate = new PageDelegate(0, paramIn.readLong());
        mDelegate.initialize(paramIn);
        final int mapSize = paramIn.readInt();

        mNameMap = new HashMap<Integer, String>(mapSize);
        for (int i = 0, l = (int)mapSize; i < l; i++) {
            final int key = paramIn.readInt();
            final int valSize = paramIn.readInt();
            final byte[] bytes = new byte[valSize];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = paramIn.readByte();
            }
            mNameMap.put(key, new String(bytes));
        }
    }

    /**
     * Get name belonging to name key.
     * 
     * @param mKey
     *            Name key identifying name.
     * @return Name of name key.
     */
    public String getName(final int mKey) {
        return mNameMap.get(mKey);
    }

    /**
     * Get raw name belonging to name key.
     * 
     * @param mKey
     *            Name key identifying name.
     * @return Raw name of name key.
     */
    public byte[] getRawName(final int mKey) {
        return TypedValue.getBytes(mNameMap.get(mKey));
    }

    /**
     * Create name key given a name.
     * 
     * @param paramKey
     *            Key for given name.
     * @param paramName
     *            Name to create key for.
     */
    public void setName(final int paramKey, final String paramName) {
        mNameMap.put(paramKey, paramName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink paramOut) {
        mDelegate.serialize(paramOut);

        paramOut.writeInt(mNameMap.size());

        for (final int key : mNameMap.keySet()) {
            paramOut.writeInt(key);
            final byte[] tmp = TypedValue.getBytes(mNameMap.get(key));
            paramOut.writeInt(tmp.length);
            for (final byte byteVal : tmp) {
                paramOut.writeByte(byteVal);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ": nameCount=" + mNameMap.size();
    }

    /**
     * Get name map.
     * 
     * @return name map
     */
    public Map<Integer, String> getNameMap() {
        return mNameMap;
    }

    @Override
    public void commit(PageWriteTrx paramState) throws AbsTTException {
        mDelegate.commit(paramState);
    }

    @Override
    public PageReference[] getReferences() {
        return mDelegate.getReferences();
    }

    @Override
    public long getRevision() {
        return mDelegate.getRevision();
    }

}
