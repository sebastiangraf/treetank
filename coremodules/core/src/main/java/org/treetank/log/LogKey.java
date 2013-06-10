package org.treetank.log;

import static com.google.common.base.Objects.toStringHelper;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

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
     * @param pRootLevel
     * 
     * 
     *            is key part of the revision-part or the node-part
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mRootLevel", mRootLevel).add("mLevel", mLevel).add("mSeq", mSeq)
            .toString();
    }

    /**
     * Binding for serializing LogKeys in the BDB.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    static class LogKeyBinding extends TupleBinding<LogKey> {

        /**
         * {@inheritDoc}
         */
        @Override
        public LogKey entryToObject(TupleInput arg0) {
            final LogKey key = new LogKey(arg0.readBoolean(), arg0.readLong(), arg0.readLong());
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void objectToEntry(LogKey arg0, TupleOutput arg1) {
            arg1.writeBoolean(arg0.isRootLevel());
            arg1.writeLong(arg0.getLevel());
            arg1.writeLong(arg0.getSeq());
        }

    }

}
