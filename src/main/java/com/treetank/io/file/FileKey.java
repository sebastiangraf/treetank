package com.treetank.io.file;

import com.treetank.io.AbstractKey;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.io.KeyFactory;

/**
 * FileKey, storing the offset and the length. The key is used for the mapping
 * between PageReerence and Page.
 * 
 * @author Sebastian Graf, University of Konstnz
 * 
 */
public class FileKey extends AbstractKey {

	/**
	 * Constructor
	 * 
	 * @param in
	 */
	public FileKey(final ITTSource in) {
		super(in.readLong(), in.readLong());
	}

	public FileKey(final long offset, final long length) {
		super(offset, length);
	}

	public final int getLength() {
		return (int) super.getKeys()[1];
	}

	public final long getOffset() {
		return super.getKeys()[0];
	}

	@Override
	public long getIdentifier() {
		return super.getKeys()[0];
	}

	public void serialize(final ITTSink out) {
		out.writeInt(KeyFactory.FILEKIND);
		super.serialize(out);
	}

}
