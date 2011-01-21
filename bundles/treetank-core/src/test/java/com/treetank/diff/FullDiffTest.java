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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.treetank.TestHelper;
import com.treetank.api.IDatabase;
import com.treetank.exception.TTException;
import com.treetank.utils.DocumentCreater;

import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * FullDiff test.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class FullDiffTest {
    private transient IDatabase mDatabase;

    private transient CountDownLatch mStart;

    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    private transient long TIMEOUT_S = 200000;

    @Before
    public void setUp() throws TTException {
        mStart = new CountDownLatch(1);
        TestHelper.deleteEverything();
        mDatabase = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        DocumentCreater.createVersioned(mDatabase.getSession().beginWriteTransaction());
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testFullDiffFirst() throws InterruptedException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(EDiff.INSERTED);
        listener.diffListener(EDiff.INSERTED);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.SAME);
        listener.diffListener(EDiff.DONE);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(mDatabase, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }
}
