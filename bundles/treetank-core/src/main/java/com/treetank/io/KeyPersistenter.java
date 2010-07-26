package com.treetank.io;

import com.treetank.io.berkeley.BerkeleyKey;
import com.treetank.io.file.FileKey;

/**
 * Factory to build the key out of a fixed source.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class KeyPersistenter {

    /** Constant to define the file-keys */
    private static final int FILEKIND = 1;
    /** Constant to define the berkeley-keys */
    private static final int BERKELEYKIND = 2;
    /** Constant to define that no key is stored */
    private static final int NULLKIND = 3;

    private KeyPersistenter() {
        // method to prohibit instantiation
    }

    /**
     * Simple create-method.
     * 
     * @param source
     *            the input from the storage
     * @return the Key.
     */
    public static AbstractKey createKey(final ITTSource source) {
        final int kind = source.readInt();
        AbstractKey returnVal = null;
        switch (kind) {
        case FILEKIND:
            returnVal = new FileKey(source);
            break;
        case BERKELEYKIND:
            returnVal = new BerkeleyKey(source);
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

    public static void serializeKey(final ITTSink sink, final AbstractKey key) {

        if (key == null) {
            sink.writeInt(NULLKIND);
        } else {

            if (key instanceof FileKey) {
                sink.writeInt(FILEKIND);
            } else if (key instanceof BerkeleyKey) {
                sink.writeInt(BERKELEYKIND);
            } else {
                throw new IllegalStateException(new StringBuilder("Key ").append(key.getClass()).append(
                    " cannot be serialized").toString());
            }

            for (long val : key.getKeys()) {
                sink.writeLong(val);
            }
        }

    }
}
