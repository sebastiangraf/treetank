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
     * Instance of KeyMaterialDatabase holding keying material stuff.
     */
    private static KeyMaterialDatabase mKeyMaterialDb;

    /**
     * Instance of KeyManagerDatabase holding key manager stuff.
     */
    private static KeyManagerDatabase mKeyManagerDb;

    /**
     * Instance of KeyCache holding all current keys of user.
     */
    private static KeyCache mKeyCache;

    private static ISession mSession;

    /**
     * The key data should be encrypted.
     */
    private long mDataEncryptionKey;

    /**
     * Store path of berkeley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkeley keying material db.
     */
    private static final File MAT_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("secretmaterialdb").toString());

    /**
     * Store path of berkeley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

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
    public void init(final ISession paramSession, final long paramDEK)
        throws TTEncryptionException {
        if (mNodeEncryption) {
            mKeySelectorDb = new KeySelectorDatabase(SEL_STORE);
            mKeyMaterialDb = new KeyMaterialDatabase(MAT_STORE);
            mKeyManagerDb = new KeyManagerDatabase(MAN_STORE);
            mSession = paramSession;
            mDataEncryptionKey = paramDEK;
            mKeyCache = new KeyCache();
            new EncryptionTreeParser().init(this);
        } else {
            throw new TTEncryptionException("Encryption is disabled!");
        }
    }

    /**
     * Invoked when a new user joining a group.
     * 
     * @param paramUser
     *            new user name joining a group.
     * @param paramGroup
     *            name of goup the user joins.
     * @throws TTEncryptionException
     *             Exception occurred during joining process.
     */
    public void joinGroup(final String paramUser, final String paramGroup)
        throws TTEncryptionException {

    }

    /**
     * Invoked when a new user leaving a group.
     * 
     * @param paramUser
     *            user name leaving a group.
     * @param paramGroup
     *            name of goup the user leaves.
     */
    public void leaveGroup(final String paramUser, final String paramGroup) {

    }

    /*
     * Clears all established berkeley dbs.
     */
    public void clear() {
        try {
            if (SEL_STORE.exists()) {
                Database.truncateDatabase(SEL_STORE);
            }
            if (MAT_STORE.exists()) {
                Database.truncateDatabase(MAT_STORE);
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
            final SortedMap<Long, KeySelector> mSelMap =
                mKeySelectorDb.getEntries();
            Iterator iter = mSelMap.keySet().iterator();

            System.out.println("\nSelector DB Size: " + mKeySelectorDb.count());

            while (iter.hasNext()) {

                final KeySelector mSelector = mSelMap.get(iter.next());
                final List<Long> mParentsList = mSelector.getParents();
                final List<Long> mChildsList = mSelector.getChilds();

                final StringBuilder mParentsString = new StringBuilder();
                for (int k = 0; k < mParentsList.size(); k++) {
                    mParentsString.append("#" + mParentsList.get(k));
                }

                final StringBuilder mChildsString = new StringBuilder();
                for (int k = 0; k < mChildsList.size(); k++) {
                    mChildsString.append("#" + mChildsList.get(k));
                }

                System.out.println("Selector: " + mSelector.getPrimaryKey()
                    + " " + mSelector.getName() + " " + mSelector.getType()
                    + " " + mParentsString.toString() + " "
                    + mChildsString.toString() + " " + mSelector.getRevision()
                    + " " + mSelector.getVersion());
            }
            System.out.println();

            /*
             * print key material db.
             */
            final SortedMap<Long, KeyMaterial> mMatMap =
                mKeyMaterialDb.getEntries();
            iter = mMatMap.keySet().iterator();

            System.out.println("Material DB Size: " + mKeyMaterialDb.count());

            while (iter.hasNext()) {
                final KeyMaterial mMaterial = mMatMap.get(iter.next());

                final List<Long> mParentsList = mMaterial.getParents();
                final List<Long> mChildsList = mMaterial.getChilds();

                final StringBuilder mParentsString = new StringBuilder();
                for (int k = 0; k < mParentsList.size(); k++) {
                    mParentsString.append("#" + mParentsList.get(k));
                }

                final StringBuilder mChildsString = new StringBuilder();
                for (int k = 0; k < mChildsList.size(); k++) {
                    mChildsString.append("#" + mChildsList.get(k));
                }

                System.out.println("Material: " + mMaterial.getPrimaryKey()
                    + " " + mMaterial.getRevsion() + " "
                    + mMaterial.getVersion() + " " + mParentsString.toString()
                    + " " + mChildsString.toString() + " "
                    + mMaterial.getSecretKey());
            }
            System.out.println();

            /*
             * print key manager db
             */
            final SortedMap<String, KeyManager> sMap =
                mKeyManagerDb.getEntries();

            // iterate through all users
            final Iterator outerIter = sMap.keySet().iterator();

            System.out.println("Key manager DB Size: " + mKeyManagerDb.count());

            StringBuilder sb;
            while (outerIter.hasNext()) {
                final String user = (String)outerIter.next();
                sb = new StringBuilder(user + ": ");

                final Set<Long> mKeySet =
                    mKeyManagerDb.getEntry(user).getKeySet();

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
            final StringBuilder cacheString =
                new StringBuilder(getUser() + ": ");
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
        //return mSession.getUser();
        return "U2";
    }

    public List<Long> getKeyCache() {
        return mKeyCache.get(getUser());
    }

    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

    public KeyMaterialDatabase getKeyMaterialDBInstance() {
        return mKeyMaterialDb;
    }

    public KeySelectorDatabase getKeySelectorDBInstance() {
        return mKeySelectorDb;
    }

    public KeyManagerDatabase getKeyManagerDBInstance() {
        return mKeyManagerDb;
    }

    public KeyCache getKeyCacheInstance() {
        return mKeyCache;
    }
}
