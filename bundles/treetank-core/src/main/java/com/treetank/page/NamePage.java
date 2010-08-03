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

package com.treetank.page;

import java.util.HashMap;
import java.util.Map;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.TypedValue;

/**
 * <h1>NamePageBinding</h1>
 * 
 * <p>
 * Name page holds all names and their keys for a revision.
 * </p>
 */
public final class NamePage extends AbstractPage {

    /** Map the hash of a name to its name. */
    private final Map<Integer, String> mNameMap;

    // /** Map the hash of a name to its name. */
    // private final Map<Integer, byte[]> mRawNameMap;

    /**
     * Create name page.
     * 
     * @param mRevision
     *            Revision number.
     */
    public NamePage(final long mRevision) {
        super(0, mRevision);
        mNameMap = new HashMap<Integer, String>();
        // mRawNameMap = new HashMap<Integer, byte[]>();
    }

    /**
     * Read name page.
     * 
     * @param mIn
     *            Input bytes to read from.
     */
    protected NamePage(final ITTSource mIn) {
        super(0, mIn);

        final int mapSize = mIn.readInt();

        mNameMap = new HashMap<Integer, String>(mapSize);
        for (int i = 0, l = (int)mapSize; i < l; i++) {
            final int key = mIn.readInt();
            final int valSize = mIn.readInt();
            final byte[] bytes = new byte[valSize];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = mIn.readByte();
            }
            mNameMap.put(key, TypedValue.parseString(bytes));
            // mRawNameMap.put(key, bytes);
        }
    }

    /**
     * Clone name page.
     * 
     * @param mCommittedNamePage
     *            Page to clone.
     * @param revisionToUse
     *            Revision Number to use.
     */
    public NamePage(final NamePage mCommittedNamePage, final long revisionToUse) {
        super(0, mCommittedNamePage, revisionToUse);
        mNameMap = new HashMap<Integer, String>(mCommittedNamePage.mNameMap);
        // mRawNameMap = new HashMap<Integer, byte[]>(
        // committedNamePage.mRawNameMap);
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
     * @param mKey
     *            Key for given name.
     * @param mName
     *            Name to create key for.
     */
    public void setName(final int mKey, final String mName) {
        mNameMap.put(mKey, mName);
        // mRawNameMap.put(key, TypedValue.getBytes(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serialize(final ITTSink mOut) {
        super.serialize(mOut);

        mOut.writeInt(mNameMap.size());

        for (final int key : mNameMap.keySet()) {
            mOut.writeInt(key);
            byte[] tmp = TypedValue.getBytes(mNameMap.get(key));
            mOut.writeInt(tmp.length);
            for (final byte byteVal : tmp) {
                mOut.writeByte(byteVal);
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

    public Map<Integer, String> getNameMap() {
        return mNameMap;
    }

}
