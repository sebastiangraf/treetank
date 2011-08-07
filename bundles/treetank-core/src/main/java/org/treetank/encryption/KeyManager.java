package org.treetank.encryption;

import java.util.HashSet;
import java.util.List;
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
