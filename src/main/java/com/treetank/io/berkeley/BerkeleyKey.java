package com.treetank.io.berkeley;

import com.treetank.io.AbstractKey;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.io.KeyFactory;

public class BerkeleyKey extends AbstractKey {
	public BerkeleyKey(final ITTSource in) {
		super(in.readLong());
	}

	public BerkeleyKey(final long key) {
		super(key);
	}

	public static final BerkeleyKey getPropsKey() {
		return new BerkeleyKey(-3);
	}

	public static final BerkeleyKey getDataInfoKey() {
		return new BerkeleyKey(-2);
	}

	public static final BerkeleyKey getFirstRevKey() {
		return new BerkeleyKey(-1);
	}

	@Override
	public long getIdentifier() {

		return super.getKeys()[0];
	}

	public void serialize(final ITTSink out) {
		out.writeInt(KeyFactory.BERKELEYKIND);
		super.serialize(out);
	}
}
