/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
@Deprecated
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
