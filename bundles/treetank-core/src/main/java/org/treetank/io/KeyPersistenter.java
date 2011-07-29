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

package org.treetank.io;

import org.treetank.io.berkeley.BerkeleyKey;
import org.treetank.io.file.FileKey;

/**
 * Factory to build the key out of a fixed source.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class KeyPersistenter {

    /** Constant to define the file-keys. */
    private static final int FILEKIND = 1;
    /** Constant to define the berkeley-keys. */
    private static final int BERKELEYKIND = 2;
    /** Constant to define that no key is stored. */
    private static final int NULLKIND = 3;

    // /**
    // private enum Keys {
    // /** File-keys. */
    // FILE(1),
    //
    // /** Berkeley-keys. */
    // BERKELEY(2),
    //
    // /** No key is stored. */
    // NULL(3);
    //
    // /** The actual key. */
    // private final int mKey;
    //
    // /**
    // * Constructor.
    // *
    // * @param paramKey
    // * the actual key
    // */
    // Keys(final int paramKey) {
    // mKey = paramKey;
    // }
    //
    // /**
    // * Get key.
    // *
    // * @return the Key
    // */
    // public int getKey() {
    // return mKey;
    // }
    // }

    /**
     * Empty constructor for this class, should never be initialized.
     */
    private KeyPersistenter() {
        // Method to prohibit instantiation.
        throw new AssertionError("Can't be instantiated!");
    }

    /**
     * Simple create-method.
     * 
     * @param paramSource
     *            the input from the storage
     * @return the Key.
     */
    public static AbsKey createKey(final ITTSource paramSource) {
        final int kind = paramSource.readInt();
        AbsKey returnVal = null;
        switch (kind) {
        case FILEKIND:
            returnVal = new FileKey(paramSource);
            break;
        case BERKELEYKIND:
            returnVal = new BerkeleyKey(paramSource);
            break;
        case NULLKIND:
            returnVal = null;
            break;
        default:
            throw new IllegalStateException(new StringBuilder("Kind ").append(kind).append(" is not known")
                .toString());
        }

        return returnVal;
    }

    /**
     * Serialize a key to a designated {@link ITTSink}.
     * 
     * @param paramSink
     *            where the data should be serialized to
     * @param paramKey
     *            key which is serialized
     */
    public static void serializeKey(final ITTSink paramSink, final AbsKey paramKey) {
        if (paramKey == null) {
            paramSink.writeInt(NULLKIND);
        } else {
            if (paramKey instanceof FileKey) {
                paramSink.writeInt(FILEKIND);
            } else if (paramKey instanceof BerkeleyKey) {
                paramSink.writeInt(BERKELEYKIND);
            } else {
                throw new IllegalStateException(new StringBuilder("Key ").append(paramKey.getClass()).append(
                    " cannot be serialized").toString());
            }

            for (final long val : paramKey.getKeys()) {
                paramSink.writeLong(val);
            }
        }

    }
}
