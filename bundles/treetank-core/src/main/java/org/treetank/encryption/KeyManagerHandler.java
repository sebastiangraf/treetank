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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.exception.TTEncryptionException;

/**
 * This class handles all operations on KeyManager.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class KeyManagerHandler {
    /**
     * KeySelector database instance.
     */
    KeySelectorDatabase mKeySelectorDb;
    /**
     * KeyManager database instance.
     */
    KeyManagerDatabase mKeyManagerDb;
    /**
     * Current logged user.
     */
    String mLoggedUser;
    /**
     * User name who join or leave a group.
     */
    String mGroupUser;
    /**
     * Group name in which a user is joining or leaving.
     */
    String mGroupName;

    /**
     * Invoked when a new user is joining a group.
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
        mGroupUser = paramUser;
        mGroupName = paramGroup;
        try {
            // check if group exits.
            if (nodeExists(paramGroup)) {
                final long mGroupId = getRecentNodeKey(paramGroup);
                final boolean mUserExists = nodeExists(paramUser);

                // check if user exists and if is already member of group
                if (!userGroupCheck(paramUser, paramGroup)) {
                    // all nodes that are affected by leave.
                    final List<Long> mTreePath =
                        getTreePathNodes(getRecentNodeKey(paramGroup));

                    // map of old node id (key), new node id (value).
                    final Map<Long, Long> mNewSelIds =
                        new HashMap<Long, Long>();

                    // ids of all new created nodes.
                    final List<Long> mNewIdsList = new LinkedList<Long>();

                    // new id of joining group
                    long mNewGroupId = -1;

                    // create new node for each node affected by join and it's version.
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
                    updateParentsChilds(mNewIdsList);

                    // create new user node with new version, parent list and add it's new id as child to the
                    // new node of its joining group.
                    final KeySelector mNewUserSel;
                    if (mUserExists) {
                        final KeySelector mOldUserSel =
                            mKeySelectorDb
                                .getEntry(getRecentNodeKey(paramUser));

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

                    // update each user key set in key manager.
                    updateKeyManagerJoin(mNewSelIds);

                    // create and encrypt key trails for logged user.
                    final Map<Long, byte[]> mKeyTrails =
                        encryptKeyTrails(mNewIdsList);
                    transmitKeyTrails(mKeyTrails);

                } else {
                    throw new TTEncryptionException("User " + paramUser
                        + " is already member of given group " + paramGroup
                        + "!");
                }
            } else {
                throw new TTEncryptionException("Group " + paramGroup
                    + " does not exist!");
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
        mGroupUser = paramUser;
        mGroupName = paramGroup;
        try {
            // check if group and user exits.
            if (nodeExists(paramGroup) && nodeExists(paramUser)) {
                if (userGroupCheck(paramUser, paramGroup)) {
                    // all nodes that are affected by leave.
                    final List<Long> mTreePath =
                        getTreePathNodes(getRecentNodeKey(paramGroup));

                    // map of old node id (key), new node id (value).
                    final Map<Long, Long> mNewSelIds =
                        new HashMap<Long, Long>();

                    // ids of all new created nodes.
                    final List<Long> mNewIdsList = new LinkedList<Long>();

                    // create new node for each node affected by leave and it's revision.
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

                        long mId = -1;
                        final Iterator iter =
                            mKeySelectorDb.getEntries().keySet().iterator();
                        while (iter.hasNext()) {
                            final KeySelector mSelector =
                                mKeySelectorDb.getEntries().get(iter.next());
                            if (mSelector.getName().equals(paramGroup)) {
                                mId = mSelector.getPrimaryKey();
                                break;
                            }
                        }
                        mUserParentList.remove(mId);

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
                    updateParentsChilds(mNewIdsList);

                    // update each user key set into key manager.
                    updateKeyManagerLeave(mNewSelIds);

                    // create and encrypt key trails for logged user.
                    final Map<Long, byte[]> mKeyTrails =
                        encryptKeyTrails(mNewIdsList);
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
     * Creates and encrypts key trails.
     * 
     * @param paramList
     *            id list of all node which are affected by update.
     * @return
     *         key trails map.
     */
    private Map<Long, byte[]> encryptKeyTrails(final List<Long> paramList) {
        final Map<Long, byte[]> mKeyTrails = new HashMap<Long, byte[]>();
        final KeyManager mKeyManager = mKeyManagerDb.getEntry(mLoggedUser);

        // mKeyManager is NULL, when logged user is not a member of any group
        if (mKeyManager != null) {
            final Set<Long> mUserKeySet = mKeyManager.getKeySet();

            final Iterator mSetIter = mUserKeySet.iterator();
            while (mSetIter.hasNext()) {
                final long mMapId = (Long)mSetIter.next();
                if (paramList.contains(mMapId)) {
                    final List<Long> mChilds =
                        mKeySelectorDb.getEntry(mMapId).getChilds();
                    for (int i = 0; i < mChilds.size(); i++) {
                        if (mUserKeySet.contains(mChilds.get(i))) {
                            final byte[] mChildSecretKey =
                                mKeySelectorDb.getEntry(mChilds.get(i))
                                    .getSecretKey();
                            final byte[] mIdAsByteArray =
                                NodeEncryption.longToByteArray(mMapId);
                            final byte[] mEncryptedId =
                                NodeEncryption.encrypt(mIdAsByteArray,
                                    mChildSecretKey);
                            mKeyTrails.put(mChilds.get(i), mEncryptedId);
                        }
                    }
                }
            }
        }

        return mKeyTrails;
    }

    /**
     * Updates parent and children of all nodes.
     * 
     * @param paramList
     *            id list of all node which are affected by update
     */
    private void updateParentsChilds(final List<Long> paramList) {
        for (int i = 0; i < paramList.size(); i++) {
            final KeySelector mSel = mKeySelectorDb.getEntry(paramList.get(i));

            final Long[] parArray = new Long[mSel.getParents().size()];
            mSel.getParents().toArray(parArray);

            for (int j = 0; j < parArray.length; j++) {
                long lastKey =
                    getRecentNodeKey(mKeySelectorDb.getEntry(parArray[j])
                        .getName());
                mSel.removeParent(parArray[j]);
                mSel.addParent(lastKey);
            }

            final Long[] childArray = new Long[mSel.getChilds().size()];
            mSel.getChilds().toArray(childArray);

            for (int j = 0; j < childArray.length; j++) {
                long lastKey =
                    getRecentNodeKey(mKeySelectorDb.getEntry(childArray[j])
                        .getName());
                mSel.removeChild(childArray[j]);
                mSel.addChild(lastKey);
            }

            if (paramList.get(i) == getRecentNodeKey(mGroupName)) {
                if (mSel.getChilds().contains(getRecentNodeKey(mGroupUser))) {
                    mSel.removeChild(getRecentNodeKey(mGroupUser));
                }
            }
            mKeySelectorDb.putEntry(mSel);
        }
    }

    /**
     * Updates the key manager on a join operation.
     * 
     * @param paramMap
     *            map containing all through join effected nodes with old and new id.
     */
    public void updateKeyManagerJoin(final Map<Long, Long> paramMap) {
        final Iterator mOuterIter =
            mKeyManagerDb.getEntries().keySet().iterator();
        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                mKeyManagerDb.getEntries().get(mKeyUser);

            final Iterator mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                final long mId = (Long)mInnerIter.next();
                if (mKeyUser.equals(mGroupUser)) {
                    mManager.addKey(paramMap.get(mId));
                } else if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));
                }
            }
            mKeyManagerDb.putEntry(mManager);
        }
    }

    /**
     * Updates the key manager on a leave operation.
     * 
     * @param paramMap
     *            map containing all through leave effected nodes with old and new id.
     */
    public void updateKeyManagerLeave(final Map<Long, Long> paramMap) {
        final Iterator mOuterIter =
            mKeyManagerDb.getEntries().keySet().iterator();
        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                mKeyManagerDb.getEntries().get(mKeyUser);

            final Iterator mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                long mId = (Long)mInnerIter.next();
                if (mKeyUser.equals(mGroupUser)) {
                    // add all new keys user gets since its remaining DAG.
                    final List<Long> mUserTreePath =
                        getTreePathNodes(getRecentNodeKey(mGroupUser));
                    if (mUserTreePath.contains(mId)
                        || mUserTreePath.contains(paramMap.get(mId))) {
                        mManager.addKey(paramMap.get(mId));
                    }
                } else if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));
                }
            }

            // remove all old keys from user's key manager it is losing through group leaving.
            if (mKeyUser.equals(mGroupUser)) {
                final Iterator mapIter = paramMap.keySet().iterator();
                while (mapIter.hasNext()) {
                    final long mMapKey = (Long)mapIter.next();
                    if (mManager.getKeySet().contains(mMapKey)) {
                        if (!mManager.getKeySet().contains(
                            paramMap.get(mMapKey))) {
                            mManager.removeKey(mMapKey);

                            final Iterator mIter =
                                mKeySelectorDb.getEntries().keySet().iterator();
                            while (mIter.hasNext()) {
                                long mMapId = (Long)mIter.next();
                                final KeySelector mInnerSel =
                                    mKeySelectorDb.getEntries().get(mMapId);
                                if (mInnerSel.getName().equals(
                                    mKeySelectorDb.getEntry(mMapKey).getName())) {
                                    mManager.removeKey(mMapId);
                                }
                            }
                        }
                    }
                }
            }
            mKeyManagerDb.putEntry(mManager);
        }
    }

    /**
     * Transmits the key to the client.
     * 
     * @param paramKeyTails
     *            map of key trails.
     */
    private void transmitKeyTrails(final Map<Long, byte[]> paramKeyTails) {
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
        final Iterator iter = mKeySelectorDb.getEntries().keySet().iterator();
        while (iter.hasNext()) {
            final KeySelector mSelector =
                mKeySelectorDb.getEntries().get(iter.next());
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
        final List<Long> mTreePath =
            getTreePathNodes(getRecentNodeKey(paramGroup));

        for (int i = 0; i < mTreePath.size(); i++) {
            final KeySelector mGroupSelector =
                mKeySelectorDb.getEntry(mTreePath.get(i));
            if (mGroupSelector.getName().equals(paramGroup)) {
                final List<Long> mChildList = mGroupSelector.getChilds();
                for (int j = 0; j < mChildList.size(); j++) {
                    final String mChildName =
                        mKeySelectorDb.getEntry(mChildList.get(j)).getName();
                    if (mChildName.equals(paramUser)) {
                        return true;
                    }
                }
            }
        }
        return false;
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

        final Iterator mGroupIter =
            mKeySelectorDb.getEntries().keySet().iterator();
        while (mGroupIter.hasNext()) {
            final KeySelector mSelector =
                mKeySelectorDb.getEntries().get(mGroupIter.next());
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
                    final Iterator iter =
                        mKeySelectorDb.getEntries().keySet().iterator();
                    while (iter.hasNext()) {
                        final KeySelector mSelector =
                            mKeySelectorDb.getEntries().get(iter.next());
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
