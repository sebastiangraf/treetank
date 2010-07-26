package com.treetank.cache;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.ITTSource;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.page.NodePage;
import com.treetank.page.PagePersistenter;

public class NodePageContainerBinding extends TupleBinding<NodePageContainer> {

    public NodePageContainerBinding() {
    }

    @Override
    public NodePageContainer entryToObject(final TupleInput arg0) {
        final ITTSource source = new TupleInputSource(arg0);
        final NodePage current = (NodePage)PagePersistenter.createPage(source);
        final NodePage modified = (NodePage)PagePersistenter.createPage(source);
        return new NodePageContainer(current, modified);
    }

    @Override
    public void objectToEntry(final NodePageContainer arg0, final TupleOutput arg1) {
        arg0.serialize(arg1);
    }
}
