package com.treetank.io.berkeley;

import com.treetank.io.AbstractKey;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.io.KeyFactory;

/**
 * Key for reference the data in the berkeley-db. The key is also the
 * soft-reference of the pages regarading the PageReference.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyKey extends AbstractKey {

    /**
     * Public constructor
     * 
     * @param input
     *            base for the key (coming from the db)
     */
    public BerkeleyKey(final ITTSource input) {
        super(input.readLong());
    }

    /**
     * Public constructor.
     * 
     * @param key
     *            , coming from the application
     */
    public BerkeleyKey(final long key) {
        super(key);
    }

    /**
     * Static method to get the key for the <code>StorageProperties</code>.
     * 
     * @return the key for the
     */
    public static final BerkeleyKey getPropsKey() {
        return new BerkeleyKey(-3);
    }

    /**
     * Static method to get the key about the information about the last
     * nodepagekey given.
     * 
     * @return the key for the last nodepage key
     */
    public static final BerkeleyKey getDataInfoKey() {
        return new BerkeleyKey(-2);
    }

    /**
     * Static method to get the key about the first reference of the Nodepages
     * 
     * @return the key for the first nodepage
     */
    public static final BerkeleyKey getFirstRevKey() {
        return new BerkeleyKey(-1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIdentifier() {

        return super.getKeys()[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        out.writeInt(KeyFactory.BERKELEYKIND);
        super.serialize(out);
    }
}
