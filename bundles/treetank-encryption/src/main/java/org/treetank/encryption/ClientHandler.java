package org.treetank.encryption;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.treetank.encryption.cache.KeyCache;
import org.treetank.encryption.database.KeySelectorDatabase;
import org.treetank.encryption.utils.NodeEncryption;

public class ClientHandler {
    
    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeySelectorDatabase mKeySelectorDb;
    /**
     * Instance of KeyCache holding all current keys of user.
     */
    private static KeyCache mKeyCache;
    
    /**
     * Instance of singleton class EncryptionHandler.
     */
    private static EncryptionController mEnHandler;
    
    /**
     * Standard constructor.
     */
    public ClientHandler(){
        mEnHandler = EncryptionController.getInstance();
        mKeySelectorDb = mEnHandler.getKeySelectorInstance();
        mKeyCache = mEnHandler.getKeyCacheInstance();
    }

    /**
     * Decrypts key trails and put it to users key cache to make it available to encrypt/decrypt data.
     * 
     * @param paramKeyTails
     *            map of key trails.
     */
    public void decryptKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        // if map contains no key trails user has been completely removed
        // from DAG and all keys for user has to be removed.
        if (paramKeyTails.size() != 0) {
            final Iterator mIter = paramKeyTails.keySet().iterator();
            while (mIter.hasNext()) {
                long mapKey = (Long)mIter.next();

                byte[] mChildSecretKey =
                    mKeySelectorDb.getEntry(mapKey).getSecretKey();
                byte[] mDecryptedBytes =
                    NodeEncryption.decrypt(paramKeyTails.get(mapKey),
                        mChildSecretKey);

                long mEncryptedKey =
                    NodeEncryption.byteArrayToLong(mDecryptedBytes);

                System.out.println("encrypted key: " + mEncryptedKey);

                final LinkedList<Long> mUserCache = mEnHandler.getKeyCache();

                if (!mUserCache.contains(mEncryptedKey)) {
                    mUserCache.add(mEncryptedKey);
                }
                mKeyCache.put(mEnHandler.getUser(), mUserCache);
            }
        } else {
            mKeyCache.put(mEnHandler.getUser(), new LinkedList<Long>());
        }
    }


}
