package com.treetank.io.berkeley.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.io.StorageProperties;

public final class StoragePropTupleBinding extends
		TupleBinding<StorageProperties> {

	@Override
	public StorageProperties entryToObject(final TupleInput arg0) {

		final long versionMajor = arg0.readLong();
		final long versionMinor = arg0.readLong();
		final boolean checksummed = arg0.readBoolean();
		final boolean encrypted = arg0.readBoolean();
		return new StorageProperties(versionMajor, versionMinor, checksummed,
				encrypted);
	}

	@Override
	public void objectToEntry(final StorageProperties arg0,
			final TupleOutput arg1) {

		arg1.writeLong(arg0.getVersionMajor());
		arg1.writeLong(arg0.getVersionMinor());
		arg1.writeBoolean(arg0.getChecksummed());
		arg1.writeBoolean(arg0.getEncrypted());
	}
}
