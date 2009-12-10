package com.treetank.settings;

/**
 * This enum stores all settable properties for treetank plus a standard value.
 * The real setting takes place in the class <code>SessionConfiguration</code>.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ESessionSetting {

    /** Number of concurrent exclusive write transactions. */
    MAX_WRITE_TRANSACTIONS("maximalWriteTransactions", 1),
    /** Number of concurrent shared read transactions. */
    MAX_READ_TRANSACTIONS("maximalReadTransactions", 128),
    /** Commit threshold. */
    COMMIT_THRESHOLD("commitThreshold", 262144);

    private final String mName;
    private final Object mStandardProperty;

    private ESessionSetting(final String name, final Object standardProperty) {
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
