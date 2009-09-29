package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageFactory;

/**
 * Binding for storing {@link AbstractPage} objects within the Berkeley DB.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class AbstractPageBinding extends TupleBinding<AbstractPage> {

    /**
     *{@inheritDoc}
     */
    @Override
    public AbstractPage entryToObject(final TupleInput arg0) {
        return PageFactory.createPage(new TupleInputSource(arg0));
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public void objectToEntry(final AbstractPage arg0, final TupleOutput arg1) {
        arg0.serialize(new TupleOutputSink(arg1));
    }

}
