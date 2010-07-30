/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.io.berkeley;

import com.treetank.io.AbstractKey;
import com.treetank.io.ITTSource;

/**
 * Key for reference the data in the berkeley-db. The key is also the
 * soft-reference of the pages regarading the PageReference.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyKey extends AbstractKey {

    /**
     * Public constructor.
     * 
     * @param mInput
     *            base for the key (coming from the db)
     */
    public BerkeleyKey(final ITTSource mInput) {
        super(mInput.readLong());
    }

    /**
     * Public constructor.
     * 
     * @param mKey
     *            , coming from the application
     */
    public BerkeleyKey(final long mKey) {
        super(mKey);
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
     * Static method to get the key about the first reference of the Nodepages.
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

}
