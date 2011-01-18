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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.stream.XMLStreamException;

import com.treetank.TestHelper;
import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TTException;
import com.treetank.service.xml.shredder.EShredderCommit;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.shredder.XMLUpdateShredder;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test StructuralDiff.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class StructuralDiffTest implements IDiffObserver {
    private transient IDatabase mDatabase;

    private transient EDiff mDiff;

    private transient List<EDiff> mList;

    private transient int mCounter;

    private transient CountDownLatch mStart;

    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    @Before
    public void setUp() throws TTException {
        mList = new LinkedList<EDiff>();
        mStart = new CountDownLatch(1);
        mDiff = EDiff.SAME;
        TestHelper.deleteEverything();
        mDatabase = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        DocumentCreater.createVersioned(mDatabase.getSession().beginWriteTransaction());
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testStructuralDiffFirst() throws InterruptedException {
        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(this);

        DiffFactory.invokeStructuralDiff(mDatabase, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await();

        while (!mList.isEmpty()) {
            mDiff = mList.remove(0);
            mCounter++;
            switch (mCounter) {
            case 1:
                assertEquals(EDiff.INSERTED, mDiff);
                break;
            case 2:
                assertEquals(EDiff.INSERTED, mDiff);
                break;
            default:
                assertEquals(EDiff.SAME, mDiff);
                break;
            }
        }
    }

    @Test
    public void testStructuralDiffSecond() throws TTException, IOException, XMLStreamException,
        InterruptedException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator + "revXMLsAll4"
                + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsAll4" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(this);
        DiffFactory.invokeStructuralDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await();

        while (!mList.isEmpty()) {
            mDiff = mList.remove(0);
            mCounter++;
            switch (mCounter) {
            case 1:
                assertEquals(EDiff.SAME, mDiff);
                break;
            case 2:
                assertEquals(EDiff.SAME, mDiff);
                break;
            case 3:
                assertEquals(EDiff.RENAMED, mDiff);
                break;
            default:
                assertEquals(EDiff.SAME, mDiff);
                break;
            }
        }
    }

    @Test
    public void testStructuralDiffThird() throws TTException, IOException, XMLStreamException,
        InterruptedException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator + "revXMLsAll3"
                + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsAll3" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(this);
        DiffFactory.invokeStructuralDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await();

        while (!mList.isEmpty()) {
            mDiff = mList.remove(0);
            mCounter++;
            switch (mCounter) {
            case 1:
                assertEquals(mDiff, EDiff.SAME);
                break;
            case 2:
                assertEquals(mDiff, EDiff.SAME);
                break;
            case 3:
                assertEquals(mDiff, EDiff.SAME);
                break;
            case 4:
                assertEquals(mDiff, EDiff.INSERTED);
                break;
            default:
                assertEquals(mDiff, EDiff.SAME);
                break;
            }
        }
    }

    @Test
    public void testStructuralDiffFourth() throws TTException, IOException, XMLStreamException,
        InterruptedException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator + "revXMLsAll2"
                + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsAll2" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(this);
        DiffFactory.invokeStructuralDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await();

        while (!mList.isEmpty()) {
            mDiff = mList.remove(0);
            mCounter++;
            switch (mCounter) {
            case 1:
                assertEquals(mDiff, EDiff.SAME);
                break;
            case 2:
                assertEquals(mDiff, EDiff.RENAMED);
                break;
            case 3:
                assertEquals(mDiff, EDiff.SAME);
                break;
            default:
                fail("Parsing should be ended already!");
                break;
            }
        }
    }

    @Override
    public void diffListener(final EDiff paramDiff) {
        if (paramDiff == EDiff.DONE) {
            mStart.countDown();
        } else {
            mList.add(paramDiff);
        }
    }
}
