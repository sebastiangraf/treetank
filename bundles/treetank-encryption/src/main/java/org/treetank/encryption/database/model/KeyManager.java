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
package org.treetank.encryption.database.model;

import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class represents the key manager model holding all key data
 * for a user comprising the initial keys and all TEKs a user owns.
 * 
 * @author Patrick Lang, University of Konstanz
 */
@Entity
public class KeyManager {

    /**
     * User and primary key for database.
     */
    @PrimaryKey
    private String mUser;

    /**
     * Set of keys the user owns.
     */
    private Set<Long> mKeySet;

    /**
     * Standard constructor.
     */
    public KeyManager() {
        super();
    }

    /**
     * Constructor for building an new key manager instance.
     * 
     * @param paramUser
     *            user.
     * @param paramKeys
     *            set of initial keys.
     */
    public KeyManager(final String paramUser, final Set<Long> paramKeys) {
        this.mUser = paramUser;
        this.mKeySet = paramKeys;
    }

    /**
     * Returns a user.
     * 
     * @return
     *         user.
     */
    public final String getUser() {
        return mUser;
    }

    /**
     * Returns a set of all keys a user owns.
     * 
     * @return
     *         set of users keys.
     */
    public final Set<Long> getKeySet() {
        return mKeySet;
    }

    /**
     * Adds a new key to the set.
     * 
     * @param paramTrail
     *            new key the user owns.
     */
    public final void addKey(final long paramKey) {
        mKeySet.add(paramKey);
    }

    /**
     * Removes a key from the set.
     * 
     * @param paramKey
     *            the key to be withdraw.
     */
    public final void removeKey(final long paramKey) {
        mKeySet.remove(paramKey);
    }

}
