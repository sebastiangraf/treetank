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
    MAX_WRITE_TRANSACTIONS("1"),
    /** Number of concurrent shared read transactions. */
    MAX_READ_TRANSACTIONS("128"),
    /** Commit threshold. */
    COMMIT_THRESHOLD("262144");

    private final String mValue;

    private ESessionSetting(final String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

}
