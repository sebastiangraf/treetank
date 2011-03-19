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
package org.treetank.diff;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * Implements {@link IDiffObservable}, which can be used for all classes, which implement the {@link IDiff}
 * interface.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiffObservable implements IDiffObservable {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(AbsDiffObservable.class));

    /** {@link IReadTransaction} on new revision. */
    transient IReadTransaction mNewRtx;

    /** {@link IReadTransaction} on old revision. */
    transient IReadTransaction mOldRtx;

    /** {@link Set} of observers, which want to be notified of the encountered differences. */
    private final Set<IDiffObserver> mDiffObservers;

    /**
     * Default constructor.
     */
    AbsDiffObservable() {
        mDiffObservers = new HashSet<IDiffObserver>();
    }

    /** {@inheritDoc} */
    @Override
    public final void fireDiff(final EDiff paramDiff, final IItem paramNewNode, final IItem paramOldNode,
        final DiffDepth paramDepth) {
        for (final IDiffObserver observer : mDiffObservers) {
            observer.diffListener(paramDiff, paramNewNode, paramOldNode, paramDepth);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void done() {
        try {
            mNewRtx.close();
            mOldRtx.close();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        
        for (final IDiffObserver observer : mDiffObservers) {
            observer.diffDone();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void addObserver(final IDiffObserver paramObserver) {
        mDiffObservers.add(paramObserver);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeObserver(final IDiffObserver paramObserver) {
        mDiffObservers.remove(paramObserver);
    }
}
