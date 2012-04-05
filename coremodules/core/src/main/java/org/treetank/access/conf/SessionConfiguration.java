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

import org.treetank.access.Database;
import org.treetank.access.Session;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the {@link Session}-wide settings that can not change within the
 * runtime of a {@link Session}. This included stuff like commit-threshold and
 * number of usable write/read transactions. Each {@link SessionConfiguration}
 * is only bound through the location to a {@link Database} and related
 * resources.
 * </p>
 */
public final class SessionConfiguration {

    /** Default User. */
    public static final String DEFAULT_USER = "ALL";
    // END STATIC STANDARD FIELDS

    /** User for this session. */
    public final String mUser;
    // END MEMBERS FOR FIXED FIELDS

    /** ResourceConfiguration for this ResourceConfig. */
    private final String mResource;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param pBuilder
     *            {@link Builder} reference
     */
    private SessionConfiguration(final SessionConfiguration.Builder pBuilder) {
        mUser = pBuilder.mUser;
        mResource = pBuilder.mResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 90599;
        int result = 13;
        result = prime * result + mUser.hashCode();
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
        builder.append(this.mUser);
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
     * Builder class for generating new {@link SessionConfiguration} instance.
     */
    public static final class Builder {

        /** User for this session. */
        private String mUser = SessionConfiguration.DEFAULT_USER;

        /** Resource for the this session. */
        private String mResource;

        /**
         * Constructor for the {@link Builder} with fixed fields to be set.
         * 
         * @param pRes
         *            to be set.
         */
        public Builder(final String pRes) {
            if (pRes == null) {
                throw new IllegalArgumentException(
                        "Parameter must not be null!");
            }
            this.mResource = pRes;
        }

        /**
         * Setter for field mUser.
         * 
         * @param pUser
         *            new value for field
         * @return reference to the builder object
         */
        public Builder setUser(final String pUser) {
            if (pUser == null) {
                throw new NullPointerException("paramUser may not be null!");
            }
            mUser = pUser;
            return this;
        }

        /**
         * Building a new {@link SessionConfiguration} with immutable fields.
         * 
         * @return a new {@link SessionConfiguration}.
         */
        public SessionConfiguration build() {
            return new SessionConfiguration(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("User: ");
            builder.append(this.mUser);
            return builder.toString();
        }

    }

}
