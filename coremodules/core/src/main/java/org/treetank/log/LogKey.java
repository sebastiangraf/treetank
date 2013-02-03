package org.treetank.log;

/**
 * Container for Key-Entry in the log determining the level and the the sequence in the level.
 * Needed for the WriteTrx for getting inserting any modified pages in the right order since the page-key can
 * not be computed from the nodekeys due to the relative position of the nodes in the subtree of the related
 * RevisionRootPage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LogKey {

    /** Is this key referencing to the root level or to the node level. */
    private final boolean mRootLevel;

    /** Level Key. */
    private final long mLevel;

    /** Sequence Key. */
    private final long mSeq;

    /**
     * Constructor.
     * 
     * @param pLevel
     *            to be set.
     * @param pSeq
     *            to be set.
     */
    public LogKey(final boolean pRootLevel, final long pLevel, final long pSeq) {
        mRootLevel = pRootLevel;
        mLevel = pLevel;
        mSeq = pSeq;
    }

    /**
     * 
     * Getting the level key.
     * 
     * @return the mLevel
     */
    public long getLevel() {
        return mLevel;
    }

    /**
     * Getting the seq key.
     * 
     * @return the mSeq
     */
    public long getSeq() {
        return mSeq;
    }

    /**
     * Getter for mRootLevel.
     * 
     * @return the mRootLevel
     */
    public boolean isRootLevel() {
        return mRootLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogKey [mRootLevel=");
        builder.append(mRootLevel);
        builder.append(", mLevel=");
        builder.append(mLevel);
        builder.append(", mSeq=");
        builder.append(mSeq);
        builder.append("]");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(mLevel ^ (mLevel >>> 32));
        result = prime * result + (mRootLevel ? 1231 : 1237);
        result = prime * result + (int)(mSeq ^ (mSeq >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogKey other = (LogKey)obj;
        if (mLevel != other.mLevel)
            return false;
        if (mRootLevel != other.mRootLevel)
            return false;
        if (mSeq != other.mSeq)
            return false;
        return true;
    }

}
