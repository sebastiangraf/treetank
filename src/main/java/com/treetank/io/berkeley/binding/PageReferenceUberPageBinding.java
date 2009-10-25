package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.PageReference;

/**
 * Binding for the PageReference of the UberPage
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class PageReferenceUberPageBinding extends
        TupleBinding<PageReference> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference entryToObject(final TupleInput arg0) {
        return new PageReference(new TupleInputSource(arg0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectToEntry(final PageReference arg0, final TupleOutput arg1) {
        arg0.serialize(new TupleOutputSink(arg1));

    }

}
