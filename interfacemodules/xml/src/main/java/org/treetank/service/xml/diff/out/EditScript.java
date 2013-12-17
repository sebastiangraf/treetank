/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.treetank.service.xml.diff.out;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.treetank.api.IData;
import org.treetank.data.interfaces.ITreeData;
import org.treetank.service.xml.diff.Diff;
import org.treetank.service.xml.diff.DiffFactory.EDiff;

/**
 * Builds an edit script.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class EditScript implements Iterator<Diff>, Iterable<Diff> {

    /** Preserves the order of changes and is used to iterate over all changes. */
    private final List<Diff> mChanges;

    /** To do a lookup; we use node/object identities. */
    private final IdentityHashMap<ITreeData, Diff> mChangeByNode;

    /** Index in the {@link List} of {@link Diff}s. */
    private transient int mIndex;

    /**
     * Constructor.
     */
    public EditScript() {
        mChanges = new ArrayList<Diff>();
        mChangeByNode = new IdentityHashMap<ITreeData, Diff>();
        mIndex = 0;
    }

    /**
     * Calculates the size of the edit script. This can be used to estimate the
     * amicability of an algorithm.
     * 
     * @return number of changes
     */
    public int size() {
        return mChanges.size();
    }

    /**
     * Checks if the edit script is empty.
     * 
     * @return true if empty
     */
    public boolean isEmpty() {
        return mChanges.isEmpty();
    }

    /**
     * Checks if an item has been added(changed).
     * 
     * @param paramItem
     *            {@link IData} implementation
     * @return true if the changes {@link List} already contains the node, false
     *         otherwise
     */
    public boolean containsNode(final ITreeData paramItem) {
        assert paramItem != null;
        return mChangeByNode.containsKey(paramItem);
    }

    /**
     * Clears the edit script.
     */
    public void clear() {
        mChanges.clear();
        mChangeByNode.clear();
    }

    /**
     * Look up a change for the given item.
     * 
     * @param paramItem
     *            (not) changed node
     * @return the change assigned to the node or null
     */
    public Diff get(final ITreeData paramItem) {
        return mChangeByNode.get(paramItem);
    }

    /**
     * Adds a change to the edit script.
     * 
     * @param paramChange
     *            {@link Diff} reference
     * @return the change
     */
    public Diff add(final Diff paramChange) {
        assert paramChange != null;
        final ITreeData item =
            paramChange.getDiff() == EDiff.DELETED ? paramChange.getOldNode() : paramChange.getNewNode();
        if (mChangeByNode.containsKey(item)) {
            return paramChange;
        }

        mChanges.add(paramChange);
        return mChangeByNode.put(item, paramChange);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Diff> iterator() {
        return mChanges.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        if (mIndex < mChanges.size() - 1) {
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Diff next() {
        if (mIndex < mChanges.size()) {
            return mChanges.get(mIndex++);
        } else {
            throw new NoSuchElementException("No more elements in the change list!");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported!");
    }

}
