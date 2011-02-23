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

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import com.treetank.TestHelper;
import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IWriteTransaction;
import com.treetank.diff.DiffFactory.EDiff;
import com.treetank.diff.DiffFactory.EDiffKind;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.shredder.EShredderCommit;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.shredder.XMLUpdateShredder;
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
    private transient CountDownLatch mStart;

    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    private static final long TIMEOUT_S = 200000;

    @Before
    public void setUp() throws AbsTTException {
        mStart = new CountDownLatch(1);
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

//    @Test
//    public void testFullDiffFirst() throws Exception {
//        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
//        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(EDiff.DONE, null, null);
//
//        expectLastCall().andAnswer(new IAnswer<Void>() {
//            @Override
//            public Void answer() throws Throwable {
//                mStart.countDown();
//                return null;
//            }
//        });
//        replay(listener);
//
//        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
//        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
//        DocumentCreater.createVersioned(wtx);
//        wtx.close();
//
//        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
//        observer.add(listener);
//        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);
//
//        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
//        verify(listener);
//    }

    @Test
    public void testFullDiffOptimizedFirst() throws InterruptedException, AbsTTException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
//        listener.diffListener(eq(EDiff.SAMEHASH), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        DocumentCreater.createVersioned(wtx);
        wtx.close();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.OPTIMIZED, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSecond() throws AbsTTException, IOException, XMLStreamException,
        InterruptedException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.RENAMED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

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
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffThird() throws AbsTTException, IOException, XMLStreamException, InterruptedException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator
                + "revXMLsDelete1" + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsDelete1" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffFourth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

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
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffFifth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.RENAMED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

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
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSixth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator
                + "revXMLsDelete2" + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsDelete2" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSeventh() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator + "revXMLsAll5"
                + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsAll5" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffEighth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class));
        listener.diffListener(EDiff.DONE, null, null);

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction wtx = database.getSession().beginWriteTransaction();
        final XMLShredder init =
            new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES + File.separator + "revXMLsAll6"
                + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
        init.call();
        final File file = new File(RESOURCES + File.separator + "revXMLsAll6" + File.separator + "2.xml");
        final XMLShredder shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(file), EShredderInsert.ADDASFIRSTCHILD, file,
                EShredderCommit.COMMIT);
        shredder.call();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeFullDiff(database, 0, 1, 0, EDiffKind.NORMAL, observer);

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }
}
