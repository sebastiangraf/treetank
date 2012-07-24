/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.access.conf;

import java.security.Key;

import org.treetank.access.Database;
import org.treetank.access.Session;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the {@link Session}-wide settings that can not change within the runtime of a {@link Session}. This
 * included stuff like commit-threshold and number of usable write/read transactions. Each
 * {@link SessionConfiguration} is only bound through the location to a {@link Database} and related
 * resources.
 * </p>
 */
public final class SessionConfiguration {

    /** ResourceConfiguration for this ResourceConfig. */
    private final String mResource;

    /** Key for accessing any encrypted data. */
    private final Key mKey;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param pResource
     *            resource to be accessed
     * @param pKey
     *            key for accessing encrypted data
     */
    private SessionConfiguration(String pResource, Key pKey) {
        mResource = pResource;
        mKey = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 90599;
        int result = 13;
        result = prime * result + mResource.hashCode();
        result = prime * result + mKey.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object mObj) {
        return this.hashCode() == mObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("User: ");
        builder.append(this.mResource);
        builder.append("Key: ");
        builder.append(this.mKey);
        return builder.toString();
    }

    /**
     * Getter for the file.
     * 
     * @return the file for the configuration.
     */
    public String getResource() {
        return mResource;
    }

    /**
     * Getter for the key material
     * 
     * @return the key within this session
     */
    public Key getKey() {
        return mKey;
    }

}
