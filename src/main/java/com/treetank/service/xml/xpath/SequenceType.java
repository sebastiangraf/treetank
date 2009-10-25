/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: SequenceType.java 4245 2008-07-08 08:44:34Z scherer $
 */

package com.treetank.service.xml.xpath;

import com.treetank.api.IFilter;

/**
 * <h1>SequenceType</h1>
 * <p>
 * A sequence type defines a type a the items in a sequnce can have. It consists
 * of either an empty-sequence-test, or an ItemType(kind test, item() or atomic
 * value) and an optional wildcard (*, ?, +)
 * </p>
 */
public class SequenceType {

    private final boolean mIsEmptySequence;

    private final IFilter mFilter;

    private final boolean mHasWildcard;

    private final char mWildcard;

    /**
     * Constructor with no arguments means, the sequence type is the empty
     * sequence.
     */
    public SequenceType() {

        mIsEmptySequence = true;
        mHasWildcard = false;
        mWildcard = ' ';
        mFilter = null;
    }

    /**
     * Constructor. Sequence type is an ItemType.
     * 
     * @param filter
     *            item type filter
     */
    public SequenceType(final IFilter filter) {

        mIsEmptySequence = false;
        mFilter = filter;
        mHasWildcard = false;
        mWildcard = ' ';
    }

    /**
     * Constructor. Sequence type is an ItemType with an wildcard. <li>'ItemType
     * ?' means the sequence has zero or one items that are of the ItemType</li>
     * <li>'ItemType +' means the sequence one or more items that are of the
     * ItemType</li> <li>'ItemType *' means the sequence has zero or more items
     * that are of the ItemType</li>
     * 
     * @param filter
     *            item type filter
     * @param wildcard
     *            either '*', '?' or '+'
     */
    public SequenceType(final IFilter filter, final char wildcard) {

        mIsEmptySequence = false;
        mFilter = filter;
        mHasWildcard = true;
        mWildcard = wildcard;
    }

    /**
     * 
     * @return true, if sequence is the empty sequence
     */
    public boolean isEmptySequence() {

        return mIsEmptySequence;
    }

    /**
     * @return the ItemType test
     */
    public IFilter getFilter() {

        return mFilter;
    }

    /**
     * @return true, if a wildcard is present
     */
    public boolean hasWildcard() {

        return mHasWildcard;
    }

    /**
     * Returns the wildcard's char representation.
     * 
     * @return wildcard sign
     */
    public char getWildcard() {

        return mWildcard;
    }

}
