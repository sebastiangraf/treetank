package org.treetank.encryption;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class represents the key selector model holding all data
 * for a node of the right tree consisting of group and user nodes.
 * 
 * @author Patrick Lang, University of Konstanz
 */
@Entity
public class KeySelector {

    /**
     * Selector key and primary key of database.
     */
    @PrimaryKey
    private long mKeyId;

    /**
     * Name of the node (group or user name).
     */
    private String mName;

    /**
     * Name of parent node.
     */
    private String mParent;

    /**
     * Current revision of node.
     */
    private int mRevision;

    /**
     * Current version of node.
     */
    private int mVersion;

    /**
     * Standard constructor.
     */
    public KeySelector() {
        super();
    }

    /**
     * Constructor for building an new key selector instance.
     * 
     * @param paramName
     *            node name.
     * @param paramParent
     *            parent node name.
     */
    public KeySelector(final String paramName, final String paramParent) {
        this.mKeyId = RightKey.getInstance().newSelectorKey();
        this.mName = paramName;
        this.mParent = paramParent;
        this.mRevision = 0;
        this.mVersion = 0;
    }

    /**
     * Returns selector id.
     * 
     * @return
     *         selector id.
     */
    public final long getKeyId() {
        return mKeyId;
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
     * Returns name of parent node.
     * 
     * @return
     *         name of parent node.
     */
    public final String getParent() {
        return mParent;
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

}
