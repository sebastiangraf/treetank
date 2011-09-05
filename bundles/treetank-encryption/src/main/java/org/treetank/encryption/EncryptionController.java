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
import java.util.LinkedList;

import org.treetank.encrpytion.exception.TTEncryptionException;
import org.treetank.encryption.cache.KeyCache;
import org.treetank.encryption.database.KeyManagerDatabase;
import org.treetank.encryption.database.KeySelectorDatabase;
import org.treetank.encryption.utils.EncryptionDAGParser;

/**
 * This central singleton class holding and handling data and instances for
 * encryption operations. It initiates all important components like databases
 * or cache. It is like a controlling class from which all other classes get
 * their database instances or session information.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public final class EncryptionController implements IEncryption {

    // #################SETTINGS START#######################

    /**
     * Instance for enabling or disabling encryption process.
     */
    private final static boolean mNodeEncryption = false;

    /**
     * The key data should be encrypted.
     */
    private long mDataEncryptionKey = 0;

    /**
     * Current session user.
     */
    private static String mLoggedUser = "ALL";

    // #################SETTINGS END#######################

    /**
     * Singleton instance.
     */
    private static EncryptionController mINSTANCE = new EncryptionController();

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
    // private static ISession mSession;

    /**
     * Selector key counter.
     */
    private int mSelectorKey = -1;

    /**
     * Path of initial right tree XML file.
     */
    private static final String FILENAME = "src" + File.separator + "main"
        + File.separator + "resources" + File.separator
        + "righttreestructure.xml";

    /**
     * Store path of berkeley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkeley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

    /**
     * Standard constructor.
     */
    private EncryptionController() {
    }

    /**
     * Returns singleton instance of handler.
     * 
     * @return Handler instance.
     */
    public static EncryptionController getInstance() {
        return mINSTANCE;
    }

    /**
     * Initiates all needed instances comprising Berkeley DBs and key cache.
     * Additionally it initiates parsing of initial right tree and setup of
     * Berkeley DBs.
     * 
     * @throws TTEncryptionException
     */
    public void init(final String mUser) throws TTEncryptionException {
        if (mNodeEncryption) {
            mLoggedUser = mUser;
            mKeySelectorDb = new KeySelectorDatabase(SEL_STORE);
            mKeyManagerDb = new KeyManagerDatabase(MAN_STORE);
            mKeyCache = new KeyCache();
            new EncryptionDAGParser().init(FILENAME, mKeySelectorDb,
                mKeyManagerDb, mKeyCache, mLoggedUser);
        } else {
            throw new TTEncryptionException("Encryption is disabled!");
        }
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
        if (MAN_STORE.exists()) {
            recursiveDelete(SEL_STORE);
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
     * Closes all databases.
     */
    public void close() {
        mKeySelectorDb.clearPersistent();
        mKeyManagerDb.clearPersistent();
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
     * Returns session user.
     * 
     * @return current logged user.
     */
    public String getUser() {
        // return mSession.getUser();
        return mLoggedUser;
    }

    /**
     * Returns cache list of current logged user.
     * 
     * @return cache list of user.
     */
    public LinkedList<Long> getKeyCache() {
        return mKeyCache.get(getUser());
    }

    /**
     * Create new selector key by increasing current state by 1.
     * 
     * @return new unique selector key.
     */
    public final int newSelectorKey() {
        return ++mSelectorKey;
    }

    /**
     * Returns data encryption key.
     * 
     * @return data encryption key.
     */
    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

    /**
     * Returns key selector database instance.
     * 
     * @return KeySelector instance.
     */
    public KeySelectorDatabase getKeySelectorInstance() {
        return mKeySelectorDb;
    }

    /**
     * Returns key manager database instance.
     * 
     * @return KeyManager instance.
     */
    public KeyManagerDatabase getKeyManagerInstance() {
        return mKeyManagerDb;
    }

    /**
     * Returns key cache instance.
     * 
     * @return KeyCache instance.
     */
    public KeyCache getKeyCacheInstance() {
        return mKeyCache;
    }

    /**
     * Returns key manager handler instance.
     * 
     * @return
     */
    public KeyManagerHandler getKMHInstance() {
        return new KeyManagerHandler();
    }

    /**
     * Returns key cac
     * 
     * @return
     */
    public ClientHandler getCHInstance() {
        return new ClientHandler();
    }

}
