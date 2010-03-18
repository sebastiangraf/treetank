package com.treetank.settings;

public enum EFixed {

    // --- File Version
    // ----------------------------------------------------------------
    /** Major version number of this release. */
    VERSION_MAJOR(5),
    /** Minor version number of this release. */
    VERSION_MINOR(1),
    /** Last major version to which this version is binary compatible. */
    LAST_VERSION_MAJOR(5),
    /** Last minor version to which this version is binary compatible. */
    LAST_VERSION_MINOR(1),

    // --- Keys
    // -------------------------------------------------------------
    /** Root node page key constant. */
    ROOT_PAGE_KEY(0l),
    /** Root node page key constant. */
    ROOT_NODE_KEY(0l),
    /** Null key for nodes. */
    NULL_NODE_KEY(-1l),
    /** Null key for nodes. */
    NULL_INT_KEY(-1);

    private final Object mStandardProperty;

    private EFixed(final Object standardProperty) {
        this.mStandardProperty = standardProperty;
    }

    public Object getStandardProperty() {
        return mStandardProperty;
    }
}
