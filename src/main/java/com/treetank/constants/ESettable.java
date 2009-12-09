package com.treetank.constants;

import com.treetank.io.AbstractIOFactory.StorageType;

/**
 * This enum stores all settable properties for treetank plus a standard value.
 * The real setting takes place in the class <code>SessionConfiguration</code>.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ESettable {

    /** Default storage */
    STORAGE_TYPE("storageType", StorageType.File),

    /** Revision properties */
    REVISION_TYPE("revisionType", ERevisioning.SLIDING_SNAPSHOT),

    /** Number of concurrent exclusive write transactions. */
    MAX_WRITE_TRANSACTIONS("maximalWriteTransactions", 1),
    /** Number of concurrent shared read transactions. */
    MAX_READ_TRANSACTIONS("maximalReadTransactions", 128),

    // --- Revision stuff
    // -------------------------------------------------------------
    /** Commit threshold. */
    COMMIT_THRESHOLD("commitThreshold", 262144),
    /** Window of Sliding Snapshot */
    SNAPSHOT_WINDOW("snapshotWindow", 4);

    private final String mName;
    private final Object mStandardProperty;

    private ESettable(final String name, final Object standardProperty) {
        this.mName = name;
        this.mStandardProperty = standardProperty;
    }

    public String getName() {
        return mName;
    }

    public Object getStandardProperty() {
        return mStandardProperty;
    }

}
