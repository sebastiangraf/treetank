/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.diff;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.treetank.TestHelper;
import org.treetank.api.IDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IWriteTransaction;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffOptimized;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderCommit;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.shredder.XMLUpdateShredder;
import org.treetank.utils.DocumentCreater;

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
    public final void setUp() throws AbsTTException {
        mStart = new CountDownLatch(1);
        TestHelper.deleteEverything();
    }

    @After
    public final void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testFullDiffFirst() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffOptimizedFirst() throws InterruptedException, AbsTTException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.HASHED, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSecond() throws AbsTTException, IOException, XMLStreamException,
        InterruptedException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffThird() throws AbsTTException, IOException, XMLStreamException, InterruptedException {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffFourth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffFifth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSixth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffSeventh() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }

    @Test
    public void testFullDiffEighth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

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
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }
    
    @Test
    public final void testFullDiffTenth() throws Exception {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IItem.class), isA(IItem.class), isA(DiffDepth.class));
        listener.diffDone();

        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(listener);

        TestHelper.closeEverything();
        TestHelper.deleteEverything();
        DocumentCreater.createRevisioned();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(database, 0, 1, 0, EDiffOptimized.NO, observer));

        mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
        verify(listener);
    }
}
