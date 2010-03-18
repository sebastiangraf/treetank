/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: NamePageBinding.java 4442 2008-08-30 16:17:17Z kramis $
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
     */
    public NamePage(final long revision) {
        super(0, revision);
        mNameMap = new HashMap<Integer, String>();
        // mRawNameMap = new HashMap<Integer, byte[]>();
    }

    /**
     * Read name page.
     * 
     * @param in
     *            Input bytes to read from.
     */
    protected NamePage(final ITTSource in) {
        super(0, in);

        int mapSize = in.readInt();

        mNameMap = new HashMap<Integer, String>(mapSize);
        for (int i = 0, l = (int) mapSize; i < l; i++) {
            final int key = in.readInt();
            final int valSize = in.readInt();
            final byte[] bytes = new byte[valSize];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = in.readByte();
            }
            mNameMap.put(key, TypedValue.parseString(bytes));
            // mRawNameMap.put(key, bytes);
        }
    }

    /**
     * Clone name page.
     * 
     * @param committedNamePage
     *            Page to clone.
     */
    public NamePage(final NamePage committedNamePage, final long revisionToUse) {
        super(0, committedNamePage, revisionToUse);
        mNameMap = new HashMap<Integer, String>(committedNamePage.mNameMap);
        // mRawNameMap = new HashMap<Integer, byte[]>(
        // committedNamePage.mRawNameMap);
    }

    /**
     * Get name belonging to name key.
     * 
     * @param key
     *            Name key identifying name.
     * @return Name of name key.
     */
    public String getName(final int key) {
        return mNameMap.get(key);
    }

    /**
     * Get raw name belonging to name key.
     * 
     * @param key
     *            Name key identifying name.
     * @return Raw name of name key.
     */
    public byte[] getRawName(final int key) {
        return TypedValue.getBytes(mNameMap.get(key));
    }

    /**
     * Create name key given a name.
     * 
     * @param key
     *            Key for given name.
     * @param name
     *            Name to create key for.
     */
    public void setName(final int key, final String name) {
        mNameMap.put(key, name);
        // mRawNameMap.put(key, TypedValue.getBytes(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serialize(final ITTSink out) {
        super.serialize(out);

        out.writeInt(mNameMap.size());

        for (final int key : mNameMap.keySet()) {
            out.writeInt(key);
            byte[] tmp = TypedValue.getBytes(mNameMap.get(key));
            out.writeInt(tmp.length);
            for (final byte byteVal : tmp) {
                out.writeByte(byteVal);
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
