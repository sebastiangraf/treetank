package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;

public final class PageReferenceBinding extends
        TupleBinding<PageReference<AbstractPage>> {

    @Override
    public PageReference<AbstractPage> entryToObject(final TupleInput arg0) {
        return new PageReference<AbstractPage>(new TupleInputSource(arg0));
    }

    @Override
    public void objectToEntry(final PageReference<AbstractPage> arg0,
            final TupleOutput arg1) {
        arg0.serialize(new TupleOutputSink(arg1));

    }

}
