package org.treetank.encryption;

import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class represents the secret material model holding
 * all data for KEKs(key encryption keys) and TEKs
 * (traffic encryption keys) with its selector key and its
 * revision and version respectively.
 * 
 * @author Patrick Lang, University of Konstanz
 */
@Entity
public class KeyMaterial {

    /**
     * Unique material key and primary key for database.
     */
    @PrimaryKey
    private long mMaterialKey;

    /**
     * Revision of keying material.
     */
    private int mRevsion;

    /**
     * Version of keying material.
     */
    private int mVersion;

    /**
     * List of parent nodes.
     */
    private List<Long> mParents;

    /**
     * List of child nodes.
     */
    private List<Long> mChilds;

    /**
     * Type of node (group or user).
     */
    private EntityType mType;

    /**
     * Secret key using for data en-/decryption.
     */
    private byte[] mSecretKey;

    /**
     * Standard constructor.
     */
    public KeyMaterial() {
        super();
    }

    /**
     * Constructor for building a new keying material instance.
     * 
     * @param paramKey
     *            selector key of keying material.
     * @param paramRev
     *            revision of keying material.
     * @param paramVer
     *            version of keying material.
     * @param paramSKey
     *            secret key of keying material.
     */
    public KeyMaterial(final int paramRev, final int paramVer,
        final List<Long> paramPar, final List<Long> paramChilds,
        final EntityType paramType) {
        this.mMaterialKey = PrimaryKeyGenerator.getInstance().newMaterialKey();
        this.mRevsion = paramRev;
        this.mVersion = paramVer;
        this.mParents = paramPar;
        this.mChilds = paramChilds;
        this.mType = paramType;
        this.mSecretKey = new NodeEncryption().generateSecretKey();
    }

    /**
     * Returns unique material key.
     * 
     * @return
     *         material key.
     */
    public final long getPrimaryKey() {
        return mMaterialKey;
    }

    /**
     * Returns revision.
     * 
     * @return
     *         revision.
     */
    public final int getRevsion() {
        return mRevsion;
    }

    /**
     * Returns version.
     * 
     * @return
     *         version.
     */
    public final int getVersion() {
        return mVersion;
    }

    /**
     * Returns a list of parent nodes for node.
     * 
     * @return
     *         set of parent nodes.
     */
    public final List<Long> getParents() {
        return mParents;
    }

    /**
     * Returns a list of child nodes for node.
     * 
     * @return
     *         set of child nodes.
     */
    public final List<Long> getChilds() {
        return mChilds;
    }

    /**
     * Returns type of entity.
     */
    public EntityType getType() {
        return mType;
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

}
