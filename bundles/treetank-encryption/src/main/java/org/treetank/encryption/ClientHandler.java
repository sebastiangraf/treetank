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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.treetank.encryption.cache.KeyCache;
import org.treetank.encryption.database.KeyManagerDatabase;
import org.treetank.encryption.database.KeySelectorDatabase;
import org.treetank.encryption.utils.NodeEncryption;

/**
 * Class represents the client side handler.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class ClientHandler {

    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeySelectorDatabase mKeySelectorDb;

    /**
     * Instance of KeyManagerDatabase holding key selection stuff.
     */
    private static KeyManagerDatabase mKeyManagerDb;

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
    public ClientHandler() {
        mEnHandler = EncryptionController.getInstance();
        mKeySelectorDb = mEnHandler.getKeySelectorInstance();
        mKeyManagerDb = mEnHandler.getKeyManagerInstance();
        mKeyCache = mEnHandler.getKeyCacheInstance();
    }

    /**
     * Decrypts key trails and put it to users key cache to make it available to
     * encrypt/decrypt data.
     * 
     * @param paramKeyTails
     *            map of key trails.
     */
    public final void decryptKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        // if map contains no key trails user has been completely removed
        // from DAG and all keys for user has to be removed.
        if (paramKeyTails.size() != 0) {

            if (mEnHandler.getKeyCache() == null) {
                initKeyCacheKeys(mEnHandler.getUser());
            }

            final Iterator<Long> mIter = paramKeyTails.keySet().iterator();
            while (mIter.hasNext()) {
                long mapKey = (Long)mIter.next();

                byte[] mChildSecretKey =
                    mKeySelectorDb.getEntry(mapKey).getSecretKey();
                byte[] mDecryptedBytes =
                    NodeEncryption.decrypt(paramKeyTails.get(mapKey),
                        mChildSecretKey);

                long mEncryptedKey =
                    NodeEncryption.byteArrayToLong(mDecryptedBytes);

                final LinkedList<Long> mUserCache = mEnHandler.getKeyCache();

                if (!mUserCache.contains(mEncryptedKey)) {
                    mUserCache.add(mEncryptedKey);
                }
                mKeyCache.put(mEnHandler.getUser(), mUserCache);
            }
        }
        // } else {
        // mKeyCache.put(mEnHandler.getUser(), new LinkedList<Long>());
        // }
    }

    /**
     * When user is changed, keys for this user has to be brought from key manager to the cache.
     * 
     * @param paramUser
     *            new user.
     */
    public final void initKeyCacheKeys(final String paramUser) {

        final Set<Long> keySet = mKeyManagerDb.getEntry(paramUser).getKeySet();

        final LinkedList<Long> keyList = new LinkedList<Long>();
        final Iterator<Long> mIter = keySet.iterator();
        while (mIter.hasNext()) {
            keyList.add(mIter.next());
        }

        mKeyCache.put(paramUser, keyList);

    }

}
