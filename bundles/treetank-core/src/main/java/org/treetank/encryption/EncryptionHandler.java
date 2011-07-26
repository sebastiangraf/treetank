package org.treetank.encryption;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.treetank.access.FileDatabase;
import org.treetank.cache.KeyCache;

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
            // clear();
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
    // public void joinGroup(final String paramUser, final String paramGroup) {
    // try {
    // if (groupExists(paramGroup)) {
    // boolean userGroupCheck = true;
    // for (long i = 0; i < mSelectorDb.count(); i++) {
    // KeySelector mSelector = mSelectorDb.getPersistent(i);
    // if (mSelector.getName().equals(paramUser) && mSelector.getParent().equals(paramGroup)) {
    // userGroupCheck = false;
    // }
    // }
    // if (userGroupCheck) {
    //
    // /*
    // * add new user and its data
    // */
    //
    // // new user entity + keying material
    // KeySelector mEntity = new KeySelector(paramUser, paramGroup);
    // mEntity.increaseRevision();
    // mSelectorDb.putPersistent(mEntity);
    // // keying material for new user
    // final long userMatKey = mMaterialDb.putPersistent(mEntity);
    //
    // /*
    // * increase all revisions of node path from leaf to
    // * root and create new secret material
    // */
    //
    // // secret key of current entity
    // byte[] entitySecretKey = mMaterialDb.getPersistent(userMatKey).getSecretKey();
    //
    // final Queue<Long> mKeyTrailQueue = new LinkedList<Long>();
    //
    // final Map<Long, byte[]> mNewSecretKeys = new HashMap<Long, byte[]>();
    //
    //
    // // parent node of current entity
    // String mParent = paramGroup;
    //
    // while (mParent != null) {
    // //iterate through all nodes to find group node
    // for (int i = 0; i < mSelectorDb.count(); i++) {
    // KeySelector mSelector = mSelectorDb.getPersistent(i);
    // if (mSelector.getName().equals(mParent)) {
    // // increase revision and add new node'
    // mSelector.increaseRevision();
    // long newMatKey = mMaterialDb.putPersistent(mSelector);
    // // new uncrypted secret key for node'
    // byte[] uncryptedSecretKey =
    // mMaterialDb.getPersistent(newMatKey).getSecretKey();
    // mNewSecretKeys.put(newMatKey, uncryptedSecretKey);
    // // decrypt new secret key with child's secret key
    // byte[] decryptedSecretKey =
    // mNodeEncrypt.encrypt(uncryptedSecretKey, entitySecretKey);
    // // set new secret key
    // mMaterialDb.getPersistent(newMatKey).setSecretKey(decryptedSecretKey);
    // // add node's material key to key trail queue
    // mKeyTrailQueue.add(newMatKey);
    //
    // mParent = mSelectorDb.getPersistent(i).getParent();
    // entitySecretKey = uncryptedSecretKey;
    // }
    // }
    // }
    //
    // // build reverse cache list from key trail queue
    // final LinkedList<Long> mCacheList = new LinkedList<Long>();
    //
    // for (int i = 0; i < mKeyTrailQueue.size(); i++) {
    // mCacheList.add(mKeyTrailQueue.remove());
    // }
    //
    // // add keys of new user to key manager
    // mManagerDb.putPersistent(paramUser, mCacheList, mCacheList.get(0));
    // // add list to lru key cache
    // mKeyCache.put(paramUser, mCacheList);
    //
    // /*
    // * change secret and cache material for all
    // * other users of group
    // */
    // final Set<String> mIterUsers = new HashSet<String>();
    // mIterUsers.add(paramUser);
    //
    // for (int i = 0; i < mSelectorDb.count(); i++) {
    // KeySelector mSelector = mSelectorDb.getPersistent(i);
    // // name of existing user in group
    // String mUserNodeName= mSelector.getName();
    // if (mSelector.getParent().equals(paramGroup) && !mIterUsers.contains(mUserNodeName)) {
    // // unique selector id of user
    // final long mSelectorId = mSelector.getKeyId();
    // // get secret material of user
    // for (int j = 0; j < mMaterialDb.count(); j++) {
    // KeyingMaterial mMaterial = mMaterialDb.getPersistent(j);
    // if (mMaterial.getSelectorKey() == mSelectorId) {
    //
    // // secret key of user
    // byte[] userSecretKey = mMaterial.getSecretKey();
    //
    //
    //
    // // TODO:
    // // 1) pruefen ob cache fuer user existiert, falls ja ziehe parent key
    // // heraus, falls nein, nehme inital keys und ziehe parent key raus
    // // 2) verschluessele parent key mit user secret key
    // // 3) fuege den parent key als neuen node in material db ein
    // // 4) ersetze den verschluesselten parent key mit letzter position in
    // // mcachelist
    // // 5) fuege die erste position der mcachelist in tek liste ein
    // // 6) schreibe mcacheliste als letzter aenderung in lru cache
    //
    // // uncrypted secret key of parent
    //
    // }
    // }
    // }
    // }
    //
    // // add list to lru cache
    // mKeyCache.put(paramUser, mCacheList);
    //
    // } else {
    // throw new TTEncryptionException("User is already member of this group!");
    // }
    // } else {
    // throw new TTEncryptionException("Group to join does not exist!");
    // }
    // } catch (final TTEncryptionException ttee) {
    // ttee.printStackTrace();
    // }
    // }

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
        /**
         * print key selector db.
         */
        for (int i = 0; i < mSelectorDb.count(); i++) {
            final StringBuilder mParentsString = new StringBuilder();
            final List<Long> mParentsList =
                mSelectorDb.getPersistent(i).getParents();
            System.out.println("parent size: " + mParentsList.size());
            for (int k = 0; k < mParentsList.size(); k++) {
                mParentsString.append("#" + mParentsList.get(k));
            }

            System.out.println("node: "
                + mSelectorDb.getPersistent(i).getKeyId() + " "
                + mSelectorDb.getPersistent(i).getName() + " "
                + mParentsString.toString() + " "
                + mSelectorDb.getPersistent(i).getRevision() + " "
                + mSelectorDb.getPersistent(i).getVersion());

        }

        /**
         * print key material db.
         */
        for (int i = 0; i < mMaterialDb.count(); i++) {

            System.out.println("material "
                + mMaterialDb.getPersistent(i).getMaterialKey() + ": "
                + mMaterialDb.getPersistent(i).getSelectorKey() + " "
                + mMaterialDb.getPersistent(i).getRevsion() + " "
                + mMaterialDb.getPersistent(i).getVersion() + " "
                + mMaterialDb.getPersistent(i).getSecretKey());
        }

        /**
         * print key manager db
         */
        final SortedMap<String, KeyManager> sMap = mManagerDb.getEntries();

        //iterate through all users
        Iterator iter = sMap.keySet().iterator();
        while (iter.hasNext()) {
            String user = (String) iter.next();
            System.out.println("Initial key trails of " + user);

            Map<Long, List<Long>> mKeyTrails =
                mManagerDb.getPersistent(user).getInitialKeys();
            
            //iterate through all key trails of user
            Iterator innerIter = mKeyTrails.keySet().iterator();
            while (innerIter.hasNext()) {
                List<Long> mKeyTrail = mKeyTrails.get(innerIter.next());
                for (long l : mKeyTrail) {
                    System.out.print(l + " ");
                }
                System.out.println();
            }

            System.out.println("TEKs of " + user + " ");
            for (long l : mManagerDb.getPersistent(user).getTEKs()) {
                System.out.print(l + " ");
            }
            System.out.println();
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
