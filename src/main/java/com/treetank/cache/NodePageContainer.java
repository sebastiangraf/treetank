package com.treetank.cache;

import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.NodePage;
import com.treetank.page.PagePersistenter;

public final class NodePageContainer {

    private final NodePage mComplete;

    private final NodePage mModified;

    public NodePageContainer(final NodePage complete) {
        this(complete, new NodePage(complete.getNodePageKey(), complete
                .getRevision()));
    }

    public NodePageContainer(final NodePage complete, final NodePage modifying) {
        this.mComplete = complete;
        this.mModified = modifying;
    }

    public NodePage getComplete() {
        return mComplete;
    }

    public NodePage getModified() {
        return mModified;
    }

    public void serialize(final TupleOutput out) {
        final TupleOutputSink sink = new TupleOutputSink(out);
        PagePersistenter.serializePage(sink, mComplete);
        PagePersistenter.serializePage(sink, mModified);
    }

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
