/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.settings;

import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.io.AbsIOFactory.StorageType;

/**
 * Setting for a database. Once a database is existing, no other settings can be
 * chosen.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum EDatabaseSetting {

    /** Default storage. */
    STORAGE_TYPE(StorageType.Berkeley.name()),

    /** Revision properties. */
    REVISION_TYPE(ERevisioning.DIFFERENTIAL.name()),

    /** Window of Sliding Snapshot. */
    REVISION_TO_RESTORE("4"),

    /** version major identifier for binary compatibility. */
    VERSION_MAJOR("5"),

    /** version minor identifier for binary compatibility. */
    VERSION_MINOR("2"),

    /** version fix identifier for binary compatibility. */
    VERSION_FIX("0"),

    /** Checksum for checking the integrity of serialized settings. */
    CHECKSUM("0"),

    /** Kind of hashing of the treestructure. */
    HASHKIND_TYPE(HashKind.Rolling.name());

    /** Member for holding the standardpropertey. */
    private final String mStandardProperty;

    /**
     * Simple constructor.
     * 
     * @param paramStandardProperty
     *            the standardproperty to set
     */
    private EDatabaseSetting(final String paramStandardProperty) {
        this.mStandardProperty = paramStandardProperty;
    }

    /**
     * Getting the property.
     * 
     * @return the standard property
     */
    public String getStandardProperty() {
        return mStandardProperty;
    }

}
