package com.treetank.constants;

public enum EFixed {

    // --- File Version
    // ----------------------------------------------------------------
    /** Major version number of this release. */
    VERSION_MAJOR("versionMajor", 5),
    /** Minor version number of this release. */
    VERSION_MINOR("versionMinor", 1),
    /** Last major version to which this version is binary compatible. */
    LAST_VERSION_MAJOR("lastVersionMajor", 5),
    /** Last minor version to which this version is binary compatible. */
    LAST_VERSION_MINOR("lastVersionMinor", 1),

    // --- Keys
    // -------------------------------------------------------------
    /** Root node page key constant. */
    ROOT_PAGE_KEY("rootPageKey", 0l),
    /** Root node page key constant. */
    ROOT_NODE_KEY("rootNodeKey", 0l),
    /** Null key for nodes. */
    NULL_NODE_KEY("nullKey", -1l),
    /** Null key for nodes. */
    NULL_INT_KEY("nullKey", -1);

    private final String mName;
    private final Object mStandardProperty;

    private EFixed(final String name, final Object standardProperty) {
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
