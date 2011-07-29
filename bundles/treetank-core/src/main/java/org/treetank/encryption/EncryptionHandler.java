package org.treetank.encryption;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.treetank.access.Database;
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
    private static final File SEL_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkley keying material db.
     */
    private static final File MAT_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("secretmaterialdb").toString());

    /**
     * Store path of berkley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("keymanagerdb").toString());

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
     * Initiates all needed instances comprising Berkeley DBs and key cache.
     * Additionally it initiates parsing of initial right tree and
     * setup of Berkeley DBs.
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
     * @throws TTEncryptionException
     *             Exception occurred during joining process.
     */
    public void joinGroup(final String paramUser, final String paramGroup) throws TTEncryptionException {
        try {
            // check if group exits.
            if (nodeExists(paramGroup)) {
                final long mGroupId = getNodeIdByName(paramGroup);
                // check if user exists; if so, check if
                // it is already member of group.
                if (nodeExists(paramUser)) {
                    boolean userGroupCheck = true;
                    for (long i = 0; i < mSelectorDb.count(); i++) {
                        KeySelector mSelector = mSelectorDb.getPersistent(i);
                        if (mSelector.getName().equals(paramUser)
                            && mSelector.getParents().contains(mGroupId)) {
                            userGroupCheck = false;
                        }
                    }
                    if (userGroupCheck) {
                        // user is not member of this group yet.
                        // add group id to its parent list.
                        final long mUserId = getNodeIdByName(paramUser);
                        final KeySelector mSelector = mSelectorDb.getPersistent(mUserId);
                        mSelector.addParent(mGroupId);
                        mSelectorDb.putPersistent(mSelector);

                        // increase revision in key trail and create new
                        // keks and tek in material db
                        final List<Long> mKeyTrail = getKeyTrail(mGroupId);
                        final List<Long> mMaterialKeys = new LinkedList<Long>();

                        for (int i = 0; i < mKeyTrail.size(); i++) {
                            final KeySelector mSel = mSelectorDb.getPersistent(mKeyTrail.get(i));
                            mSelectorDb.putPersistent(mSel);
                            final long mMatKey = mMaterialDb.putPersistent(mSel);
                            mMaterialKeys.add(mMatKey);
                        }

                        // add new key trail and tek to initial key
                        // list of user.
                        final KeyManager mManager = mManagerDb.getPersistent(paramUser);
                        mManager.addInitialKeyTrail(mMaterialKeys);

                        final long mTek = mMaterialKeys.get(mMaterialKeys.size() - 1);
                        mManager.addTEK(mTek);
                        mManagerDb.putPersistent(mManager);

                        // transmit new kek to all other users.
                        transmitKEK(paramUser, mTek);

                    } else {
                        throw new TTEncryptionException("User is already member of this group!");
                    }
                } else {
                    // user does not exist yet. create user and its keying
                    // material and add group id to its parent list.
                    final KeySelector mSelector = new KeySelector(paramUser);
                    mSelector.addParent(mGroupId);
                    mSelectorDb.putPersistent(mSelector);
                    mMaterialDb.putPersistent(mSelector);

                    // increase revision in key trail and create new
                    // KEKs and TEK in material db.
                    final List<Long> mKeyTrail = getKeyTrail(mGroupId);
                    final List<Long> mMaterialKeys = new LinkedList<Long>();

                    for (int i = 0; i < mKeyTrail.size(); i++) {
                        final KeySelector mSel = mSelectorDb.getPersistent(mKeyTrail.get(i));
                        mSel.increaseRevision();
                        mSelectorDb.putPersistent(mSel);
                        final long mMatKey = mMaterialDb.putPersistent(mSel);
                        mMaterialKeys.add(mMatKey);
                    }

                    final Map<Long, List<Long>> mKeyTrails = new HashMap<Long, List<Long>>();
                    mKeyTrails.put(mMaterialKeys.get(0), mMaterialKeys);

                    final KeyManager mManager = new KeyManager(paramUser, mKeyTrails);
                    mManagerDb.putPersistent(mManager);

                    // transmit new kek to all other users.
                    transmitKEK(paramUser, mManager.getTEKs().get(0));
                }

            } else {
                throw new TTEncryptionException("Group does not exist!");
            }

        } catch (final TTEncryptionException ttee) {
            ttee.printStackTrace();
            System.exit(0);
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

    }

    /**
     * Checks whether a node exists or not.
     * 
     * @param paramNodeName
     *            node name.
     * @return
     *         node existence.
     */
    private boolean nodeExists(final String paramNodeName) {
        for (int i = 0; i < mSelectorDb.count(); i++) {
            final KeySelector mSelector = mSelectorDb.getPersistent(i);
            if (mSelector.getName().equals(paramNodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns selector node id by given node name.
     * 
     * @param paramNodeName
     *            node name for what the id should found.
     * @return
     *         selector node id.
     */
    private long getNodeIdByName(final String paramNodeName) {
        for (int i = 0; i < mSelectorDb.count(); i++) {
            final KeySelector mSelector = mSelectorDb.getPersistent(i);
            if (mSelector.getName().equals(paramNodeName)) {
                return mSelector.getKeyId();
            }
        }
        return -1;
    }

    /**
     * Returns the complete key trail from leave to root.
     * 
     * @param paramGroupKey
     *            Node key of leave.
     * 
     * @return
     *         List of key trail.
     */
    private List<Long> getKeyTrail(final long paramGroupKey) {
        final List<Long> mKeyTrail = new LinkedList<Long>();
        mKeyTrail.add(paramGroupKey);
        List<Long> mParentList = mSelectorDb.getPersistent(paramGroupKey).getParents();

        while (mParentList.size() != 0) {
            final long newParent = mParentList.get(0);
            mKeyTrail.add(newParent);
            mParentList = mSelectorDb.getPersistent(newParent).getParents();
        }
        return mKeyTrail;
    }

    /**
     * Transmits a new KEK to all users into a hierarchy except
     * the one that has joined or left.
     * 
     * @param paramUser
     *            User that has joined or left.
     * 
     * @param paramTEK
     *            New TEK to be transmitted to the users.
     */
    private void transmitKEK(final String paramUser, final long paramTEK) {
        // iterate through all nodes to find all user ids.
        final Map<Long, String> mUsers = new HashMap<Long, String>();
        for (int i = 0; i < mSelectorDb.count(); i++) {
            KeySelector mSelector = mSelectorDb.getPersistent(i);
            mUsers.put(mSelector.getKeyId(), mSelector.getName());
            if (mSelector.getParents().size() > 0) {
                for (long l : mSelector.getParents()) {
                    if (mUsers.containsKey(l)) {
                        mUsers.remove(l);
                    }
                }
            }
        }
        // remove id from user that joined the group.
        mUsers.remove(getNodeIdByName(paramUser));

        // add new TEK to all other users.
        final Iterator iter = mUsers.keySet().iterator();
        while (iter.hasNext()) {
            final String mUser = mUsers.get(iter.next());
            final KeyManager mManager = mManagerDb.getPersistent(mUser);
            mManager.addTEK(paramTEK);
            mManagerDb.putPersistent(mManager);
        }
    }

    /**
     * Returns the initial TEK Id of the current logged user.
     * 
     * @return
     *         First TEK Id of user.
     */
    public long getInitialTEKId() {
        KeyManager manager = mManagerDb.getPersistent(mUser);
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
            Database.truncateDatabase(SEL_STORE);
            System.out.println("Selector DB has been removed.");
        }
        if (MAT_STORE.exists()) {
            Database.truncateDatabase(MAT_STORE);
            System.out.println("Secret Material DB has been removed.");
        }
        if (MAN_STORE.exists()) {
            Database.truncateDatabase(MAN_STORE);
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
            final List<Long> mParentsList = mSelectorDb.getPersistent(i).getParents();
            for (int k = 0; k < mParentsList.size(); k++) {
                mParentsString.append("#" + mParentsList.get(k));
            }

            System.out.println("Node: " + mSelectorDb.getPersistent(i).getKeyId() + " "
                + mSelectorDb.getPersistent(i).getName() + " " + mParentsString.toString() + " "
                + mSelectorDb.getPersistent(i).getRevision() + " "
                + mSelectorDb.getPersistent(i).getVersion());

        }

        /**
         * print key material db.
         */
        for (int i = 0; i < mMaterialDb.count(); i++) {

            System.out.println("Material " + mMaterialDb.getPersistent(i).getMaterialKey() + ": "
                + mMaterialDb.getPersistent(i).getSelectorKey() + " "
                + mMaterialDb.getPersistent(i).getRevsion() + " " + mMaterialDb.getPersistent(i).getVersion()
                + " " + mMaterialDb.getPersistent(i).getSecretKey());
        }

        /**
         * print key manager db
         */
        final SortedMap<String, KeyManager> sMap = mManagerDb.getEntries();

        // iterate through all users
        Iterator iter = sMap.keySet().iterator();
        while (iter.hasNext()) {
            String user = (String)iter.next();
            System.out.println("Initial key trails of " + user);

            Map<Long, List<Long>> mKeyTrails = mManagerDb.getPersistent(user).getInitialKeys();

            // iterate through all key trails of user
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
