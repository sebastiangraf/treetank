package org.treetank.encryption;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;

import org.treetank.access.FileDatabase;
import org.treetank.cache.KeyCache;
import org.treetank.exception.TTEncryptionException;

/**
 * Singleton class holding and handling all necessary operations and
 * data for encryption process.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public final class EncryptionHandler {

    /**
     * Singleton instance.
     */
    private static EncryptionHandler mINSTANCE;

    /**
     * Instance for activating or deactivating encryption process.
     */
    private final boolean mNodeEncryption = false;

    /**
     * Helper variable for current logged user.
     */
    private final String mUser = "TESTUSER1";
    // spaeter Schnittstelle um aktuellen User zu erhalten

    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeySelectorDatabase mSelectorDb;

    /**
     * Instance of KeyMaterialDatabase holding keying material stuff.
     */
    private static KeyMaterialDatabase mMaterialDb;

    /**
     * Instance of KeyManagerDatabase holding key manager stuff.
     */
    private static KeyManagerDatabase mManagerDb;

    /**
     * Instance for key cache holding last changes of keying material.
     */
    private static KeyCache mKeyCache;

    /**
     * Instance of helper class NodeEncryption that provides operations
     * for en-/decryption.
     */
    private static NodeEncryption mNodeEncrypt;

    /**
     * Store path of berkley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkley keying material db.
     */
    private static final File MAT_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("secretmaterialdb").toString());

    /**
     * Store path of berkley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

    /**
     * Constructor of singleton class that initiates all needed instances.
     */
    private EncryptionHandler() {
        if (checkEncryption()) {
            init();
        }
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
     * Initiates all needed instances comprising berkley dbs and key cache.
     * Additionally it initiates parsing of initial right tree and
     * setup of berkley dbs.
     */
    private void init() {
        mSelectorDb = new KeySelectorDatabase(SEL_STORE);
        mMaterialDb = new KeyMaterialDatabase(MAT_STORE);
        mManagerDb = new KeyManagerDatabase(MAN_STORE);
        mKeyCache = new KeyCache();
        mNodeEncrypt = new NodeEncryption();

        new EncryptionTreeParser().init(mSelectorDb, mMaterialDb, mManagerDb);
    }

    /**
     * Invoked when a new user joining a group.
     * 
     * @param paramUser
     *            new user name joining a group.
     * @param paramGroup
     *            name of goup the user joins.
     */
    public void joinGroup(final String paramUser, final String paramGroup) {
        try {
            if (groupExists(paramGroup)) {
                boolean userGroupCheck = true;
                for (long i = 0; i < mSelectorDb.count(); i++) {
                    KeySelector mSelector = mSelectorDb.getPersistent(i);
                    if (mSelector.getName().equals(paramUser)
                        && mSelector.getParent().equals(paramGroup)) {
                        userGroupCheck = false;
                    }
                }
                if (userGroupCheck) {

                    /*
                     * add new user and its data
                     */

                    // new user entity + keying material
                    KeySelector mEntity =
                        new KeySelector(paramUser, paramGroup);
                    mEntity.increaseRevision();
                    mSelectorDb.putPersistent(mEntity);
                    final long userMatKey = mMaterialDb.putPersistent(mEntity);

                    // increase all revisions of node path from leaf to 
                    // root and create new secret material

                    final Queue<Long> mHelpQueue = new LinkedList<Long>();

                    String mParent = paramGroup;
                    // secret key of current user
                    byte[] entitySecretKey =
                        mMaterialDb.getPersistent(userMatKey).getSecretKey();

                    while (mParent != null) {
                        for (int i = 0; i < mSelectorDb.count(); i++) {
                            KeySelector mSelector =
                                mSelectorDb.getPersistent(i);
                            if (mSelector.getName().equals(mParent)) {
                                mSelector.increaseRevision();
                                // add new node'
                                long newMatKey =
                                    mMaterialDb.putPersistent(mSelector);
                                // new uncrypted secret key for node'
                                byte[] uncryptedSecretKey =
                                    mMaterialDb.getPersistent(newMatKey)
                                        .getSecretKey();
                                // decrypted secret key with childs secret key
                                byte[] decryptedSecretKey =
                                    mNodeEncrypt.encrypt(uncryptedSecretKey,
                                        entitySecretKey);
                                // set new secret key
                                mMaterialDb.getPersistent(newMatKey)
                                    .setSecretKey(decryptedSecretKey);
                                // add node's material key to queue
                                mHelpQueue.add(newMatKey);

                                mParent =
                                    mSelectorDb.getPersistent(i).getParent();
                                entitySecretKey = uncryptedSecretKey;
                            }
                        }
                    }
                    // build cache list from help list
                    final LinkedList<Long> mCacheList = new LinkedList<Long>();
                    for (int i = 0; i < mHelpQueue.size(); i++) {
                        mCacheList.add(mHelpQueue.remove());
                    }
                    // add keys of new user to key manager
                    mManagerDb.putPersistent(paramUser, mCacheList, mCacheList
                        .get(0));

                    // // add list to lru cache
                    // mKeyCache.put(paramUser, mCacheList);

                    /*
                     * change secret and cache material for all 
                     * other users of group
                     */

                    for (int i = 0; i < mSelectorDb.count(); i++) {
                        KeySelector mSelector = mSelectorDb.getPersistent(i);
                        if (mSelector.getParent().equals(paramGroup)
                            && mSelector.getName() != paramUser) {
                            // name of existing user in group
                            String groupUser = mSelector.getName();
                            // unique selector id of user
                            long selectorId = mSelector.getKeyId();
                            // get secret material of user
                            for (int j = 0; j < mMaterialDb.count(); j++) {
                                KeyingMaterial mMaterial =
                                    mMaterialDb.getPersistent(j);
                                if (mMaterial.getSelectorKey() == selectorId) {

                                    // secret key of user
                                    byte[] userSecretKey =
                                        mMaterial.getSecretKey();

                                    // TODO:
                                    // 1) pruefen ob cache fuer user existiert, falls ja ziehe parent key
                                    // heraus, falls nein, nehme inital keys und ziehe parent key raus
                                    // 2) verschluessele parent key mit user secret key
                                    // 3) fuege den parent key als neuen node in material db ein
                                    // 4) ersetze den verschluesselten parent key mit letzter position in
                                    // mcachelist
                                    // 5) fuege die erste position der mcachelist in tek liste ein
                                    // 6) schreibe mcacheliste als letzter aenderung in lru cache

                                    // uncrypted secret key of parent

                                }
                            }
                        }
                    }

                    // add list to lru cache
                    mKeyCache.put(paramUser, mCacheList);

                } else {
                    throw new TTEncryptionException(
                        "User is already member of this group!");
                }
            } else {
                throw new TTEncryptionException("Group to join does not exist!");
            }
        } catch (final TTEncryptionException ttee) {
            ttee.printStackTrace();
        }
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
        // Operationen auf db und cache ebene wenn user geloescht wird
        System.out.println("i do");

    }

    /**
     * Checks whether a group exists or not.
     * 
     * @param paramGroup
     *            group name.
     * @return
     *         group existence.
     */
    private boolean groupExists(final String paramGroup) {
        for (long i = 0; i < mSelectorDb.count(); i++) {
            KeySelector mSelector = mSelectorDb.getPersistent(i);
            if (mSelector.getName().equals(paramGroup)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the initial TEK (transfer encryption key) id of a user.
     * 
     * @return
     *         initial TEK id.
     */
    public long getInitialTEKId() {
        final KeyManager manager = mManagerDb.getPersistent(mUser);
        return manager.getTEKs().get(0);
    }

    /**
     * Returns a list of all TEKs a user owns.
     * 
     * @return
     *         list of TEKs.
     */
    public List<Long> getTEKs() {
        final KeyManager manager = mManagerDb.getPersistent(mUser);
        return manager.getTEKs();

    }

    /**
     * Clears all established berkeley dbs.
     */
    public void clear() {
        if (SEL_STORE.exists()) {
            FileDatabase.truncateDatabase(SEL_STORE);
            System.out.println("Selector DB has been removed.");
        }
        if (MAT_STORE.exists()) {
            FileDatabase.truncateDatabase(MAT_STORE);
            System.out.println("Secret Material DB has been removed.");
        }
        if (MAN_STORE.exists()) {
            FileDatabase.truncateDatabase(MAN_STORE);
            System.out.println("Key Manager DB has been  removed.");
        }
    }

    /**
     * Prints all stored information of KeySelector, KeyingMaterial 
     * and KeyManager database. This method is just for testing issues.
     */
    public void print() {
        for (long i = 0; i < mSelectorDb.count(); i++) {
            System.out.println("node: "
                + mSelectorDb.getPersistent(i).getKeyId() + " "
                + mSelectorDb.getPersistent(i).getName() + " "
                + mSelectorDb.getPersistent(i).getParent() + " "
                + mSelectorDb.getPersistent(i).getRevision() + " "
                + mSelectorDb.getPersistent(i).getVersion());

        }

        for (long i = 0; i < mMaterialDb.count(); i++) {

            System.out.println("material "
                + mMaterialDb.getPersistent(i).getMaterialKey() + ": "
                + mMaterialDb.getPersistent(i).getSelectorKey() + " "
                + mMaterialDb.getPersistent(i).getRevsion() + " "
                + mMaterialDb.getPersistent(i).getVersion() + " "
                + mMaterialDb.getPersistent(i).getSecretKey());
        }

        final SortedMap<String, KeyManager> sMap = mManagerDb.getEntries();

        Iterator iter = sMap.keySet().iterator();
        while (iter.hasNext()) {
            String user = (String) iter.next();
            System.out.print("Initial keys of " + user + " ");
            for (long l : mManagerDb.getPersistent(user).getInitialKeys()) {
                System.out.print(l + " ");
            }
            System.out.println();
            System.out.print("TEKs of " + user + " ");
            for (long l : mManagerDb.getPersistent(user).getTEKs()) {
                System.out.print(l + " ");
            }
            System.out.println();
        }
    }

    /**
     * Checks if encryption is activated or not.
     * 
     * @return
     *         encrpytion state.
     */
    public boolean checkEncryption() {
        return this.mNodeEncryption;
    }

    /**
     * Gets current logged user.
     * 
     * @return
     *         logged user.
     */
    public String getUser() {
        return this.mUser;
    }

    /**
     * Returns keying material by a given id.
     * 
     * @param paramKey
     *            unquie key material id.
     * @return
     *         instance of KeyingMaterial.
     */
    public KeyingMaterial getKeyMaterial(final long paramKey) {
        return mMaterialDb.getPersistent(paramKey);
    }

}
