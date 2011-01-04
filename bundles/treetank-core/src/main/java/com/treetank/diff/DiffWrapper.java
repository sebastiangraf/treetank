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

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class DiffWrapper implements Runnable {

    private enum Diff {
        NONE {
            @Override
            void invoke(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
                final IReadTransaction paramSecondRtx) {
                throw new IllegalStateException(
                    "Before invoking or submitting the thread call one of the diff methods!");
            }
        },
        FULL {
            @Override
            void invoke(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
                final IReadTransaction paramSecondRtx) {
                new FullDiff(paramDb, paramFirstRtx, paramSecondRtx);
            }
        },

        STRUCTURAL {
            @Override
            void invoke(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
                final IReadTransaction paramSecondRtx) {
                new StructuralDiff(paramDb, paramFirstRtx, paramSecondRtx);
            }
        };

        abstract void invoke(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
            final IReadTransaction paramSecondRtx);
    }

    private transient Diff mDiff;

    private final IDatabase mDatabase;

    private final IReadTransaction mFirstRtx;

    private final IReadTransaction mSecondRtx;

    public DiffWrapper(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx) {
        mDatabase = paramDb;
        mFirstRtx = paramFirstRtx;
        mSecondRtx = paramSecondRtx;
        mDiff = Diff.NONE;
    }

    public void fullDiff() {
        mDiff = Diff.FULL;
    }

    public void structuralDiff() {
        mDiff = Diff.STRUCTURAL;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        mDiff.invoke(mDatabase, mFirstRtx, mSecondRtx);
    }

}
