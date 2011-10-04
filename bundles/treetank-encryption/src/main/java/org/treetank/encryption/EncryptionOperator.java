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
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;

import org.treetank.encryption.database.model.DAGSelector;
import org.treetank.encryption.database.model.KeyManager;
import org.treetank.encryption.database.model.KeySelector;
import org.treetank.encryption.utils.NodeEncryption;
import org.treetank.exception.TTEncryptionException;

/**
 * This class provides functions for DAG operations.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class EncryptionOperator {

	/** Marker for changed IDs.*/
    private final LinkedList<Long> idsChanged = new LinkedList<Long>();

    /**
     * Adds nodes to the DAG.
     * 
     * @param parent
     *            root new node(s) should be attached.
     * @param descendants
     *            new node(s) array.
     * @throws TTEncryptionException
     */
    public void join(final String parent, final String[] descendants) throws TTEncryptionException {

        String user = "ALL";
        if (descendants.length > 0) {
            user = descendants[descendants.length - 1];
        }
        if (nodeExists(parent)) {
            final long parentKey = getNodeKey(parent);

            // calculate all nodes that are affected by the join update
            // bottom up from parent
            Queue<Long> queue = new LinkedList<Long>();
            LinkedList<Long> visited = new LinkedList<Long>();
            queue.add(parentKey);
            visited.add(parentKey);
            idsChanged.add(parentKey);
            while (!queue.isEmpty()) {
                final DAGSelector mDAG = getDAGSelector(queue.remove());
                final LinkedList<Long> parents = mDAG.getParents();
                for (long aParent : parents) {
                    if (!visited.contains(aParent)) {
                        queue.add(aParent);
                        if (!idsChanged.contains(aParent)) {
                            idsChanged.add(aParent);
                        }
                        visited.add(aParent);
                    }
                }
            }

            if (nodeExists(user)) {
                // calculate all nodes that are affected by the join update
                // only when parent and child already exists
                // bottom up from child
                final long childKey = getNodeKey(user);
                queue = new LinkedList<Long>();
                visited = new LinkedList<Long>();
                queue.add(childKey);
                visited.add(childKey);
                idsChanged.add(childKey);
                while (!queue.isEmpty()) {
                    final DAGSelector mDAG = getDAGSelector(queue.remove());
                    final LinkedList<Long> parents = mDAG.getParents();
                    for (long aParent : parents) {
                        if (!visited.contains(aParent)) {
                            queue.add(aParent);
                            if (!idsChanged.contains(aParent)) {
                                idsChanged.add(aParent);
                            }
                            visited.add(aParent);
                        }
                    }
                }
            } else {
                // create key manager entry for new user.
                EncryptionController.getInstance().getManDb().putEntry(
                    new KeyManager(user, new HashSet<Long>()));
            }

            // create all new nodes
            long prevNode = -1;
            final long lastNodeRevChild = getNodeKey(descendants[0]);
            final long lastNodeRevParent = getNodeKey(parent);
            for (int i = descendants.length - 1; i >= 0; i--) {
                // check if node to be inserted already exits, if so, do only a connection
                if (descendants.length == 1 && lastNodeRevChild != -1) {
                    final DAGSelector mParentNode =
                        EncryptionController.getInstance().getDAGDb().getEntry(lastNodeRevParent);
                    mParentNode.addChild(lastNodeRevChild);
                    EncryptionController.getInstance().getDAGDb().putEntry(mParentNode);

                    final DAGSelector mChildNode =
                        EncryptionController.getInstance().getDAGDb().getEntry(lastNodeRevChild);
                    mChildNode.addParent(lastNodeRevParent);
                    EncryptionController.getInstance().getDAGDb().putEntry(mChildNode);

                } else {

                    final LinkedList<Long> parentList = new LinkedList<Long>();
                    final LinkedList<Long> childList = new LinkedList<Long>();

                    if (prevNode != -1) {
                        childList.add(prevNode);
                    }

                    final DAGSelector mNewNode =
                        new DAGSelector(descendants[i], parentList, childList, 0, 0, NodeEncryption
                            .generateSecretKey());

                    if (i == 0) {
                        mNewNode.addParent(parentKey);

                        final DAGSelector mPrevNode =
                            EncryptionController.getInstance().getDAGDb().getEntry(parentKey);
                        mPrevNode.addChild(mNewNode.getPrimaryKey());
                        EncryptionController.getInstance().getDAGDb().putEntry(mPrevNode);
                    }
                    idsChanged.add(mNewNode.getPrimaryKey());
                    EncryptionController.getInstance().getDAGDb().putEntry(mNewNode);

                    if (prevNode != -1) {
                        final DAGSelector mPrevNode =
                            EncryptionController.getInstance().getDAGDb().getEntry(prevNode);
                        final LinkedList<Long> prevNodeParents = mPrevNode.getParents();
                        prevNodeParents.add(mNewNode.getPrimaryKey());
                        EncryptionController.getInstance().getDAGDb().putEntry(mPrevNode);
                    }

                    prevNode = mNewNode.getPrimaryKey();

                }
            }

            // find childs of all affected nodes
            final Long[] idsChangedArray = idsChanged.toArray(new Long[0]);
            final LinkedList<Long> affectedChilds = new LinkedList<Long>();
            for (int j = 0; j < idsChangedArray.length; j++) {
                if (EncryptionController.getInstance().getDAGDb().containsKey(idsChangedArray[j])) {
                    final DAGSelector mDAG =
                        EncryptionController.getInstance().getDAGDb().getEntry(idsChangedArray[j]);
                    final LinkedList<Long> childList = mDAG.getChilds();
                    for (int i = 0; i < childList.size(); i++) {
                        if (!idsChanged.contains(childList.get(i))) {
                            idsChanged.add(childList.get(i));
                            affectedChilds.add(childList.get(i));
                        }
                    }
                }
            }

            // update revisions and secret material
            final LinkedList<DAGSelector> dagList = new LinkedList<DAGSelector>();
            for (int j = 0; j < idsChanged.size(); j++) {
                if (EncryptionController.getInstance().getDAGDb().containsKey(idsChanged.get(j))) {
                    final DAGSelector mNode =
                        EncryptionController.getInstance().getDAGDb().getEntry(idsChanged.get(j));

                    mNode.increaseRevision();
                    mNode.setSecretKey(NodeEncryption.generateSecretKey());
                    dagList.add(mNode);
                    EncryptionController.getInstance().getDAGDb().putEntry(mNode);
                }
            }

            // write new DAG revision to selector store
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            final LinkedList<KeySelector> keySels = new LinkedList<KeySelector>();
            for (int i = 0; i < dagList.size(); i++) {
                final DAGSelector mDAG = dagList.get(i);
                final KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG.getChilds(), mDAG.getRevision(),
                        mDAG.getVersion(), mDAG.getSecretKey());
                mDAG.setRevSelKey(mSel.getPrimaryKey());
                EncryptionController.getInstance().getDAGDb().putEntry(mDAG);
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());
            }

            // update selector key parents and childs
            for (int i = 0; i < keySels.size(); i++) {
                final KeySelector aSel = keySels.get(i);
                final LinkedList<Long> replacedParents = new LinkedList<Long>();
                final LinkedList<Long> replacedChilds = new LinkedList<Long>();
                final Iterator<Long> mIter = newOldIds.keySet().iterator();
                while (mIter.hasNext()) {
                    final long key = mIter.next();
                    final long value = newOldIds.get(key);

                    if (aSel.getParents().contains(key) && !replacedParents.contains(key)) {
                        aSel.removeParent(key);
                        aSel.addParent(value);
                        replacedParents.add(value);

                    }

                    if (affectedChilds.contains(key) && aSel.getPrimaryKey() == value) {
                        final Long[] childList = aSel.getChilds().toArray(new Long[0]);
                        for (int j = 0; j < childList.length; j++) {

                            if (EncryptionController.getInstance().getDAGDb().containsKey(childList[j])) {
                                long mLastSelId =
                                    EncryptionController.getInstance().getDAGDb().getEntry(childList[j])
                                        .getLastRevSelKey();

                                aSel.removeChild(childList[j]);

                                aSel.addChild(mLastSelId);
                                replacedChilds.add(mLastSelId);
                            }
                        }

                    }

                    else if (aSel.getChilds().contains(key) && !replacedChilds.contains(key)) {
                        aSel.removeChild(key);
                        aSel.addChild(value);
                        replacedChilds.add(value);

                    }
                }

                EncryptionController.getInstance().getSelDb().putEntry(aSel);
            }

            updateKeyManagerJoin(newOldIds, user);

            // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException("Join: Parent node does not exist!");
        }

    }

    /**
     * Remove nodes or edges from the DAG.
     * 
     * @param child
     *            node or node edge to remove.
     * @param parents
     *            parent node of child, connection should remove.
     * @throws TTEncryptionException
     */
    public void leave(final String child, String[] parents) throws TTEncryptionException {

        final long childKey = getNodeKey(child);
        final DAGSelector mDAGSel = getDAGSelector(childKey);
        if (!nodeExists(child) || mDAGSel.getChilds().size() == 0) {

            final String user = child;

            // check if all parent nodes exits and whether they are parents of child
            for (String aParent : parents) {
                if (!nodeExists(aParent)) {
                    throw new TTEncryptionException("Leave: Parent node " + aParent + " does not exist!");
                }
            }

            // if no parents are given, child is deleted completely from DAG and from all its parent. So, get
            // parent by traversing the database.
            if (parents.length == 0) {
                final LinkedList<Long> parentList =
                    EncryptionController.getInstance().getDAGDb().getEntry(childKey).getParents();
                parents = new String[parentList.size()];
                for (int i = 0; i < parentList.size(); i++) {
                    final String parentName =
                        EncryptionController.getInstance().getDAGDb().getEntry(parentList.get(i)).getName();
                    parents[i] = parentName;
                }
            }

            // calculate all nodes that are affected by the leave update
            final Queue<Long> queue = new LinkedList<Long>();
            final LinkedList<Long> visited = new LinkedList<Long>();

            // if (parents.length == 0) { // if parents length is 0, than remove child from all its parents
            queue.add(childKey);
            visited.add(childKey);
            while (!queue.isEmpty()) {
                final DAGSelector mDAG = getDAGSelector(queue.remove());
                final LinkedList<Long> parentList = mDAG.getParents();
                for (long aParent : parentList) {
                    if (!visited.contains(aParent)) {
                        queue.add(aParent);
                        if (!idsChanged.contains(aParent)) {
                            idsChanged.add(aParent);
                        }
                        visited.add(aParent);
                    }
                }
            }

            // find childs of all affected nodes
            final Long[] idsChangedArray = idsChanged.toArray(new Long[0]);
            final LinkedList<Long> affectedChilds = new LinkedList<Long>();
            for (int j = 0; j < idsChangedArray.length; j++) {
                final DAGSelector mDAG =
                    EncryptionController.getInstance().getDAGDb().getEntry(idsChangedArray[j]);
                final LinkedList<Long> childList = mDAG.getChilds();
                for (int i = 0; i < childList.size(); i++) {
                    if (!idsChanged.contains(childList.get(i))) {
                        idsChanged.add(childList.get(i));
                        affectedChilds.add(childList.get(i));
                    }
                }
            }

            // update version and secret material and node references
            LinkedList<DAGSelector> dagList = new LinkedList<DAGSelector>();
            for (long idChanged : idsChanged) {
                final DAGSelector mNode = EncryptionController.getInstance().getDAGDb().getEntry(idChanged);
                mNode.increaseVersion();
                mNode.setSecretKey(NodeEncryption.generateSecretKey());

                // delete all references which reference to deleted node
                // if (mNode.getParents().contains(childKey)) {
                // mNode.removeParent(childKey);
                //
                // }
                if (mNode.getChilds().contains(childKey)) {
                    int i = 0;
                    while (i < parents.length) {
                        if (parents[i].equals(mNode.getName())) {
                            mNode.removeChild(childKey);
                            break;
                        }
                        i++;
                    }
                }

                // delete the parent reference from node.
                if (mNode.getPrimaryKey() == childKey) {
                    for (int i = 0; i < parents.length; i++) {
                        long parentKey = getNodeKey(parents[i]);
                        if (mNode.getParents().contains(parentKey)) {
                            mNode.removeParent(parentKey);
                        }
                    }

                    if (mNode.getParents().size() > 0) {
                        dagList.add(mNode);
                    }
                } else {
                    dagList.add(mNode);
                }

                EncryptionController.getInstance().getDAGDb().putEntry(mNode);

            }

            // remove node from DAG if node should be deleted from all its parents; if not, remove its
            // references from the corresponding parents
            DAGSelector node = EncryptionController.getInstance().getDAGDb().getEntry(childKey);
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            if (node.getParents().size() == 0) {
                EncryptionController.getInstance().getDAGDb().deleteEntry(childKey);
                EncryptionController.getInstance().getManDb().deleteEntry(user);
                newOldIds.put(childKey, -1L);
            }

            final LinkedList<KeySelector> keySels = new LinkedList<KeySelector>();
            for (int i = 0; i < dagList.size(); i++) {
                final DAGSelector mDAG = dagList.get(i);

                final KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG.getChilds(), mDAG.getRevision(),
                        mDAG.getVersion(), mDAG.getSecretKey());
                mDAG.setRevSelKey(mSel.getPrimaryKey());
                EncryptionController.getInstance().getDAGDb().putEntry(mDAG);
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());

            }

            // update selector key parents and childs
            for (int i = 0; i < keySels.size(); i++) {
                final KeySelector aSel = keySels.get(i);
                final LinkedList<Long> replacedParents = new LinkedList<Long>();
                final LinkedList<Long> replacedChilds = new LinkedList<Long>();
                final Iterator<Long> mIter = newOldIds.keySet().iterator();
                while (mIter.hasNext()) {
                    final long key = mIter.next();
                    final long value = newOldIds.get(key);

                    if (aSel.getParents().contains(key) && !replacedParents.contains(key)) {
                        aSel.removeParent(key);
                        aSel.addParent(value);
                        replacedParents.add(value);

                    }

                    if (affectedChilds.contains(key) && aSel.getPrimaryKey() == value) {
                        final Long[] childList = aSel.getChilds().toArray(new Long[0]);
                        for (int j = 0; j < childList.length; j++) {

                            if (EncryptionController.getInstance().getDAGDb().containsKey(childList[j])) {
                                long mLastSelId =
                                    EncryptionController.getInstance().getDAGDb().getEntry(childList[j])
                                        .getLastRevSelKey();

                                aSel.removeChild(childList[j]);

                                aSel.addChild(mLastSelId);
                                replacedChilds.add(mLastSelId);
                            }
                        }

                    }

                    else if (aSel.getChilds().contains(key) && !replacedChilds.contains(key)) {
                        aSel.removeChild(key);
                        aSel.addChild(value);
                        replacedChilds.add(value);

                    }
                }

                EncryptionController.getInstance().getSelDb().putEntry(aSel);
            }

            updateKeyManagerLeave(newOldIds, user);

            // // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException("Leave: Node to be deleted does not exist or is not a leaf node!");
        }
    }

    /**
     * Checks if node exists in DAG.
     * 
     * @param nodeName
     *            node name to to be checked.
     * @return
     *         bool result.
     */
    public boolean nodeExists(final String nodeName) {
        final SortedMap<Long, DAGSelector> mMap = EncryptionController.getInstance().getDAGDb().getEntries();
        final Iterator<Long> iter = mMap.keySet().iterator();
        while (iter.hasNext()) {
            final DAGSelector mSelector = mMap.get(iter.next());
            if (mSelector.getName().equals(nodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if node exists in key manager.
     * 
     * @param userName
     *            user name to to be checked.
     * @return
     *         bool result.
     */
    public boolean userExists(final String userName) {
        return EncryptionController.getInstance().getManDb().containsEntry(userName);
    }

    /**
     * Returns node's DAG id of given node name.
     * 
     * @param nodeName
     *            name of node id should be found.
     * @return
     *         id of node.
     */
    public long getNodeKey(final String nodeName) {
        final SortedMap<Long, DAGSelector> mMap = EncryptionController.getInstance().getDAGDb().getEntries();
        final Iterator<Long> iter = mMap.keySet().iterator();
        long curNode = -1;
        while (iter.hasNext()) {
            final DAGSelector mSelector = mMap.get(iter.next());
            if (mSelector.getName().equals(nodeName)) {
                curNode = mSelector.getPrimaryKey();
            }
        }
        return curNode;
    }

    /**
     * Checks whether node is child of given parent node.
     * 
     * @param child
     *            child node.
     * @param parent
     *            parent node.
     * @return
     *         bool result.
     */
    public boolean checkMembership(final String child, final String parent) {
        final long parentKey = getNodeKey(parent);
        final long childKey = getNodeKey(child);
        if (nodeExists(child) && nodeExists(parent)) {
            if (EncryptionController.getInstance().getDAGDb().getEntry(childKey).getParents().contains(
                parentKey)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Returns DAGSelector instance of given id.
     * 
     * @param id
     *            id of DAGSelector instance.
     * @return
     *         DAGSelector instance.
     */
    public DAGSelector getDAGSelector(long id) {
        return EncryptionController.getInstance().getDAGDb().getEntry(id);
    }

    /**
     * Returns KeyManager instance of given id.
     * 
     * @param id
     *            id of KeyManager instance.
     * @return
     *         KeyManager instance.
     */
    public KeyManager getManager(String id) {
        return EncryptionController.getInstance().getManDb().getEntry(id);
    }

    /**
     * Transmits the key to the client.
     * 
     * @param paramKeyTails
     *            map of key trails.
     */
    private void transmitKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        new ClientHandler().decryptKeyTrails(paramKeyTails);
    }

    /**
     * Updates the key manager on a join operation.
     * 
     * @param paramMap
     *            map containing all through join effected nodes with old and new id.
     */
    private void updateKeyManagerJoin(final Map<Long, Long> paramMap, final String user) {

        // final KeyManager mManager =
        // EncryptionController.getInstance().getManDb().getEntry(
        // EncryptionController.getInstance().getUser());
        //
        // final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
        // while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
        // final long mId = (Long)mInnerIter.next();
        // mManager.addKey(paramMap.get(mId));
        // }
        // EncryptionController.getInstance().getManDb().putEntry(mManager);

        final Iterator<String> mOuterIter =
            EncryptionController.getInstance().getManDb().getEntries().keySet().iterator();

        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                EncryptionController.getInstance().getManDb().getEntries().get(mKeyUser);

            final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                final long mId = (Long)mInnerIter.next();
                if (mKeyUser.equals(user)) {
                    mManager.addKey(paramMap.get(mId));
                } else if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));

                }
            }
            EncryptionController.getInstance().getManDb().putEntry(mManager);
        }
    }

    /**
     * Updates the key manager on a leave operation.
     * 
     * @param paramMap
     *            map containing all through leave effected nodes with old and new id.
     */
    private void updateKeyManagerLeave(final Map<Long, Long> paramMap, final String user) {

        //
        // KeyManager mManager =
        // EncryptionController.getInstance().getManDb().getEntry(EncryptionController.getInstance().getUser());
        //
        // final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
        // while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
        // long mId = (Long)mInnerIter.next();
        //
        // mManager.addKey(paramMap.get(mId));
        //
        // }
        //
        // EncryptionController.getInstance().getManDb().putEntry(mManager);

        final Iterator<String> mOuterIter =
            EncryptionController.getInstance().getManDb().getEntries().keySet().iterator();

        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                EncryptionController.getInstance().getManDb().getEntries().get(mKeyUser);

            final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                long mId = (Long)mInnerIter.next();
                if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));
                }
            }

            // remove all old keys from user's key manager it is losing through group leaving.
            if (mKeyUser.equals(user)) {
                final Iterator<Long> mapIter = paramMap.keySet().iterator();
                while (mapIter.hasNext()) {
                    final long mMapKey = (Long)mapIter.next();
                    if (mManager.getKeySet().contains(mMapKey)) {
                        if (!mManager.getKeySet().contains(paramMap.get(mMapKey))) {
                            mManager.removeKey(mMapKey);

                            final Iterator<Long> mIter =
                                EncryptionController.getInstance().getDAGDb().getEntries().keySet()
                                    .iterator();
                            while (mIter.hasNext()) {
                                long mMapId = (Long)mIter.next();
                                final DAGSelector mInnerSel =
                                    EncryptionController.getInstance().getDAGDb().getEntries().get(mMapId);
                                if (mInnerSel.getName()
                                    .equals(
                                        EncryptionController.getInstance().getDAGDb().getEntry(mMapKey)
                                            .getName())) {
                                    mManager.removeKey(mMapId);
                                }
                            }
                        }
                    }
                }
            }
            EncryptionController.getInstance().getManDb().putEntry(mManager);
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
        final KeyManager mKeyManager = getManager(new EncryptionController().getUser());

        // mKeyManager is NULL, when logged user is not a member of any group
        if (mKeyManager != null) {
            final Set<Long> mUserKeySet = mKeyManager.getKeySet();

            final Iterator<Long> mSetIter = mUserKeySet.iterator();

            while (mSetIter.hasNext()) {
                final long mMapId = (Long)mSetIter.next();

                if (paramList.contains(mMapId)
                    && EncryptionController.getInstance().getDAGDb().containsKey(mMapId)) {

                    final List<Long> mChilds =
                        EncryptionController.getInstance().getDAGDb().getEntry(mMapId).getChilds();
                    for (int i = 0; i < mChilds.size(); i++) {
                        if (mUserKeySet.contains(mChilds.get(i))) {
                            final byte[] mChildSecretKey =
                                EncryptionController.getInstance().getDAGDb().getEntry(mChilds.get(i))
                                    .getSecretKey();
                            final byte[] mIdAsByteArray = NodeEncryption.longToByteArray(mMapId);
                            final byte[] mEncryptedId =
                                NodeEncryption.encrypt(mIdAsByteArray, mChildSecretKey);
                            mKeyTrails.put(mChilds.get(i), mEncryptedId);
                        }
                    }
                }
            }
        }

        return mKeyTrails;
    }

    public LinkedList<Long> getAffectedNodes() {
        return idsChanged;
    }

}
