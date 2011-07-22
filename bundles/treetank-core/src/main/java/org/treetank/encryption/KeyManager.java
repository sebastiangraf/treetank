package org.treetank.encryption;

import java.util.LinkedList;
import java.util.List;

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
     * Initial trace of keys the user gets for entry a group.
     */
    private List<Long> mInitialKeys;

    /**
     * List of all TEKs the user owns.
     */
    private List<Long> mTekKeys;

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
     * @param paramInitial
     *            initial keys.
     * @param paramFirstTek
     *            first tek.
     */
    public KeyManager(final String paramUser, final List<Long> paramInitial,
        final long paramFirstTek) {
        this.mUser = paramUser;
        this.mInitialKeys = paramInitial;
        this.mTekKeys = new LinkedList<Long>();
        mTekKeys.add(paramFirstTek);
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
     * Returns a list of initial keys.
     * 
     * @return
     *         list of initial keys.
     */
    public final List<Long> getInitialKeys() {
        return mInitialKeys;
    }

    /**
     * Returns a list of TEKs the user owns.
     * 
     * @return
     *         TEK list.
     */
    public final List<Long> getTEKs() {
        return mTekKeys;
    }

    /**
     * Adds a new TEK to users TEK list.
     * 
     * @param paramTek
     *            new TEK to add.
     */
    public final void addTEK(final long paramTek) {
        mTekKeys.add(paramTek);
    }

}
