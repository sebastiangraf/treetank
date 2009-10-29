package com.treetank.io.file;

import com.treetank.io.AbstractKey;
import com.treetank.io.ITTSource;

/**
 * FileKey, storing the offset and the length. The key is used for the mapping
 * between PageReerence and Page.
 * 
 * @author Sebastian Graf, University of Konstnz
 * 
 */
public final class FileKey extends AbstractKey {

    /**
     * Constructor for {@link ITTSource}
     * 
     * @param in
     *            Source for Input
     */
    public FileKey(final ITTSource inSource) {
        super(inSource.readLong(), inSource.readLong());
    }

    /**
     * Constructor for direct data.
     * 
     * @param offset
     * @param length
     */
    public FileKey(final long offset, final long length) {
        super(offset, length);
    }

    /**
     * Getting the length of the file fragment.
     * 
     * @return the length of the file fragment
     */
    public int getLength() {
        return (int) super.getKeys()[1];
    }

    /**
     * Getting the offset of the file fragment
     * 
     * @return the offset
     */
    public long getOffset() {
        return super.getKeys()[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIdentifier() {
        return super.getKeys()[0];
    }

}
