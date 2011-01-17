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

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import com.treetank.TestHelper;
import com.treetank.api.IDatabase;
import com.treetank.exception.TTException;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class StructuralDiffTest implements IDiffObserver {
    private transient IDatabase mDatabase;

    private transient EDiff mDiff;
    
    private transient int mCounter;

    @Before
    public final void setUp() throws TTException {
        TestHelper.deleteEverything();
        mDatabase = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        DocumentCreater.createVersioned(mDatabase.getSession().beginWriteTransaction());
        mDiff = EDiff.SAME;
    }

    @After
    public final void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public final void testStructuralDiff() {
        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(this);
        DiffFactory.invokeStructuralDiff(mDatabase, 0, 1, 0, EDiffKind.NORMAL, observer);
        
        while (mDiff != EDiff.DONE) {
            switch (mCounter) {
            case 1:
                assertEquals(mDiff, EDiff.INSERTED);
                break;
            case 2:
                assertEquals(mDiff, EDiff.INSERTED);
                break;     
            default:
                assertEquals(mDiff, EDiff.SAME);
                break;
            }
        }
    }

    @Override
    public final void diffListener(final EDiff paramDiff) {
        mDiff = paramDiff;
        mCounter++;
    }
}
