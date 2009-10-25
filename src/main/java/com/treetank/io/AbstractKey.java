package com.treetank.io;

/**
 * Abstract class to provide a key corresponding to the storage. A Key is the
 * link to the persistent representation in the physical database e.g. the
 * offset in a file or the key in a relational mapping.
 * 
 * More than one keys are possible if necessary e.g. related to the
 * file-layer-implementation: the offset plus the length. Only one key must be
 * unique.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractKey {

    /** all keys */
    private final transient long[] keys;

    /**
     * Protected constructor, just setting the keys.
     * 
     * @param paramKeys
     *            setting the keys.
     */
    protected AbstractKey(final long... paramKeys) {
        keys = paramKeys;
    }

    /**
     * Getting all keys
     * 
     * @return the keys
     */
    protected long[] getKeys() {
        final long[] returnKeys = new long[keys.length];
        System.arraycopy(keys, 0, returnKeys, 0, keys.length);
        return returnKeys;
    }

    /**
     * Getting the primary one.
     * 
     * @return the key which is profmaly
     */
    public abstract long getIdentifier();
}
