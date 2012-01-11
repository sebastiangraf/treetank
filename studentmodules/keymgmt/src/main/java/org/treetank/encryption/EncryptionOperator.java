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

import org.treetank.encryption.benchmarking.KtsMeter;
import org.treetank.encryption.database.DAGDatabase;
import org.treetank.encryption.database.KeyDatabase;
import org.treetank.encryption.database.KeyManagerDatabase;
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

    private static long timeNeededBenchKts = 0;

    /** Marker for changed IDs. */
    private final LinkedList<Long> idsChanged = new LinkedList<Long>();

    private int nodesAffected = 0;

    /**
     * Adds nodes to the DAG.
     * 
     * @param parent
     *            root new node(s) should be attached.
     * @param descendants
     *            new node(s) array.
     * @throws TTEncryptionException
     */
    public void join(final String parent, final String[] descendants)
        throws TTEncryptionException {

        String user = "ALL";
        if (descendants.length > 0) {
            user = descendants[descendants.length - 1];
        }
        if (nodeExists(parent)) {

            final Queue<Long> queue = new LinkedList<Long>();
            final LinkedList<Long> visited = new LinkedList<Long>();

            long mStartKey = -1;
            if (nodeExists(user)) {
                mStartKey = getNodeKey(user);
            } else {
                mStartKey = getNodeKey(parent);
                // create key manager entry for new user.
                getManagerDB().putEntry(
                    new KeyManager(user, new HashSet<Long>()));
            }

            queue.add(mStartKey);
            visited.add(mStartKey);
            idsChanged.add(mStartKey);
            while (!queue.isEmpty()) {
                final Long[] parents =
                    getDAGDB().getEntry(queue.remove()).getParents().toArray(
                        new Long[0]);
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

            // create all new nodes
            long prevNode = -1;
            final long lastNodeRevChild = getNodeKey(descendants[0]);
            final long lastNodeRevParent = getNodeKey(parent);
            for (int i = descendants.length - 1; i >= 0; i--) {
                // check if node to be inserted already exits, if so, do only a connection
                if (descendants.length == 1 && lastNodeRevChild != -1) {
                    final DAGSelector mParentNode =
                        getDAGDB().getEntry(lastNodeRevParent);
                    mParentNode.addChild(lastNodeRevChild);
                    getDAGDB().putEntry(mParentNode);

                    final DAGSelector mChildNode =
                        getDAGDB().getEntry(lastNodeRevChild);
                    mChildNode.addParent(lastNodeRevParent);
                    getDAGDB().putEntry(mChildNode);

                } else {

                    final LinkedList<Long> childList = new LinkedList<Long>();
                    if (prevNode != -1) {
                        childList.add(prevNode);
                    }

                    final DAGSelector mNewNode =
                        new DAGSelector(descendants[i], new LinkedList<Long>(),
                            childList, 0, 0, NodeEncryption.generateSecretKey());

                    if (i == 0) {
                        final long parentKey = getNodeKey(parent);
                        mNewNode.addParent(parentKey);

                        final DAGSelector mPrevNode =
                            getDAGDB().getEntry(parentKey);
                        mPrevNode.addChild(mNewNode.getPrimaryKey());
                        getDAGDB().putEntry(mPrevNode);
                    }
                    idsChanged.add(mNewNode.getPrimaryKey());
                    getDAGDB().putEntry(mNewNode);

                    if (prevNode != -1) {
                        final DAGSelector mPrevNode =
                            getDAGDB().getEntry(prevNode);
                        final LinkedList<Long> prevNodeParents =
                            mPrevNode.getParents();
                        prevNodeParents.add(mNewNode.getPrimaryKey());
                        getDAGDB().putEntry(mPrevNode);
                    }

                    prevNode = mNewNode.getPrimaryKey();

                }
            }

            // find childs of all affected nodes
            final Long[] idsChangedArray = idsChanged.toArray(new Long[0]);
            final LinkedList<Long> affectedChilds = new LinkedList<Long>();
            for (int j = 0; j < idsChangedArray.length; j++) {
                if (getDAGDB().containsKey(idsChangedArray[j])) {
                    final DAGSelector mDAG =
                        getDAGDB().getEntry(idsChangedArray[j]);
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
            final LinkedList<DAGSelector> dagList =
                new LinkedList<DAGSelector>();
            for (int j = 0; j < idsChanged.size(); j++) {
                if (getDAGDB().containsKey(idsChanged.get(j))) {
                    final DAGSelector mNode =
                        getDAGDB().getEntry(idsChanged.get(j));

                    mNode.increaseRevision();
                    mNode.setSecretKey(NodeEncryption.generateSecretKey());
                    dagList.add(mNode);
                    getDAGDB().putEntry(mNode);
                }
            }

            // create and write new DAG revision to selector store
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            final LinkedList<KeySelector> keySels =
                new LinkedList<KeySelector>();
            for (int i = 0; i < dagList.size(); i++) {
                final DAGSelector mDAG = dagList.get(i);
                final KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG
                        .getChilds(), mDAG.getRevision(), mDAG.getVersion(),
                        mDAG.getSecretKey());
                mDAG.setRevSelKey(mSel.getPrimaryKey());
                // getDAGDB().putEntry(mDAG);
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());
            }

            // update selector key parents and childs
            updateSelectorRefs(keySels, newOldIds, affectedChilds);

            updateKeyManagerJoin(newOldIds, user);

            // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException("Join: Parent node does not exist!");
        }

    }

    /**
     * A helper method to create a new DAG node without revision.
     * 
     * @param parent
     *            parent of node to be inserted.
     * @param child
     *            child to be inserted.
     * 
     * @throws TTEncryptionException
     */
    public void singleJoin(final String[] parent, final String child)
        throws TTEncryptionException {

        final DAGSelector mNewNode;

        if (parent[0].equals("null")) {
            mNewNode =
                new DAGSelector(child, new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
        } else {
            mNewNode =
                new DAGSelector(child, new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
            for (int i = 0; i < parent.length; i++) {
                if (nodeExists(parent[i])) {

                    final long parentKey = getNodeKey(parent[i]);
                    mNewNode.addParent(parentKey);

                    final DAGSelector mPrevNode =
                        getDAGDB().getEntry(parentKey);

                    mPrevNode.addChild(mNewNode.getPrimaryKey());
                    getDAGDB().putEntry(mPrevNode);

                } else {
                    throw new TTEncryptionException(
                        "Single Join: Parent node does not exist!");
                }
            }
        }

        getDAGDB().putEntry(mNewNode);

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
    public void leave(final String child, String[] parents)
        throws TTEncryptionException {

        final long childKey = getNodeKey(child);
        final DAGSelector mDAGSel = getDAGDB().getEntry(childKey);
        if (!nodeExists(child) || mDAGSel.getChilds().size() == 0) {

            final String user = child;

            // check if all parent nodes exits and whether they are parents of child
            for (String aParent : parents) {
                if (!nodeExists(aParent)) {
                    throw new TTEncryptionException("Leave: Parent node "
                        + aParent + " does not exist!");
                }
            }

            // if no parents are given, child is deleted completely from DAG and from all its parent. So, get
            // parent by traversing the database.
            if (parents.length == 0) {
                final LinkedList<Long> parentList =
                    getDAGDB().getEntry(childKey).getParents();
                parents = new String[parentList.size()];
                for (int i = 0; i < parentList.size(); i++) {
                    final String parentName =
                        getDAGDB().getEntry(parentList.get(i)).getName();
                    parents[i] = parentName;
                }
            }

            final Queue<Long> queue = new LinkedList<Long>();
            final LinkedList<Long> visited = new LinkedList<Long>();

            queue.add(childKey);
            visited.add(childKey);
            while (!queue.isEmpty()) {
                final DAGSelector mDAG = getDAGDB().getEntry(queue.remove());
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
                    getDAGDB().getEntry(idsChangedArray[j]);
                final LinkedList<Long> childList = mDAG.getChilds();
                for (int i = 0; i < childList.size(); i++) {
                    if (!idsChanged.contains(childList.get(i))) {
                        idsChanged.add(childList.get(i));
                        affectedChilds.add(childList.get(i));
                    }
                }
            }

            // update version and secret material and node references
            final LinkedList<DAGSelector> dagList =
                new LinkedList<DAGSelector>();
            for (long idChanged : idsChanged) {
                final DAGSelector mNode = getDAGDB().getEntry(idChanged);
                mNode.increaseVersion();
                mNode.setSecretKey(NodeEncryption.generateSecretKey());

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

                getDAGDB().putEntry(mNode);

            }

            // remove node from DAG if node should be deleted from all its parents; if not, remove its
            // references from the corresponding parents
            final DAGSelector node = getDAGDB().getEntry(childKey);
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            if (node.getParents().size() == 0) {
                getDAGDB().deleteEntry(childKey);
                getManagerDB().deleteEntry(user);
                newOldIds.put(childKey, -1L);
            }

            final LinkedList<KeySelector> keySels =
                new LinkedList<KeySelector>();
            for (int i = 0; i < dagList.size(); i++) {
                final DAGSelector mDAG = dagList.get(i);

                final KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG
                        .getChilds(), mDAG.getRevision(), mDAG.getVersion(),
                        mDAG.getSecretKey());
                mDAG.setRevSelKey(mSel.getPrimaryKey());
                getDAGDB().putEntry(mDAG);
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());

            }

            // update selector key parents and childs
            updateSelectorRefs(keySels, newOldIds, affectedChilds);

            updateKeyManagerLeave(newOldIds, user);

            // // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException(
                "Leave: Node to be deleted does not exist or is not a leaf node!");
        }
    }

    /**
     * Updates all child and parents references of a node after an update.s
     * 
     * @param keySels
     * @param newOldIds
     * @param affectedChilds
     */
    private void updateSelectorRefs(final LinkedList<KeySelector> keySels,
        final Map<Long, Long> newOldIds, final LinkedList<Long> affectedChilds) {
        // update selector key parents and childs
        for (int i = 0; i < keySels.size(); i++) {
            final KeySelector aSel = keySels.get(i);
            final LinkedList<Long> replacedParents = new LinkedList<Long>();
            final LinkedList<Long> replacedChilds = new LinkedList<Long>();
            final Iterator<Long> mIter = newOldIds.keySet().iterator();
            while (mIter.hasNext()) {
                final long key = mIter.next();
                final long value = newOldIds.get(key);

                if (aSel.getParents().contains(key)
                    && !replacedParents.contains(key)) {
                    aSel.removeParent(key);
                    aSel.addParent(value);
                    replacedParents.add(value);

                }

                if (affectedChilds.contains(key)
                    && aSel.getPrimaryKey() == value) {
                    final Long[] childList =
                        aSel.getChilds().toArray(new Long[0]);
                    for (int j = 0; j < childList.length; j++) {

                        if (getDAGDB().containsKey(childList[j])) {
                            long mLastSelId =
                                getDAGDB().getEntry(childList[j])
                                    .getLastRevSelKey();

                            aSel.removeChild(childList[j]);

                            aSel.addChild(mLastSelId);
                            replacedChilds.add(mLastSelId);
                        }
                    }

                } else if (aSel.getChilds().contains(key)
                    && !replacedChilds.contains(key)) {
                    aSel.removeChild(key);
                    aSel.addChild(value);
                    replacedChilds.add(value);

                }
            }
            getKeyDB().putEntry(aSel);
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
        final SortedMap<Long, DAGSelector> mMap = getDAGDB().getEntries();
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
    private boolean userExists(final String userName) {
        return getManagerDB().containsEntry(userName);
    }

    /**
     * Returns node's DAG id of given node name.
     * 
     * @param nodeName
     *            name of node id should be found.
     * @return
     *         id of node.
     */
    private long getNodeKey(final String nodeName) {
        final SortedMap<Long, DAGSelector> mMap = getDAGDB().getEntries();
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
            if (getDAGDB().getEntry(childKey).getParents().contains(parentKey)) {
                return true;
            }
        }
        return false;

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
    private void updateKeyManagerJoin(final Map<Long, Long> paramMap,
        final String user) {

        final Iterator<String> mOuterIter =
            getManagerDB().getEntries().keySet().iterator();

        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                getManagerDB().getEntries().get(mKeyUser);

            final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                final long mId = (Long)mInnerIter.next();
                if (mKeyUser.equals(user)) {
                    mManager.addKey(paramMap.get(mId));
                } else if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));

                }
            }
            getManagerDB().putEntry(mManager);
        }
    }

    /**
     * Updates the key manager on a leave operation.
     * 
     * @param paramMap
     *            map containing all through leave effected nodes with old and new id.
     */
    private void updateKeyManagerLeave(final Map<Long, Long> paramMap,
        final String user) {

        final Iterator<String> mOuterIter =
            getManagerDB().getEntries().keySet().iterator();

        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                getManagerDB().getEntries().get(mKeyUser);

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
                        if (!mManager.getKeySet().contains(
                            paramMap.get(mMapKey))) {
                            mManager.removeKey(mMapKey);

                            final Iterator<Long> mIter =
                                getDAGDB().getEntries().keySet().iterator();
                            while (mIter.hasNext()) {
                                long mMapId = (Long)mIter.next();
                                final DAGSelector mInnerSel =
                                    EncryptionController.getInstance()
                                        .getDAGDb().getEntries().get(mMapId);
                                if (mInnerSel.getName()
                                    .equals(
                                        EncryptionController.getInstance()
                                            .getDAGDb().getEntry(mMapKey)
                                            .getName())) {
                                    mManager.removeKey(mMapId);
                                }
                            }
                        }
                    }
                }
            }
            getManagerDB().putEntry(mManager);
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
        final long benchTime = System.currentTimeMillis();
        int affectedNode = 0;

        final Map<Long, byte[]> mKeyTrails = new HashMap<Long, byte[]>();
        final KeyManager mKeyManager =
            getManagerDB().getEntry(new EncryptionController().getUser());

        // mKeyManager is NULL, when logged user is not a member of any group
        if (mKeyManager != null) {
            final Set<Long> mUserKeySet = mKeyManager.getKeySet();

            final Iterator<Long> mSetIter = mUserKeySet.iterator();

            while (mSetIter.hasNext()) {
                final long mMapId = (Long)mSetIter.next();

                if (paramList.contains(mMapId)
                    && getDAGDB().containsKey(mMapId)) {

                    final List<Long> mChilds =
                        getDAGDB().getEntry(mMapId).getChilds();
                    for (int i = 0; i < mChilds.size(); i++) {
                        if (mUserKeySet.contains(mChilds.get(i))) {
                            if (i == 0) {
                                affectedNode++;
                            }
                            final byte[] mEncryptedId =
                                createKeyTrail(mChilds.get(i), mMapId);
                            mKeyTrails.put(mChilds.get(i), mEncryptedId);
                        }
                    }
                }
            }
        }

        nodesAffected = affectedNode;
        timeNeededBenchKts = System.currentTimeMillis() - benchTime;
        KtsMeter.getInstance().count(mKeyTrails.size());
        return mKeyTrails;
    }

    /**
     * Creates a single key trails.
     * 
     * @param paramChild
     *            id of child.
     * @param paramMapKey
     *            map key of node which is encrypted.
     * @return
     *         encrypted node as byte array.
     */
    private byte[]
        createKeyTrail(final long paramChild, final long paramMapKey) {
        final byte[] mChildSecretKey =
            getDAGDB().getEntry(paramChild).getSecretKey();
        final byte[] mIdAsByteArray =
            NodeEncryption.longToByteArray(paramMapKey);
        return NodeEncryption.encrypt(mIdAsByteArray, mChildSecretKey);
    }

    /**
     * Returns time need to create key trails.
     * 
     * @return
     *         time in milliseconds.s
     */
    public long getKtsTime() {
        return timeNeededBenchKts;
    }

    /**
     * Returns DAG database instance.
     */
    private DAGDatabase getDAGDB() {
        return EncryptionController.getInstance().getDAGDb();
    }

    /**
     * Returns key database instance.
     */
    private KeyDatabase getKeyDB() {
        return EncryptionController.getInstance().getSelDb();
    }

    /**
     * Returns key manager database instance.
     */
    private KeyManagerDatabase getManagerDB() {
        return EncryptionController.getInstance().getManDb();
    }
    
    public int getNodes(){
        return nodesAffected;
    }

}
