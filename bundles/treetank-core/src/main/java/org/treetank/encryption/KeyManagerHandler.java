package org.treetank.encryption;

import java.util.HashMap;
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
     *            name of goup the user joins.
     * @throws TTEncryptionException
     *             Exception occurred during joining process.
     */
    public void joinGroup(final String paramUser, final String paramGroup)
        throws TTEncryptionException {
        init();

        try {
            // check if group exits.
            if (nodeExists(paramGroup)) {
                final long mGroupId = getNodeIdByName(paramGroup);
                // check if user exists; if so, check if
                // it is already member of group.
                if (nodeExists(paramUser)) {
                    final long mUserId = getNodeIdByName(paramUser);

                    boolean userGroupCheck = true;
                    final SortedMap<Long, KeySelector> mSelMap =
                        mKeySelectorDb.getEntries();
                    Iterator iter = mSelMap.keySet().iterator();

                    while (iter.hasNext()) {
                        final KeySelector mSelector = mSelMap.get(iter.next());
                        if (mSelector.getName().equals(paramUser)
                            && mSelector.getParents().contains(mGroupId)) {
                            userGroupCheck = false;
                        }
                    }
                    if (userGroupCheck) {

                        /*
                         * create new node for each join affected tree tail node and update version, parent
                         * and child list.
                         */

                        // changing tree path.
                        final List<Long> mTreePath = getTreePathNodes(mGroupId);

                        // old node id (key), new node id (value)
                        final Map<Long, Long> mNewSelIds =
                            new HashMap<Long, Long>();

                        long mNewGroupId = -1;

                        // create new group nodes with updated child list.
                        for (int i = 0; i < mTreePath.size(); i++) {
                            final KeySelector mOldSel =
                                mKeySelectorDb.getEntry(mTreePath.get(i));

                            int mNewVersion = mOldSel.getVersion() + 1;

                            final KeySelector mNewSel =
                                new KeySelector(mOldSel.getName(), mOldSel
                                    .getParents(), mOldSel.getChilds(), mOldSel
                                    .getRevision(), mNewVersion, mOldSel
                                    .getType());

                            mKeySelectorDb.putEntry(mNewSel);
                            mNewSelIds.put(mOldSel.getPrimaryKey(), mNewSel
                                .getPrimaryKey());

                            if (mOldSel.getPrimaryKey() == mGroupId) {
                                mNewGroupId = mNewSel.getPrimaryKey();
                            }
                        }

                        // update parent and child list of all new nodes.
                        for (int i = 0; i < mTreePath.size(); i++) {
                            final KeySelector mSel =
                                mKeySelectorDb.getEntry(mNewSelIds
                                    .get(mTreePath.get(i)));

                            final List<Long> mGroupParentList =
                                mSel.getParents();

                            final List<Long> mGroupChildList = mSel.getChilds();

                            final Iterator mapIter =
                                mNewSelIds.keySet().iterator();
                            while (mapIter.hasNext()) {
                                long mapKey = (Long)mapIter.next();
                                if (mGroupParentList.contains(mapKey)) {
                                    mSel.removeParent(mapKey);
                                    mSel.addParent(mNewSelIds.get(mapKey));
                                }
                                if (mGroupChildList.contains(mapKey)) {
                                    mSel.removeChild(mapKey);
                                    mSel.addChild(mNewSelIds.get(mapKey));
                                }
                            }
                            mKeySelectorDb.putEntry(mSel);
                        }

                        // create new user node with new version and parent and add its new id as child to the
                        // new node of itsjoining group.
                        final KeySelector mOldUserSel =
                            mKeySelectorDb.getEntry(mUserId);

                        final List<Long> mUserParentList =
                            mOldUserSel.getParents();
                        mUserParentList.add(mNewGroupId);

                        final KeySelector mNewUserSel =
                            new KeySelector(mOldUserSel.getName(),
                                mUserParentList, mOldUserSel.getChilds(),
                                mOldUserSel.getRevision(), mOldUserSel
                                    .getVersion() + 1, mOldUserSel.getType());

                        mKeySelectorDb.putEntry(mNewUserSel);
                        mNewSelIds.put(mOldUserSel.getPrimaryKey(), mNewUserSel
                            .getPrimaryKey());

                        final KeySelector mGroupSel =
                            mKeySelectorDb.getEntry(mNewGroupId);
                        mGroupSel.addChild(mNewUserSel.getPrimaryKey());
                        mKeySelectorDb.putEntry(mGroupSel);

                        /*
                         * update each user key set into key manager.
                         */
                        final SortedMap<String, KeyManager> mManMap =
                            mKeyManagerDb.getEntries();
                        final Iterator mOuterIter = mManMap.keySet().iterator();

                        while (mOuterIter.hasNext()) { // iterate through all users.
                            final String mKeyUser = (String)mOuterIter.next();
                            final KeyManager mManager =
                                mManMap.get(mKeyUser);

                            final Iterator mInnerIter =
                                mNewSelIds.keySet().iterator();
                            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                                long mId = (Long)mInnerIter.next();
                                if(mKeyUser.equals(paramUser)){
                                    mManager.addKey(mNewSelIds.get(mId));
                                }
                                else if (mManager.getKeySet().contains(mId)) {
                                    mManager.addKey(mNewSelIds.get(mId));
                                }
                            }
                            mKeyManagerDb.putEntry(mManager);
                        }

                        //
                        //
                        // // add group to user's parents
                        // final KeySelector mUserSel =
                        // mKeySelectorDb.getEntry(mUserId);
                        // mUserSel.addParent(mGroupId);
                        // mKeySelectorDb.putEntry(mUserSel);
                        //
                        // // add user to group node's childs
                        // final KeySelector mGroupSel =
                        // mKeySelectorDb.getEntry(mGroupId);
                        // mGroupSel.addChild(mUserId);
                        // mKeySelectorDb.putEntry(mGroupSel);
                        //
                        // // keys of node which are updated on the tree path
                        // final List<Long> mTreePath = getTreePathNodes(mUserId);
                        //
                        // final Map<Long, Long> mNewMatIds =
                        // new HashMap<Long, Long>();
                        //
                        // // increase version of all nodes which are lying on the changing DAG tree path by
                        // one
                        // // respectively and create new key material for each of it.
                        // for (int i = 0; i < mTreePath.size(); i++) {
                        // final KeySelector mNodeSelector =
                        // mKeySelectorDb.getEntry(mTreePath.get(i));
                        // mNodeSelector.increaseVersion();
                        // mKeySelectorDb.putEntry(mNodeSelector);
                        //
                        // final long newMatId =
                        // mKeyMaterialDb.putEntry(new KeyMaterial(
                        // mNodeSelector.getName(), mNodeSelector
                        // .getRevision(), mNodeSelector
                        // .getVersion(), mNodeSelector
                        // .getParents(), mNodeSelector
                        // .getChilds()));
                        // mNewMatIds.put(mNodeSelector.getPrimaryKey(),
                        // newMatId);
                        //
                        // }
                        //
                        // // find changing nodes that are related to logged user to encrypt its key trails.
                        // final List<Long> mTreePathUser =
                        // getTreePathNodes(getNodeIdByName(mLoggedUser));
                        // final List<Long> mCommonNodes = new LinkedList<Long>();
                        //
                        // for (int i = 0; i < mTreePath.size(); i++) {
                        // for (int j = 0; j < mTreePathUser.size(); j++) {
                        // if (mTreePath.get(i) == mTreePathUser.get(j)) {
                        // mCommonNodes.add(mTreePathUser.get(j));
                        // }
                        // }
                        // }
                        //
                        // // for nodes lying on DAG tree path encrypt them for logged user.
                        // final Map<Long, LinkedList<byte[]>> mKeyTrails =
                        // new HashMap<Long, LinkedList<byte[]>>();
                        //
                        // final LinkedList<byte[]> mDecryptTrail =
                        // new LinkedList<byte[]>();
                        //
                        // byte[] mSecretKeyChild = null;
                        // long mUserMatId = -1;
                        //
                        // for (int i = 0; i < mCommonNodes.size(); i++) {
                        // final KeyMaterial mNodeMaterial =
                        // mKeyMaterialDb.getEntry(mNewMatIds
                        // .get(mCommonNodes.get(i)));
                        // final List<Long> mChildList =
                        // mNodeMaterial.getChilds();
                        //
                        // for (long mChildId : mChildList) {
                        // if (mTreePathUser.contains(mChildId)) {
                        // long newChildId = mChildId;
                        // if (mNewMatIds.containsKey(mChildId)) {
                        // newChildId = mNewMatIds.get(mChildId);
                        // }
                        //
                        // final KeyMaterial mChildMaterial =
                        // mKeyMaterialDb.getEntry(newChildId);
                        //
                        // if (i == 0) {
                        // mUserMatId = newChildId;
                        // }
                        //
                        // mSecretKeyChild =
                        // mChildMaterial.getSecretKey();
                        //
                        // final byte[] mIdAsByteArray =
                        // NodeEncryption
                        // .longToByteArray(mNodeMaterial
                        // .getPrimaryKey());
                        //
                        // final byte[] mEncryptedId =
                        // NodeEncryption.encrypt(mIdAsByteArray,
                        // mSecretKeyChild);
                        // mDecryptTrail.add(mEncryptedId);
                        //
                        // }
                        // }
                        // mKeyTrails.put(mUserMatId, mDecryptTrail);

                        // }

                    } else {
                        throw new TTEncryptionException(
                            "User is already member of this group!");
                    }
                } else {
                    // users does not exist
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
        init();
    }

    public void transmitKeyTrails() {

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
     * Returns selector node id by given node name.
     * 
     * @param paramNodeName
     *            node name for what the id should found.
     * @return
     *         selector node id.
     */
    private long getNodeIdByName(final String paramNodeName) {
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
     * Returns all nodes lying on a nodes tree path.
     * 
     * @param mNodeId
     *            node it to find its leaf to root nodes.
     * @return
     *         list of node keys.
     */
    private List<Long> getTreePathNodes(final long mNodeId) {
        final List<Long> mTreePath = new LinkedList<Long>();
        mTreePath.add(mNodeId);

        for (int i = 0; i < mTreePath.size(); i++) {
            final List<Long> mParentList =
                mKeySelectorDb.getEntry(mTreePath.get(i)).getParents();
            if (mParentList.size() > 0) {
                for (long parentId : mParentList) {
                    if (!mTreePath.contains(parentId)) {
                        mTreePath.add(parentId);
                    }
                }
            }
        }
        return mTreePath;
    }

    private void init() {
        mKeySelectorDb =
            EncryptionHandler.getInstance().getKeySelectorInstance();
        mKeyManagerDb = EncryptionHandler.getInstance().getKeyManagerInstance();
        mLoggedUser = EncryptionHandler.getInstance().getUser();
    }

}
