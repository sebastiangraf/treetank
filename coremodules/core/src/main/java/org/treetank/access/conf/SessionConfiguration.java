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

import static com.google.common.base.Objects.toStringHelper;

import java.security.Key;

import org.treetank.access.Session;
import org.treetank.access.Storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the {@link Session}-wide settings that can not change within the runtime of a {@link Session}. This
 * included stuff like commit-threshold and number of usable write/read transactions. Each
 * {@link SessionConfiguration} is only bound through the location to a {@link Storage} and related resources.
 * </p>
 */
@Singleton
public final class SessionConfiguration {

    /** ResourceConfiguration for this ResourceConfig. */
    private final String mResource;

    /** Key for accessing any encrypted data. */
    private final Key mKey;

    /** SINGLETON instance for this configuration. */
    private static SessionConfiguration SINGLETON;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param pResource
     *            resource to be accessed
     * @param pKey
     *            key for accessing encrypted data
     */
    @Inject
    public SessionConfiguration(@Assisted String pResource, Key pKey) {
        mResource = pResource;
        mKey = pKey;
        SINGLETON = this;
    }

    /**
     * Singleton to get easy the key from the Encryptor.
     * 
     * @return the Singleton-instance.
     */
    public static SessionConfiguration getInstance() {
        return SINGLETON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mResource", mResource).add("mKey", mKey).toString();
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

    /**
     * 
     * Factory for generating an {@link SessionConfiguration}-instance. Needed mainly
     * because of Guice-Assisted utilization.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static interface ISessionConfigurationFactory {

        /**
         * Generating a storage for a fixed file.
         * 
         * @param pResourceName
         *            Name of resource to be set.
         * @return an {@link SessionConfiguration}-instance
         */
        SessionConfiguration create(String pResourceName);
    }
}
