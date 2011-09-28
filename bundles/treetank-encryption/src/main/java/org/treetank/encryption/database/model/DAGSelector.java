package org.treetank.encryption.database.model;

import java.util.LinkedList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import org.treetank.encryption.EncryptionController;

@Entity
public class DAGSelector {

    /**
     * DAG key and primary key of database.
     */
    @PrimaryKey
    private long mDAGKey;

    /**
     * Name of the node (group or user name).
     */
    private String mName;

    /**
     * List of parent nodes.
     */
    private LinkedList<Long> mParents;

    /**
     * List of child nodes.
     */
    private LinkedList<Long> mChilds;

    /**
     * Current revision of node.
     */
    private int mRevision;

    /**
     * Current version of node.
     */
    private int mVersion;

    /**
     * Last revision in key selector.
     */
    private long mLastRevSelectorKey;
    
    /**
     * Secret key using for data en-/decryption.
     */
    private byte[] mSecretKey;
    
    /**
     * Standard constructor.
     */
    public DAGSelector() {
        super();
    }

    /**
     * Constructor for building an new dag selector instance.
     * 
     * @param paramName
     *            node name.
     */
    public DAGSelector(final String paramName, final LinkedList<Long> paramPar,
        final LinkedList<Long> paramChild, final int paramRev,
        final int paramVer, byte[] mSecretKey) {
        this.mDAGKey = EncryptionController.getInstance().newDAGKey();

        this.mName = paramName;
        this.mParents = paramPar;
        this.mChilds = paramChild;
        this.mRevision = paramRev;
        this.mVersion = paramVer;
        this.mSecretKey = mSecretKey;
    }

    /**
     * Returns selector id.
     * 
     * @return
     *         selector id.
     */
    public final long getPrimaryKey() {
        return mDAGKey;
    }

    /**
     * Returns node name.
     * 
     * @return
     *         node name.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Returns a list of parent nodes for node.
     * 
     * @return
     *         set of parent nodes.
     */
    public final LinkedList<Long> getParents() {
        return mParents;
    }

    /**
     * Returns a list of child nodes for node.
     * 
     * @return
     *         set of child nodes.
     */
    public final LinkedList<Long> getChilds() {
        return mChilds;
    }

    /**
     * Adds a new parent node to the list.
     * 
     * @param paramParent
     *            parent to add to list.
     */
    public final void addParent(final long paramParent) {
        mParents.add(paramParent);
    }

    /**
     * Adds a new child node to the list.
     * 
     * @param paramParent
     *            parent to add to list.
     */
    public final void addChild(final long paramChild) {
        mChilds.add(paramChild);
    }

    /**
     * Removes a parent node from list.
     * 
     * @param paramParent
     *            parent node to remove.
     */
    public void removeParent(final long paramParent) {
        mParents.remove(paramParent);
    }

    /**
     * Removes a child node from list.
     * 
     * @param paramChild
     *            child node to remove.
     */
    public void removeChild(final long paramChild) {
        mChilds.remove(paramChild);
    }

    /**
     * Sets new primary key. Usually not needed.
     * 
     * @param paramKey
     *            new key for node.
     */
    public final void setPrimaryKey(final long paramKey) {
        this.mDAGKey = paramKey;
    }

    /**
     * Returns current revision of node.
     * 
     * @return
     *         node's revision.
     */
    public final int getRevision() {
        return mRevision;
    }

    /**
     * Increases node revision by 1.
     */
    public final void increaseRevision() {
        this.mRevision += 1;
    }

    /**
     * Returns current version of node.
     * 
     * @return
     *         node's version.
     */
    public final int getVersion() {
        return mVersion;
    }

    /**
     * Increases node version by 1.
     */
    public final void increaseVersion() {
        this.mVersion += 1;
    }
    
    /**
     * Returns secret key.
     * 
     * @return
     *         secret key.
     */
    public final byte[] getSecretKey() {
        return mSecretKey;
    }
    
    /**
     * Sets secret key.
     * 
     * @return
     *         secret key.
     */
    public final void setSecretKey(final byte[] mSecretKey) {
        this.mSecretKey = mSecretKey;
    }
    
    public final void setRevSelKey(final long mSelKey){
        this.mLastRevSelectorKey = mSelKey;
    }
    
    public final long getLastRevSelKey(){
        return mLastRevSelectorKey;
    }
}
