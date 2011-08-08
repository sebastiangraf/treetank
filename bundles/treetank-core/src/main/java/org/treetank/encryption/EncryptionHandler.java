package org.treetank.encryption;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.access.Database;
import org.treetank.api.ISession;
import org.treetank.cache.KeyCache;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTEncryptionException;
import org.treetank.exception.TTIOException;

/**
 * Singleton class holding and handling all necessary operations and
 * data for encryption process.
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

    private static ISession mSession;

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
     * Constructor of singleton class that initiates all needed instances.
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
                Database.truncateDatabase(SEL_STORE);
            }
            if (MAN_STORE.exists()) {
                Database.truncateDatabase(MAN_STORE);
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

    public boolean checkEncryption() {
        return mNodeEncryption;
    }

    public String getUser() {
        // return mSession.getUser();
        return mLoggedUser;
    }

    public List<Long> getKeyCache() {
        return mKeyCache.get(getUser());
    }

    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

    public KeySelectorDatabase getKeySelectorInstance() {
        return mKeySelectorDb;
    }

    public KeyManagerDatabase getKeyManagerInstance() {
        return mKeyManagerDb;
    }

    public KeyCache getKeyCacheInstance() {
        return mKeyCache;
    }

}
