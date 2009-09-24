package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.AbstractKey;
import com.treetank.io.KeyFactory;
import com.treetank.io.berkeley.TupleInputSource;
import com.treetank.io.berkeley.TupleOutputSink;

public final class KeyBinding extends TupleBinding<AbstractKey> {

	@Override
	public AbstractKey entryToObject(TupleInput arg0) {
		return KeyFactory.createKey(new TupleInputSource(arg0));
	}

	@Override
	public void objectToEntry(AbstractKey arg0, TupleOutput arg1) {
		arg0.serialize(new TupleOutputSink(arg1));
	}
}
