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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.access.Database;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.api.ISession;
import org.treetank.cache.KeyCache;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTEncryptionException;
import org.treetank.exception.TTIOException;

/**
 * Singleton class holding and handling data for encryption.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public final class EncryptionHandler {

    // #################START SETTINGS#####################

    /**
     * Instance for enabling or disabling encryption process.
     */
    private final static boolean mNodeEncryption = false;

    // #################END SETTINGS#######################

    /**
     * Singleton instance.
     */
    private static EncryptionHandler mINSTANCE;

    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeySelectorDatabase mKeySelectorDb;

    /**
     * Instance of KeyManagerDatabase holding key manager stuff.
     */
    private static KeyManagerDatabase mKeyManagerDb;

    /**
     * Instance of KeyCache holding all current keys of user.
     */
    private static KeyCache mKeyCache;

    /**
     * Instance of Session.
     */
    private static ISession mSession;

    /**
     * Current session user.
     */
    private static String mLoggedUser = "U2";

    /**
     * The key data should be encrypted.
     */
    private long mDataEncryptionKey;

    /**
     * Store path of berkeley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkeley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("keymanagerdb").toString());

    /**
     * Standard constructor.
     */
    private EncryptionHandler() {

    }

    /**
     * Returns singleton instance of handler.
     * 
     * @return
     *         Handler instance.
     */
    public static synchronized EncryptionHandler getInstance() {
        if (mINSTANCE == null) {
            mINSTANCE = new EncryptionHandler();
        }
        return mINSTANCE;
    }

    /**
     * Initiates all needed instances comprising Berkeley DBs and key cache.
     * Additionally it initiates parsing of initial right tree and
     * setup of Berkeley DBs.
     * 
     * @throws TTEncryptionException
     */
    public void init(final ISession paramSession, final long paramDEK) throws TTEncryptionException {
        if (mNodeEncryption) {
            mKeySelectorDb = new KeySelectorDatabase(SEL_STORE);
            mKeyManagerDb = new KeyManagerDatabase(MAN_STORE);
            mSession = paramSession;
            mDataEncryptionKey = paramDEK;
            mKeyCache = new KeyCache();
            new EncryptionTreeParser().init();
        } else {
            throw new TTEncryptionException("Encryption is disabled!");
        }
    }

    /**
     * Clears all established berkeley dbs.
     * 
     * @throws AbsTTException
     */
    public void clear() throws AbsTTException {
        try {
            if (SEL_STORE.exists()) {
                Database.truncateDatabase(new DatabaseConfiguration(SEL_STORE));
            }
            if (MAN_STORE.exists()) {
                Database.truncateDatabase(new DatabaseConfiguration(MAN_STORE));
            }
        } catch (final TTIOException ttee) {
            ttee.printStackTrace();
        }
    }

    /**
     * Prints all stored information of KeySelector, KeyingMaterial
     * and KeyManager database. This method is just for testing issues.
     */
    public void print() {
        if (mNodeEncryption) {

            /*
             * print key selector db.
             */
            final SortedMap<Long, KeySelector> mSelMap = mKeySelectorDb.getEntries();
            Iterator iter = mSelMap.keySet().iterator();

            System.out.println("\nSelector DB Size: " + mKeySelectorDb.count());

            while (iter.hasNext()) {

                final KeySelector mSelector = mSelMap.get(iter.next());
                final LinkedList<Long> mParentsList = mSelector.getParents();
                final List<Long> mChildsList = mSelector.getChilds();

                final StringBuilder mParentsString = new StringBuilder();
                for (int k = 0; k < mParentsList.size(); k++) {
                    mParentsString.append("#" + mParentsList.get(k));
                }

                final StringBuilder mChildsString = new StringBuilder();
                for (int k = 0; k < mChildsList.size(); k++) {
                    mChildsString.append("#" + mChildsList.get(k));
                }

                System.out.println("Selector: " + mSelector.getPrimaryKey() + " " + mSelector.getName() + " "
                    + mSelector.getType() + " " + mParentsString.toString() + " " + mChildsString.toString()
                    + " " + mSelector.getRevision() + " " + mSelector.getVersion() + " "
                    + mSelector.getSecretKey());
            }
            System.out.println();

            /*
             * print key manager db
             */
            final SortedMap<String, KeyManager> sMap = mKeyManagerDb.getEntries();

            // iterate through all users
            final Iterator outerIter = sMap.keySet().iterator();

            System.out.println("Key manager DB Size: " + mKeyManagerDb.count());

            StringBuilder sb;
            while (outerIter.hasNext()) {
                final String user = (String)outerIter.next();
                sb = new StringBuilder(user + ": ");

                final Set<Long> mKeySet = mKeyManagerDb.getEntry(user).getKeySet();

                // iterate through user's key set.
                final Iterator innerIter = mKeySet.iterator();
                while (innerIter.hasNext()) {
                    sb.append(innerIter.next() + " ");
                }

                System.out.println(sb.toString());
            }
            System.out.println();

            /*
             * print key cache.
             */
            final LinkedList<Long> mKeyList = mKeyCache.get(getUser());
            final StringBuilder cacheString = new StringBuilder(getUser() + ": ");
            for (long aKey : mKeyList) {
                cacheString.append(aKey + " ");
            }
            System.out.println(cacheString);
        }

    }

    /**
     * Returns whether encryption is enabled or not.
     * 
     * @return
     *         encryption enabled.
     */
    public boolean checkEncryption() {
        return mNodeEncryption;
    }

    /**
     * Returns session user.
     * 
     * @return
     *         current logged user.
     */
    public String getUser() {
        // return mSession.getUser();
        return mLoggedUser;
    }

    /**
     * Returns cache list of current logged user.
     * 
     * @return
     *         cache list of user.
     */
    public List<Long> getKeyCache() {
        return mKeyCache.get(getUser());
    }

    /**
     * Returns data encryption key.
     * 
     * @return
     *         data encryption key.
     */
    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

    /**
     * Returns key selector database instance.
     * 
     * @return
     *         KeySelector instance.
     */
    public KeySelectorDatabase getKeySelectorInstance() {
        return mKeySelectorDb;
    }

    /**
     * Returns key manager database instance.
     * 
     * @return
     *         KeyManager instance.
     */
    public KeyManagerDatabase getKeyManagerInstance() {
        return mKeyManagerDb;
    }

    /**
     * Returns key cache instance.
     * 
     * @return
     *         KeyCache instance.
     */
    public KeyCache getKeyCacheInstance() {
        return mKeyCache;
    }

}
