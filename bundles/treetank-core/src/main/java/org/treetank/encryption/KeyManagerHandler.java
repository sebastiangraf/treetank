package org.treetank.encryption;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.exception.TTEncryptionException;

public class KeyManagerHandler {

    KeySelectorDatabase mKeySelectorDb;
    KeyManagerDatabase mKeyManagerDb;
    String mLoggedUser;

    /**
     * Invoked when a new user joining a group.
     * 
     * @param paramUser
     *            new user name joining a group.
     * @param paramGroup
     *            name of group the user joins.
     * @throws TTEncryptionException
     *             Exception occurred during joining process.
     */
    public void joinGroup(final String paramUser, final String paramGroup)
        throws TTEncryptionException {
        init();
        try {
            // check if group exits.
            if (nodeExists(paramGroup)) {
                final long mGroupId = getRecentNodeKey(paramGroup);
                // check if user exists and if is already member of group
                long mUserId = -1;
                boolean mUserExists = false;
                if (nodeExists(paramUser)) {
                    mUserExists = true;
                    mUserId = getRecentNodeKey(paramUser);
                }
                if (!userGroupCheck(paramUser, paramGroup)) {
                    /*
                     * create new node for each join affected tree tail node and update version, parent
                     * and child list.
                     */
                    // nodes that are affected by leave.
                    final List<Long> mTreePath = getTreePathNodes(mGroupId);

                    // old node id (key), new node id (value)
                    final Map<Long, Long> mNewSelIds =
                        new HashMap<Long, Long>();

                    // new node ids
                    final List<Long> mNewIdsList = new LinkedList<Long>();

                    long mNewGroupId = -1;

                    // create new group nodes.
                    for (int i = 0; i < mTreePath.size(); i++) {

                        final KeySelector mOldSel =
                            mKeySelectorDb.getEntry(mTreePath.get(i));

                        int mNewVersion = mOldSel.getVersion() + 1;

                        final KeySelector mNewSel =
                            new KeySelector(mOldSel.getName(), mOldSel
                                .getParents(), mOldSel.getChilds(), mOldSel
                                .getRevision(), mNewVersion, mOldSel.getType());

                        mKeySelectorDb.putEntry(mNewSel);
                        mNewSelIds.put(mOldSel.getPrimaryKey(), mNewSel
                            .getPrimaryKey());
                        mNewIdsList.add(mNewSel.getPrimaryKey());

                        if (mOldSel.getPrimaryKey() == mGroupId) {
                            mNewGroupId = mNewSel.getPrimaryKey();
                        }
                    }

                    // update parent and child list of each new node.
                    for (int i = 0; i < mNewIdsList.size(); i++) {
                        final KeySelector mSel =
                            mKeySelectorDb.getEntry(mNewIdsList.get(i));

                        final Long[] parArray =
                            new Long[mSel.getParents().size()];
                        mSel.getParents().toArray(parArray);

                        final Long[] childArray =
                            new Long[mSel.getChilds().size()];
                        mSel.getChilds().toArray(childArray);

                        for (int j = 0; j < parArray.length; j++) {
                            long lastKey =
                                getRecentNodeKey(mKeySelectorDb.getEntry(
                                    parArray[j]).getName());
                            mSel.removeParent(parArray[j]);
                            mSel.addParent(lastKey);
                        }

                        for (int j = 0; j < childArray.length; j++) {
                            long lastKey =
                                getRecentNodeKey(mKeySelectorDb.getEntry(
                                    childArray[j]).getName());
                            mSel.removeChild(childArray[j]);
                            mSel.addChild(lastKey);
                        }

                        if (mNewIdsList.get(i) == getRecentNodeKey(paramGroup)) {
                            if (mSel.getChilds().contains(
                                getRecentNodeKey(paramUser))) {
                                mSel.removeChild(getRecentNodeKey(paramUser));
                            }
                        }
                        mKeySelectorDb.putEntry(mSel);
                    }

                    // create new user node with new version, parent list and add its new id as child to the
                    // new node of its joining group.
                    final KeySelector mNewUserSel;
                    if (mUserExists) {
                        final KeySelector mOldUserSel =
                            mKeySelectorDb.getEntry(mUserId);

                        final LinkedList<Long> mUserParentList =
                            mOldUserSel.getParents();
                        mUserParentList.add(mNewGroupId);

                        final int mNewVersion = mOldUserSel.getVersion() + 1;

                        mNewUserSel =
                            new KeySelector(mOldUserSel.getName(),
                                mUserParentList, mOldUserSel.getChilds(),
                                mOldUserSel.getRevision(), mNewVersion,
                                mOldUserSel.getType());

                        mKeySelectorDb.putEntry(mNewUserSel);
                        mNewSelIds.put(mOldUserSel.getPrimaryKey(), mNewUserSel
                            .getPrimaryKey());
                        mNewIdsList.add(mNewUserSel.getPrimaryKey());
                    } else {
                        mNewUserSel =
                            new KeySelector(paramUser, new LinkedList<Long>(),
                                new LinkedList<Long>(), 0, 0, EntityType.USER);
                        mNewUserSel.addParent(mNewGroupId);
                        mKeySelectorDb.putEntry(mNewUserSel);
                        mNewSelIds.put(-1L, mNewUserSel.getPrimaryKey());
                        mNewIdsList.add(mNewUserSel.getPrimaryKey());

                        // create key manager entry for new user.
                        mKeyManagerDb.putEntry(new KeyManager(paramUser,
                            new HashSet<Long>()));

                    }

                    final KeySelector mGroupSel =
                        mKeySelectorDb.getEntry(mNewGroupId);
                    mGroupSel.addChild(mNewUserSel.getPrimaryKey());
                    mKeySelectorDb.putEntry(mGroupSel);

                    /*
                     * update each user key set in key manager.
                     */
                    final SortedMap<String, KeyManager> mManMap =
                        mKeyManagerDb.getEntries();
                    final Iterator mOuterIter = mManMap.keySet().iterator();

                    while (mOuterIter.hasNext()) { // iterate through all users.
                        final String mKeyUser = (String)mOuterIter.next();
                        final KeyManager mManager = mManMap.get(mKeyUser);

                        final Iterator mInnerIter =
                            mNewSelIds.keySet().iterator();
                        while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                            long mId = (Long)mInnerIter.next();

                            if (mKeyUser.equals(paramUser)) {
                                mManager.addKey(mNewSelIds.get(mId));
                            } else if (mManager.getKeySet().contains(mId)) {
                                mManager.addKey(mNewSelIds.get(mId));
                            }
                        }
                        mKeyManagerDb.putEntry(mManager);
                    }

                    /*
                     * create and encrypt key trails for logged user.
                     */
                    final Map<Long, byte[]> mKeyTrails =
                        new HashMap<Long, byte[]>();

                    final KeyManager mKeyManager =
                        mKeyManagerDb.getEntry(mLoggedUser);
                    final Set<Long> mUserKeySet = mKeyManager.getKeySet();

                    final Iterator mSetIter = mUserKeySet.iterator();
                    while (mSetIter.hasNext()) {
                        final long mMapId = (Long)mSetIter.next();
                        if (mNewIdsList.contains(mMapId)) {
                            final KeySelector mSel =
                                mKeySelectorDb.getEntry(mMapId);
                            final List<Long> mChilds = mSel.getChilds();
                            for (int i = 0; i < mChilds.size(); i++) {
                                if (mUserKeySet.contains(mChilds.get(i))) {
                                    final KeySelector mChildSel =
                                        mKeySelectorDb.getEntry(mChilds.get(i));
                                    final byte[] mChildSecretKey =
                                        mChildSel.getSecretKey();
                                    final byte[] mIdAsByteArray =
                                        NodeEncryption.longToByteArray(mMapId);
                                    final byte[] mEncryptedId =
                                        NodeEncryption.encrypt(mIdAsByteArray,
                                            mChildSecretKey);
                                    mKeyTrails
                                        .put(mChilds.get(i), mEncryptedId);
                                }
                            }
                        }
                    }

                    transmitKeyTrails(mKeyTrails);

                } else {
                    throw new TTEncryptionException("User " + paramUser
                        + " is already member of given group " + paramGroup + "!");
                }
            } else {
                throw new TTEncryptionException("Group "+ paramGroup +" does not exist!");
            }
        } catch (final TTEncryptionException mTTExp) {
            mTTExp.printStackTrace();
            System.exit(0);
        }

    }

    /**
     * Invoked when a new user leaving a group.
     * 
     * @param paramUser
     *            user name leaving a group.
     * @param paramGroup
     *            name of group the user leaves.
     */
    public void leaveGroup(final String paramUser, final String paramGroup) {
        init();
        try {
            // check if group and user exits.
            if (nodeExists(paramGroup) && nodeExists(paramUser)) {
                final long mGroupId = getRecentNodeKey(paramGroup);
                if (userGroupCheck(paramUser, paramGroup)) {
                    /*
                     * create new node for each join affected tree tail node and update revision, parent
                     * and child list.
                     */
                    // nodes that are affected by leave.
                    final List<Long> mTreePath = getTreePathNodes(mGroupId);

                    // old node id (key), new node id (value)
                    final Map<Long, Long> mNewSelIds =
                        new HashMap<Long, Long>();

                    // new node ids
                    final List<Long> mNewIdsList = new LinkedList<Long>();

                    // create new group nodes.
                    for (int i = 0; i < mTreePath.size(); i++) {
                        final KeySelector mOldSel =
                            mKeySelectorDb.getEntry(mTreePath.get(i));

                        int mNewRevision = mOldSel.getRevision() + 1;

                        final KeySelector mNewSel =
                            new KeySelector(mOldSel.getName(), mOldSel
                                .getParents(), mOldSel.getChilds(),
                                mNewRevision, mOldSel.getVersion(), mOldSel
                                    .getType());

                        mKeySelectorDb.putEntry(mNewSel);
                        mNewSelIds.put(mOldSel.getPrimaryKey(), mNewSel
                            .getPrimaryKey());
                        mNewIdsList.add(mNewSel.getPrimaryKey());

                    }
                    
                 // create new user node with new revision and new parent list (without leaving group id).
                    final KeySelector mOldUserSel =
                        mKeySelectorDb.getEntry(getRecentNodeKey(paramUser));

                    if (mOldUserSel.getParents().size() > 1) {
                        final LinkedList<Long> mUserParentList =
                            mOldUserSel.getParents();
                        mUserParentList.remove(getFirstNodeKey(paramGroup));

                        final KeySelector mNewUserSel =
                            new KeySelector(mOldUserSel.getName(),
                                mUserParentList, mOldUserSel.getChilds(),
                                mOldUserSel.getRevision() + 1, mOldUserSel
                                    .getVersion(), mOldUserSel.getType());

                        mKeySelectorDb.putEntry(mNewUserSel);
                        mNewSelIds.put(mOldUserSel.getPrimaryKey(), mNewUserSel
                            .getPrimaryKey());
                        mNewIdsList.add(mNewUserSel.getPrimaryKey());
                    } else {
                        // if user has no more children, completely remove it from key manager.
                        mKeyManagerDb.deleteEntry(paramUser);
                    }

                    // update parent and child list of each new node.
                    for (int i = 0; i < mNewIdsList.size(); i++) {
                        final KeySelector mSel =
                            mKeySelectorDb.getEntry(mNewIdsList.get(i));

                        final Long[] parArray =
                            new Long[mSel.getParents().size()];
                        mSel.getParents().toArray(parArray);

                        final Long[] childArray =
                            new Long[mSel.getChilds().size()];
                        mSel.getChilds().toArray(childArray);

                        for (int j = 0; j < parArray.length; j++) {
                            long lastKey =
                                getRecentNodeKey(mKeySelectorDb.getEntry(
                                    parArray[j]).getName());
                            mSel.removeParent(parArray[j]);
                            mSel.addParent(lastKey);
                        }

                        for (int j = 0; j < childArray.length; j++) {
                            long lastKey =
                                getRecentNodeKey(mKeySelectorDb.getEntry(
                                    childArray[j]).getName());
                            mSel.removeChild(childArray[j]);
                            mSel.addChild(lastKey);
                        }

                        if (mNewIdsList.get(i) == getRecentNodeKey(paramGroup)) {
                            if (mSel.getChilds().contains(
                                getRecentNodeKey(paramUser))) {
                                mSel.removeChild(getRecentNodeKey(paramUser));
                            }
                        }
                        mKeySelectorDb.putEntry(mSel);
                    }

                    

                    /*
                     * update each user key set into key manager.
                     */
                    final SortedMap<String, KeyManager> mManMap =
                        mKeyManagerDb.getEntries();
                    final Iterator mOuterIter = mManMap.keySet().iterator();

                    while (mOuterIter.hasNext()) { // iterate through all users.
                        final String mKeyUser = (String)mOuterIter.next();
                        final KeyManager mManager = mManMap.get(mKeyUser);

                        final Iterator mInnerIter =
                            mNewSelIds.keySet().iterator();
                        while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                            long mId = (Long)mInnerIter.next();
                            if (mKeyUser.equals(paramUser)) {
                                // add all new keys user gets since its remaining DAG.

                                final List<Long> mUserTreePath =
                                    getTreePathNodes(getRecentNodeKey(paramUser));
                                if (mUserTreePath.contains(mId)
                                    || mUserTreePath.contains(mNewSelIds
                                        .get(mId))) {
                                    mManager.addKey(mNewSelIds.get(mId));
                                }

                            } else if (mManager.getKeySet().contains(mId)) {
                                mManager.addKey(mNewSelIds.get(mId));
                            }
                        }

                        // remove all old keys from user's key manager it is losing through group leaving.
                        if (mKeyUser.equals(paramUser)) {
                            final Set<Long> mUserSet = mManager.getKeySet();

                            final Iterator mapIter =
                                mNewSelIds.keySet().iterator();
                            while (mapIter.hasNext()) {
                                final long mMapKey = (Long)mapIter.next();
                                if (mUserSet.contains(mMapKey)) {
                                    if (!mUserSet.contains(mNewSelIds
                                        .get(mMapKey))) {
                                        mManager.removeKey(mMapKey);

                                        final String nodeName =
                                            mKeySelectorDb.getEntry(mMapKey)
                                                .getName();
                                        final SortedMap<Long, KeySelector> mSelectorMap =
                                            mKeySelectorDb.getEntries();
                                        final Iterator mIter =
                                            mSelectorMap.keySet().iterator();
                                        while (mIter.hasNext()) {
                                            long mMapId = (Long)mIter.next();
                                            KeySelector mInnerSel =
                                                mSelectorMap.get(mMapId);
                                            if (mInnerSel.getName().equals(
                                                nodeName)) {
                                                mManager.removeKey(mMapId);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mKeyManagerDb.putEntry(mManager);
                    }

                    /*
                     * create and encrypt key trails for logged user.
                     */
                    final Map<Long, byte[]> mKeyTrails =
                        new HashMap<Long, byte[]>();

                    final KeyManager mKeyManager =
                        mKeyManagerDb.getEntry(mLoggedUser);

                    // mKeyManager is NULL, when logged user is not a member of any group
                    if (mKeyManager != null) {
                        final Set<Long> mUserKeySet = mKeyManager.getKeySet();

                        final Iterator mSetIter = mUserKeySet.iterator();
                        while (mSetIter.hasNext()) {
                            final long mMapId = (Long)mSetIter.next();
                            if (mNewIdsList.contains(mMapId)) {
                                final KeySelector mSel =
                                    mKeySelectorDb.getEntry(mMapId);
                                final List<Long> mChilds = mSel.getChilds();
                                for (int i = 0; i < mChilds.size(); i++) {
                                    if (mUserKeySet.contains(mChilds.get(i))) {
                                        final KeySelector mChildSel =
                                            mKeySelectorDb.getEntry(mChilds
                                                .get(i));
                                        final byte[] mChildSecretKey =
                                            mChildSel.getSecretKey();
                                        final byte[] mIdAsByteArray =
                                            NodeEncryption
                                                .longToByteArray(mMapId);
                                        final byte[] mEncryptedId =
                                            NodeEncryption
                                                .encrypt(mIdAsByteArray,
                                                    mChildSecretKey);
                                        mKeyTrails.put(mChilds.get(i),
                                            mEncryptedId);
                                    }
                                }
                            }
                        }
                    }

                    transmitKeyTrails(mKeyTrails);

                } else {
                    throw new TTEncryptionException("User " + paramUser
                        + " is not member of given group " + paramGroup + "!");
                }
            } else {
                throw new TTEncryptionException(
                    "Group and/or user do not exist!");
            }
        } catch (final TTEncryptionException mTTExp) {
            mTTExp.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Transmits the key to the client.
     * 
     * @param paramKeyTails
     *            map of key trails.
     */
    public void transmitKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        // if map has no values, the logged user key cache has to be removed
        // final Iterator iter = paramKeyTails.keySet().iterator();
        // System.out.println("KeyTrails of user " + mLoggedUser + ": ");
        // while (iter.hasNext()) {
        // long id = (Long)iter.next();
        // System.out.println(id + " - " + paramKeyTails.get(id));
        //
        // byte [] mChildSecretKey = mKeySelectorDb.getEntry(id).getSecretKey();
        // byte [] mDecryptedBytes = NodeEncryption.decrypt(paramKeyTails.get(id), mChildSecretKey);
        //
        // long key = NodeEncryption.byteArrayToLong(mDecryptedBytes);
        //
        // System.out.println("encrypted key: " + key);
        // }

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
        final SortedMap<Long, KeySelector> mSelMap =
            mKeySelectorDb.getEntries();
        final Iterator iter = mSelMap.keySet().iterator();
        while (iter.hasNext()) {
            final KeySelector mSelector = mSelMap.get(iter.next());
            if (mSelector.getName().equals(paramNodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if user is member of group.
     * 
     * @param paramUser
     *            user to be checked.
     * @param paramGroup
     *            group to be checked.
     * @return
     *         when user is member of group.
     */
    private boolean userGroupCheck(final String paramUser,
        final String paramGroup) {
        
        final List<Long> mTreePath = getTreePathNodes(getRecentNodeKey(paramGroup));
        
        for(int i=0; i< mTreePath.size(); i++){
            final KeySelector mGroupSelector =
              mKeySelectorDb.getEntry(mTreePath.get(i));
            
            if(mGroupSelector.getName().equals(paramGroup)){
                final List<Long> mChildList = mGroupSelector.getChilds();
                for(int j=0; j<mChildList.size(); j++){
                    final String mChildName = mKeySelectorDb.getEntry(mChildList.get(j)).getName();
                    if(mChildName.equals(paramUser)){
                        return true;
                    }
                } 
            }
        }
        return false;
    }

    /**
     * Returns first selector node id by given node name.
     * 
     * @param paramNodeName
     *            node name for what the id should found.
     * @return
     *         first found selector node id.
     */
    private long getFirstNodeKey(final String paramNodeName) {
        final SortedMap<Long, KeySelector> mSelMap =
            mKeySelectorDb.getEntries();
        final Iterator iter = mSelMap.keySet().iterator();

        while (iter.hasNext()) {
            final KeySelector mSelector = mSelMap.get(iter.next());
            if (mSelector.getName().equals(paramNodeName)) {
                return mSelector.getPrimaryKey();
            }
        }
        return -1;
    }

    /**
     * Returns last selector node id by given node name.
     * 
     * @param paramNodeName
     *            node name for what the id should found.
     * @return
     *         last found selector node id.
     */
    private long getRecentNodeKey(final String paramNodeName) {
        final SortedMap<Long, KeySelector> mSelMap =
            mKeySelectorDb.getEntries();
        final Iterator iter = mSelMap.keySet().iterator();
        long mNodeId = -1;
        while (iter.hasNext()) {
            final KeySelector mSelector = mSelMap.get(iter.next());
            if (mSelector.getName().equals(paramNodeName)) {
                mNodeId = mSelector.getPrimaryKey();
            }
        }
        return mNodeId;
    }

    /**
     * Returns all nodes lying on a nodes tree path.
     * 
     * @param mNodeId
     *            node it to find its leaf to root nodes.
     * @return
     *         list of node keys.
     */
    private List<Long> getTreePathNodes(final long mNodeId) {
        final List<Long> mTreePath = new LinkedList<Long>();

        long mGroupId = mNodeId;
        final String mGroupName = mKeySelectorDb.getEntry(mGroupId).getName();

        final SortedMap<Long, KeySelector> mGroupSelMap =
            mKeySelectorDb.getEntries();
        final Iterator mGroupIter = mGroupSelMap.keySet().iterator();
        while (mGroupIter.hasNext()) {
            final KeySelector mSelector = mGroupSelMap.get(mGroupIter.next());
            if (mSelector.getName().equals(mGroupName)) {
                mGroupId = mSelector.getPrimaryKey();
            }
        }

        mTreePath.add(mGroupId);

        for (int i = 0; i < mTreePath.size(); i++) {
            final List<Long> mParentList =
                mKeySelectorDb.getEntry(mTreePath.get(i)).getParents();
            if (mParentList.size() > 0) {
                for (long parentId : mParentList) {
                    long mParentId = parentId;
                    final String mNodeName =
                        mKeySelectorDb.getEntry(mParentId).getName();

                    final SortedMap<Long, KeySelector> mSelMap =
                        mKeySelectorDb.getEntries();
                    final Iterator iter = mSelMap.keySet().iterator();
                    while (iter.hasNext()) {
                        final KeySelector mSelector = mSelMap.get(iter.next());
                        if (mSelector.getName().equals(mNodeName)) {
                            mParentId = mSelector.getPrimaryKey();
                        }
                    }

                    if (!mTreePath.contains(mParentId)) {
                        mTreePath.add(mParentId);
                    }
                }
            }
        }
        return mTreePath;
    }

    /**
     * Initialize storage instances.
     */
    private void init() {
        mKeySelectorDb =
            EncryptionHandler.getInstance().getKeySelectorInstance();
        mKeyManagerDb = EncryptionHandler.getInstance().getKeyManagerInstance();
        mLoggedUser = EncryptionHandler.getInstance().getUser();
    }

}
