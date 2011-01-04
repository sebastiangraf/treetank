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
package com.treetank.diff;

import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TTException;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class FullDiff extends AbsDiffObservable implements IDiff {
    
    /** Logger. */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(FullDiff.class));
    
    /**
     * Constructor.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of (sub)tree to check
     * @param paramNewRev
     *            new revision key
     * @param paramOldRev
     *            old revision key
     * @param paramObserver
     *            observes the kind of diff between two nodes
     */
    FullDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final IDiffObserver paramObserver) {
        assert paramDb != null;
        assert paramKey > -2;
        assert paramNewRev >= 0;
        assert paramOldRev >= 0;
        assert paramObserver != null;
        try {
            final IReadTransaction newRev = paramDb.getSession().beginReadTransaction(paramNewRev);
            final IReadTransaction oldRev = paramDb.getSession().beginReadTransaction(paramOldRev);
            newRev.moveTo(paramKey);
            oldRev.moveTo(paramKey);
            new Diff(paramDb, newRev, oldRev, this).evaluate();
        } catch (final TTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        addObserver(paramObserver);
    }

    /** {@inheritDoc} */
    @Override
    public EDiff diff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        // TODO Auto-generated method stub
        return null;
    }
}
