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

package com.treetank.utils;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;

/**
 * <h1>ItemList</h1>
 * <p>
 * Data structure to store XPath items.
 * </p>
 * <p>
 * This structure is used for atomic values that are needed for the evaluation of a query. They can be results
 * of a query expression or be specified directly in the query e.g. as literals perform an arithmetic
 * operation or a comparison.
 * </p>
 * <p>
 * Since these items have to be distinguishable from nodes their key will be a negative long value (node key
 * is always a positive long value). This value is retrieved by negate their index in the internal data
 * structure.
 * </p>
 */
public final class ItemList implements IItemList {

    /**
     * Internal storage of items.
     */
    private final List<IItem> mList;

    /**
     * Constructor. Initializes the list.
     */
    public ItemList() {

        mList = new ArrayList<IItem>();
    }

    /**
     * {@inheritDoc}
     */
    public int addItem(final IItem mItem) {

        final int key = mList.size();
        mItem.setNodeKey(key);
        // TODO: +2 is necessary, because key -1 is the NULL_NODE
        final int itemKey = (key + 2) * (-1);
        mItem.setNodeKey(itemKey);

        mList.add(mItem);
        return itemKey;
    }

    /**
     * {@inheritDoc}
     */
    public IItem getItem(final long mKey) {

        assert mKey <= Integer.MAX_VALUE;

        int index = (int)mKey; // cast to integer, because the list only accepts
        // int

        if (index < 0) {
            index = index * (-1);
        }

        // TODO: This is necessary, because key -1 is the NULL_NODE
        index = index - 2;

        return mList.get(index);
    }

}
