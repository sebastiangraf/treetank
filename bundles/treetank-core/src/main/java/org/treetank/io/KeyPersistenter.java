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

    /**
     * Empty constructor for this class, should never be initialized.
     */
    private KeyPersistenter() {
        // method to prohibit instantiation
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
     *            where the data should be serialized to.
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

            for (long val : paramKey.getKeys()) {
                paramSink.writeLong(val);
            }
        }

    }
}
