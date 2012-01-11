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
package org.treetank.encryption;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.encryption.cache.KeyCache;
import org.treetank.encryption.database.DAGDatabase;
import org.treetank.encryption.database.KeyManagerDatabase;
import org.treetank.encryption.database.KeyDatabase;
import org.treetank.encryption.database.model.DAGSelector;
import org.treetank.encryption.database.model.KeyManager;
import org.treetank.encryption.database.model.KeySelector;
import org.treetank.encryption.utils.NodeEncryption;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTEncryptionException;

/**
 * Class holding encryption control parameters and methods.
 */
public class EncryptionController implements IEncryption {

    // #################SETTINGS START#######################

    /**
     * Instance for enabling or disabling encryption process.
     */
    private static boolean mNodeEncryption = false;

    /**
     * The key data should be encrypted.
     */
    private long mDataEncryptionKey = 0;

    /**
     * Current session user.
     */
    private static String mLoggedUser;

    // #################SETTINGS END#######################

    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeyDatabase mKeyDB;

    /**
     * Instance of CurrentDAGDatabase holding last DAG revision stuff.
     */
    private static DAGDatabase mDAGDB;

    /**
     * Instance of KeyManagerDatabase holding key manager stuff.
     */
    private static KeyManagerDatabase mManDB;

    /**
     * Instance of KeyCache holding all current keys of user.
     */
    private static KeyCache mKeyCache;

    /**
     * Singleton instance.
     */
    private static EncryptionController mINSTANCE = new EncryptionController();

    /**
     * Returns singleton instance of handler.
     * 
     * @return Handler instance.
     */
    public static EncryptionController getInstance() {
        return mINSTANCE;
    }

    /**
     * Global key selector database key.
     */
    private int mSelectorKey = 0;
    /**
     * Global dag selector database key.
     */
    private int mDAGKey = 0;

    /**
     * Store path of berkeley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkeley dag selector db.
     */
    private static final File DAG_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("dagdb").toString());

    /**
     * Store path of berkeley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

    /**
     * Initiates all needed instances comprising Berkeley DBs and key cache.
     * Additionally it initiates parsing of initial right tree and setup of
     * Berkeley DBs.
     * 
     * @throws TTEncryptionException
     */
    public void init() throws TTEncryptionException {
        if (mNodeEncryption) {
            mLoggedUser = "ALL";
            mKeyDB = new KeyDatabase(SEL_STORE);
            final KeySelector rootSel =
                new KeySelector("ROOT", new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
            mKeyDB.putEntry(rootSel);

            mDAGDB = new DAGDatabase(DAG_STORE);
            final DAGSelector rootDAG =
                new DAGSelector("ROOT", new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
            mDAGDB.putEntry(rootDAG);

            mManDB = new KeyManagerDatabase(MAN_STORE);
            final Set<Long> manSet = new HashSet<Long>();
            manSet.add(rootSel.getPrimaryKey());
            mManDB.putEntry(new KeyManager(mLoggedUser, manSet));

            mKeyCache = new KeyCache();
            final LinkedList<Long> keyList = new LinkedList<Long>();
            keyList.add(rootSel.getPrimaryKey());
            mKeyCache.put(mLoggedUser, keyList);

        } else {
            throw new TTEncryptionException("Encryption is disabled!");
        }

    }

    /**
     * Creates a new key selector key.
     * 
     * @return
     */
    public int newSelectorKey() {
        return mSelectorKey++;
    }

    /**
     * Creates a new dag selector key.
     * 
     * @return
     */
    public int newDAGKey() {
        return mDAGKey++;
    }

    public void print() {

        final SortedMap<Long, KeySelector> mSelMap = mKeyDB.getEntries();
        Iterator<Long> iter = mSelMap.keySet().iterator();

        System.out.println("\nSelector DB Size: " + mKeyDB.count());

        while (iter.hasNext()) {

            final KeySelector mSelector = mSelMap.get(iter.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();

            System.out.println("Selector: " + mSelector.getPrimaryKey() + " "
                + mSelector.getName() + " " + mParentsList.toString() + " "
                + mChildsList.toString() + " " + mSelector.getRevision() + " "
                + mSelector.getVersion() + " " + mSelector.getSecretKey());
        }
        System.out.println(" ");

        final SortedMap<Long, DAGSelector> mDAGMap = mDAGDB.getEntries();
        Iterator<Long> iter2 = mDAGMap.keySet().iterator();

        System.out.println("\nDAG DB Size: " + mDAGDB.count());

        while (iter2.hasNext()) {

            final DAGSelector mSelector = mDAGMap.get(iter2.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();

            System.out.println("Selector: " + mSelector.getPrimaryKey() + " "
                + mSelector.getName() + " " + mParentsList.toString() + " "
                + mChildsList.toString() + " " + mSelector.getRevision() + " "
                + mSelector.getVersion() + " " + mSelector.getSecretKey() + " "
                + mSelector.getLastRevSelKey());
        }
        System.out.println(" ");

        /*
         * print key manager db
         */
        final SortedMap<String, KeyManager> sMap = mManDB.getEntries();

        // iterate through all users
        final Iterator<String> outerIter = sMap.keySet().iterator();

        System.out.println("Key manager DB Size: " + mManDB.count());

        StringBuilder sb;
        while (outerIter.hasNext()) {
            final String user = (String)outerIter.next();
            sb = new StringBuilder(user + ": ");

            final Set<Long> mKeySet = mManDB.getEntry(user).getKeySet();

            // iterate through user's key set.
            final Iterator<Long> innerIter = mKeySet.iterator();
            while (innerIter.hasNext()) {
                sb.append(innerIter.next() + " ");
            }

            System.out.println(sb.toString());

        }
        System.out.println(" ");

        /*
         * print key cache.
         */
        final LinkedList<Long> mKeyList = mKeyCache.get(getUser());
        System.out.println("Key Cache of " + getUser() + ": ");
        System.out.println(mKeyList.toString());

    }

    /**
     * Returns key selector database instance.
     * 
     * @return KeySelector instance.
     */
    public KeyDatabase getSelDb() {
        return mKeyDB;
    }

    /**
     * Returns dag selector database instance.
     * 
     * @return CurrentDAGSelector instance.
     */
    public DAGDatabase getDAGDb() {
        return mDAGDB;
    }

    /**
     * Returns key manager database instance.
     * 
     * @return KeyManager instance.
     */
    public KeyManagerDatabase getManDb() {
        return mManDB;
    }

    /**
     * Returns key cache instance.
     * 
     * @return KeyCache instance.
     */
    public KeyCache getKeyCache() {
        return mKeyCache;
    }

    /**
     * Clears all established berkeley dbs.
     * 
     * @throws AbsTTException
     */
    public void clear() throws TTEncryptionException {
        if (SEL_STORE.exists()) {
            recursiveDelete(SEL_STORE);
        }
        if (DAG_STORE.exists()) {
            recursiveDelete(DAG_STORE);
        }
        if (MAN_STORE.exists()) {
            recursiveDelete(MAN_STORE);
        }

    }

    /**
     * Deletes berkeley db file recursively.
     * 
     * @param paramFile
     *            File to delete.
     * @return if some more files available.
     */
    protected static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    /**
     * Returns whether encryption is enabled or not.
     * 
     * @return encryption enabled.
     */
    public boolean checkEncryption() {
        return mNodeEncryption;
    }

    /**
     * Enables or disables encryption option.
     * 
     * @param paramBol
     *            if encryption should be enabled or not.
     */
    public void setEncryptionOption(final boolean paramBol) {
        mNodeEncryption = paramBol;
    }

    /**
     * Set session user.
     */
    public void setUser(final String paramUser) {
        mLoggedUser = paramUser;
    }

    /**
     * Set dek.
     */
    public void setDek(final long paramDek) {
        mDataEncryptionKey = paramDek;
    }

    /**
     * Returns session user.
     * 
     * @return current logged user.
     */
    public String getUser() {
        return mLoggedUser;
    }

    /**
     * Returns data encryption key.
     * 
     * @return data encryption key.
     */
    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

}
