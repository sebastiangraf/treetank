package com.treetank.io.berkeley;

import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.ITTSink;

public class TupleOutputSink implements ITTSink {

	private final TupleOutput output;

	public TupleOutputSink(final TupleOutput paramOutput) {
		this.output = paramOutput;
	}

	@Override
	public final void writeByte(final byte byteVal) {
		output.writeByte(byteVal);
	}

	@Override
	public final void writeLong(final long longVal) {
		output.writeLong(longVal);
	}

	@Override
	public void writeInt(int intVal) {
		output.writeInt(intVal);
	}

}
