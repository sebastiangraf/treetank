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

package org.treetank.access;

import org.treetank.exception.TTUsageException;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the session-wide settings that can not change. This included stuff like commit-threshold and number
 * of usable write/read transactions
 * 
 */
public final class SessionConfiguration {

    // STATIC STANDARD FIELDS
    /** Number of concurrent exclusive write transactions. */
    public final static int MAX_WRITE_TRANSACTIONS = 1;
    /** Number of concurrent shared read transactions. */
    public final static int MAX_READ_TRANSACTIONS = 128;
    /** Commit threshold. */
    public final static int COMMIT_THRESHOLD = 262144;
    /** Default User. */
    public final static String DEFAULT_USER = "ALL";
    /** Folder for tmp-database. */
    public final static String INTRINSICTEMP = "tmp";
    // END STATIC STANDARD FIELDS

    /** Numbers of allowed IWriteTransaction Instances. */
    public final int mWtxAllowed;

    /** Numbers of allowed IWriteTransaction Instances. */
    public final int mRtxAllowed;

    /** Number of node modifications until an automatic commit occurs. */
    public final int mCommitThreshold;

    /** User for this session. */
    public final String mUser;

    /** Name for the resource to be associated. */
    public final String mName;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @throws TTUsageException
     *             if session is not valid
     */
    private SessionConfiguration(final SessionConfiguration.Builder paramBuilder, final String paramName) {
        this.mWtxAllowed = paramBuilder.mWtxAllowed;
        this.mRtxAllowed = paramBuilder.mRtxAllowed;
        this.mCommitThreshold = paramBuilder.mCommitThreshold;
        this.mUser = paramBuilder.mUser;
        this.mName = paramName;
    }

    /**
     * Builder class for generating new SessionConfiguration.
     */
    public static class Builder {

        /** Numbers of allowed IWriteTransaction Instances. */
        private int mWtxAllowed = SessionConfiguration.MAX_READ_TRANSACTIONS;

        /** Numbers of allowed IWriteTransaction Instances. */
        private int mRtxAllowed = SessionConfiguration.MAX_READ_TRANSACTIONS;

        /** Number of node modifications until an automatic commit occurs. */
        private int mCommitThreshold = SessionConfiguration.COMMIT_THRESHOLD;

        /** User for this session. */
        private String mUser = SessionConfiguration.DEFAULT_USER;

        /**
         * Setter for field mWtxAllowed
         * 
         * @param mWtxAllowed
         *            new value for field
         */
        public void setWtxAllowed(int mWtxAllowed) {
            this.mWtxAllowed = mWtxAllowed;
        }

        /**
         * Setter for field mRtxAllowed
         * 
         * @param mRtxAllowed
         *            new value for field
         */
        public void setRtxAllowed(int mRtxAllowed) {
            this.mRtxAllowed = mRtxAllowed;
        }

        /**
         * Setter for field mCommitThreshold
         * 
         * @param mCommitThreshold
         *            new value for field
         */
        public void setCommitThreshold(int mCommitThreshold) {
            this.mCommitThreshold = mCommitThreshold;
        }

        /**
         * Setter for field mUser
         * 
         * @param mUser
         *            new value for field
         */
        public void setUser(String mUser) {
            this.mUser = mUser;
        }

        /**
         * Builder method to generate new configuration
         * 
         * @return a new {@link SessionConfiguration} instance
         */
        public SessionConfiguration build() {
            return new SessionConfiguration(this, INTRINSICTEMP);
        }

        /**
         * Builder method to generate new configuration
         * 
         * @param resourceName
         *            name of the resource where the data should be persisted to
         * @return a new {@link SessionConfiguration} instance
         */
        public SessionConfiguration build(final String resourceName) {
            return new SessionConfiguration(this, resourceName);
        }
    }

}
