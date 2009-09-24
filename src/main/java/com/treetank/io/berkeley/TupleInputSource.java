package com.treetank.io.berkeley;

import com.sleepycat.bind.tuple.TupleInput;
import com.treetank.io.ITTSource;

public class TupleInputSource implements ITTSource {

	private final TupleInput input;

	public TupleInputSource(final TupleInput paramInput) {
		input = paramInput;
	}

	@Override
	public byte readByte() {

		return input.readByte();
	}

	@Override
	public long readLong() {

		return input.readLong();
	}

	@Override
	public int readInt() {

		return input.readInt();
	}

}
