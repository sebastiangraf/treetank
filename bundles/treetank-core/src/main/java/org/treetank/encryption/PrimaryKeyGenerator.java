package org.treetank.encryption;

/**
 * A singleton class to create several unique keys for encryption processes.
 */
public class PrimaryKeyGenerator {

    /**
     * Singleton instance.
     */
    private static PrimaryKeyGenerator mINSTANCE = new PrimaryKeyGenerator();

    /**
     * Selector key counter.
     */
    private int mSelectorKey = -1;

    /**
     * Material key counter.
     */
    private int mMaterialKey = -1;

    /**
     * Returns singleton instance.
     * 
     * @return
     *         singleton instance.
     */
    public static PrimaryKeyGenerator getInstance() {
        return mINSTANCE;
    }


    /**
     * Create new selector key by increasing current state by 1.
     * 
     * @return
     *         new unique selector key.
     */
    public final int newSelectorKey() {
        return ++mSelectorKey;
    }

    /**
     * Create new material key by increasing current state by 1.
     * 
     * @return
     *         new material right key.
     */
    public final int newMaterialKey() {
        return ++mMaterialKey;
    }

}
