package com.treetank.cache;

import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.NodePage;
import com.treetank.page.PagePersistenter;

/**
 * <h1>NodePageContainer</h1> This class acts as a container for revisioned
 * {@link NodePage}s. Each {@link NodePage} is stored in a versioned manner. If
 * modifications occur, the versioned {@link NodePage}s are dereferenced and
 * reconstructed. Afterwards, this container is used to store a complete
 * {@link NodePage} as well as one for upcoming modifications.
 * 
 * Both {@link Nodepage}s can differ since the complete one is mainly used for
 * read access and the modifying one for write access (and therefore mostly lazy
 * dereferenced).
 * 
 * Since objects of this class are stored in a cache, the class has to be
 * serializable.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class NodePageContainer {

    private final NodePage mComplete;

    private final NodePage mModified;

    /**
     * Constructor with complete page and lazy instantiated modifying page.
     * 
     * @param complete
     *            to be used as a base for this container.
     */
    public NodePageContainer(final NodePage complete) {
        this(complete, new NodePage(complete.getNodePageKey(), complete
                .getRevision()));
    }

    /**
     * Constructor with both, complete and modifying page.
     * 
     * @param complete
     *            to be used as a base for this container
     * @param modifying
     *            to be used as a base for this container
     */
    public NodePageContainer(final NodePage complete, final NodePage modifying) {
        this.mComplete = complete;
        this.mModified = modifying;
    }

    /**
     * Getting the complete page.
     * 
     * @return the complete page
     */
    public NodePage getComplete() {
        return mComplete;
    }

    /**
     * Getting the modified page.
     * 
     * @return the modified page
     */
    public NodePage getModified() {
        return mModified;
    }

    /**
     * Serializing the container to the cache.
     * 
     * @param out
     *            for serialization
     */
    public void serialize(final TupleOutput out) {
        final TupleOutputSink sink = new TupleOutputSink(out);
        PagePersistenter.serializePage(sink, mComplete);
        PagePersistenter.serializePage(sink, mModified);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mComplete == null) ? 0 : mComplete.hashCode());
        result = prime * result
                + ((mModified == null) ? 0 : mModified.hashCode());
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
        NodePageContainer other = (NodePageContainer) obj;
        if (mComplete == null) {
            if (other.mComplete != null)
                return false;
        } else if (!mComplete.equals(other.mComplete))
            return false;
        else if (!mModified.equals(other.mModified))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder(
                "NodePageContainer has pagekey =");
        builder.append(mComplete.getNodePageKey());
        builder.append("\nComplete page: ");
        builder.append(mComplete.toString());
        builder.append("\nModified page: ");
        builder.append(mModified.toString());
        return builder.toString();

    }

}
