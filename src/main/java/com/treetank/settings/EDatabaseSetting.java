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
    STORAGE_TYPE("storageType", StorageType.File),

    /** Revision properties */
    REVISION_TYPE("revisionType", ERevisioning.SLIDING_SNAPSHOT),

    /** Window of Sliding Snapshot */
    MILESTONE_REVISION("milestoneRevision", 4);

    private final String mName;
    private final Object mStandardProperty;

    private EDatabaseSetting(final String name, final Object standardProperty) {
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
