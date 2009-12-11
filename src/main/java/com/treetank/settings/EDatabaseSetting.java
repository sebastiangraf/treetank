package com.treetank.settings;

import com.treetank.io.AbstractIOFactory.StorageType;

/**
 * Setting for a database. Once a database is existing, no other settings can be
 * chosen.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum EDatabaseSetting {

    /** Default storage */
    STORAGE_TYPE(StorageType.File.name()),

    /** Revision properties */
    REVISION_TYPE(ERevisioning.SLIDING_SNAPSHOT.name()),

    /** Window of Sliding Snapshot */
    MILESTONE_REVISION("4"),

    /** version major identifier for binary compatibility */
    VERSION_MAJOR("5"),

    /** version minor identifier for binary compatibility */
    VERSION_MINOR("2"),

    /** version fix identifier for binary compatibility */
    VERSION_FIX("0"),

    /** Checksum for settings */
    CHECKSUM("0");

    private final String mStandardProperty;

    private EDatabaseSetting(final String standardProperty) {
        this.mStandardProperty = standardProperty;
    }

    public String getStandardProperty() {
        return mStandardProperty;
    }

}
