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
import org.treetank.exception.TTException;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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

    /**
     * Create name page.
     */
    public NamePage() {
        mNameMap = new HashMap<Integer, String>();
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NamePage [mNameMap=");
        builder.append(mNameMap);
        builder.append("]");
        return builder.toString();
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
    public void commit(PageWriteTrx paramState) throws TTException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.NAMEPAGE);
        pOutput.writeInt(mNameMap.size());

        for (final int key : mNameMap.keySet()) {
            pOutput.writeInt(key);
            final byte[] tmp = mNameMap.get(key).getBytes();
            pOutput.writeInt(tmp.length);
            pOutput.write(tmp);
        }
        return pOutput.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPageKey() {
        // TODO Auto-generated method stub
        return 0;
    }

}
