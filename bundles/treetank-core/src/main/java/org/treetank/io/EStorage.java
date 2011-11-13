package org.treetank.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyFactory;
import org.treetank.io.berkeley.BerkeleyKey;
import org.treetank.io.file.FileFactory;
import org.treetank.io.file.FileKey;

/**
 * Utility methods for the storage. Those methods included common deletion
 * procedures as well as common checks. Furthermore, specific serialization are
 * summarized upon this enum.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum EStorage {

    File(1, FileKey.class) {

        @Override
        public IKey deserialize(final ITTSource pSource) {
            return new FileKey(pSource.readLong(), pSource.readLong());
        }

        @Override
        public void serialize(final ITTSink pSink, final IKey pKey) {
            serializeKey(pKey, mIdent, pSink);
        }
    },

    Berkeley(2, BerkeleyKey.class) {

        @Override
        public IKey deserialize(final ITTSource pSource) {
            return new BerkeleyKey(pSource.readLong());
        }

        @Override
        public void serialize(final ITTSink pSink, final IKey pKey) {
            serializeKey(pKey, mIdent, pSink);
        }

    };

    /** Getting identifier mapping. */
    private static final Map<Integer, EStorage> INSTANCEFORID = new HashMap<Integer, EStorage>();
    private static final Map<Class<? extends IKey>, EStorage> INSTANCEFORCLASS = new HashMap<Class<? extends IKey>, EStorage>();
    static {
        for (EStorage storage : values()) {
            INSTANCEFORID.put(storage.mIdent, storage);
            INSTANCEFORCLASS.put(storage.mClass, storage);
        }
    }

    /** Identifier for the storage. */
    final int mIdent;

    /** Class for Key. */
    final Class<? extends IKey> mClass;

    /**
     * Constructor.
     * 
     * @param paramIdent
     *            identifier to be set.
     */
    EStorage(final int paramIdent, final Class<? extends IKey> paramClass) {
        mIdent = paramIdent;
        mClass = paramClass;
    }

    /**
     * Deserialization of a key
     * 
     * @param paramSource
     *            where the key should be serialized from.
     * @return the {@link IKey} retrieved from the storage.
     */
    public abstract IKey deserialize(final ITTSource paramSource);

    /**
     * Serialization of a key
     * 
     * @param paramSink
     *            where the key should be serialized to
     * @param paramKey
     *            which should be serialized.
     */
    public abstract void serialize(final ITTSink paramSink, final IKey paramKey);

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    public static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    /**
     * Factory method to retrieve suitable {@link IStorage} instances based upon
     * the suitable {@link ResourceConfiguration}.
     * 
     * @param paramResourceConf
     *            determining the storage.
     * @return an implementation of the {@link IStorage} interface.
     * @throws TTIOException
     *             if anything happens.
     */
    public static final IStorage getStorage(
            final ResourceConfiguration paramResourceConf) throws TTIOException {
        IStorage fac = null;
        final EStorage storageType = paramResourceConf.mType;
        switch (storageType) {
        case File:
            fac = new FileFactory(paramResourceConf.mPath);
            break;
        case Berkeley:
            fac = new BerkeleyFactory(paramResourceConf.mPath);
            break;
        default:
            throw new TTIOException("Type", storageType.toString(),
                    "not valid!");
        }
        return fac;
    }

    /**
     * Getting an instance of this enum for the identifier.
     * 
     * @param paramId
     *            the identifier of the enum.
     * @return a concrete enum
     */
    public static final EStorage getInstance(final Integer paramId) {
        return INSTANCEFORID.get(paramId);
    }

    /**
     * Getting an instance of this enum for the identifier.
     * 
     * @param paramKey
     *            the identifier of the enum.
     * @return a concrete enum
     */
    public static final EStorage getInstance(
            final Class<? extends IKey> paramKey) {
        return INSTANCEFORCLASS.get(paramKey);
    }

    /**
     * Serializing the keys.
     * 
     * @param pKey
     *            to serialize
     * @param pId
     *            to serialize
     * @param pSink
     *            to be serialized to
     */
    private static void serializeKey(final IKey pKey, final int pId,
            final ITTSink pSink) {
        pSink.writeInt(pId);
        for (final long val : pKey.getKeys()) {
            pSink.writeLong(val);
        }
    }
}
