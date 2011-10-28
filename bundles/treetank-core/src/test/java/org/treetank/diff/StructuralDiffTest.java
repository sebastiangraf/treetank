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

package org.treetank.diff;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.diff.DiffFactory.EDiffOptimized;
import org.treetank.exception.AbsTTException;

/**
 * Test StructuralDiff.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class StructuralDiffTest {

    private Holder mHolder;

    @Before
    public void setUp() throws AbsTTException {
        DiffTestHelper.setUp();
        mHolder = Holder.generateWtx();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    @Ignore
    public void testStructuralDiffFirst() throws Exception {
        DiffTestHelper.setUpFirst(mHolder);
        final IDiffObserver listener = DiffTestHelper.testDiffFirst();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testOptimizedFirst() throws Exception {
        DiffTestHelper.setUpFirst(mHolder);
        final IDiffObserver listener = DiffTestHelper.testOptimizedFirst();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffSecond() throws AbsTTException, InterruptedException, IOException,
        XMLStreamException {

        DiffTestHelper.setUpSecond(mHolder);
        final IDiffObserver listener = DiffTestHelper.testDiffSecond();

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffOptimizedSecond() throws AbsTTException, IOException, XMLStreamException,
        InterruptedException {
        final IDiffObserver listener = DiffTestHelper.testDiffSecond();
        DiffTestHelper.setUpSecond(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffThird() throws AbsTTException, IOException, XMLStreamException,
        InterruptedException {
        final IDiffObserver listener = DiffTestHelper.testDiffThird();
        DiffTestHelper.setUpThird(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffOptimizedThird() throws AbsTTException, IOException, XMLStreamException,
        InterruptedException {
        final IDiffObserver listener = DiffTestHelper.testOptimizedThird();
        DiffTestHelper.setUpThird(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffFourth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffFourth();
        DiffTestHelper.setUpFourth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffOptimizedFourth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffFourth();
        DiffTestHelper.setUpFourth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffFifth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffFifth();
        DiffTestHelper.setUpFifth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffOptimizedFifth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffFifth();
        DiffTestHelper.setUpFifth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffSixth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffSixth();
        DiffTestHelper.setUpSixth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.NO, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    @Test
    @Ignore
    public void testStructuralDiffOptimizedSixth() throws Exception {
        final IDiffObserver listener = DiffTestHelper.testDiffSixth();
        DiffTestHelper.setUpSixth(mHolder);

        final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
        observer.add(listener);
        DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mHolder.getDatabase(), 0, 1, 0,
            EDiffOptimized.HASHED, observer));

        DiffTestHelper.verifyInternal(listener);
    }

    // @Test
    // public void testStructuralDiffSeventh() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator
    // + "revXMLsDelete1" + File.separator + "1.xml")),
    // EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsDelete1"
    // + File.separator + "3.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.NO,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // @Test
    // public void testStructuralDiffOptimizedSeventh() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator
    // + "revXMLsDelete1" + File.separator + "1.xml")),
    // EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsDelete1"
    // + File.separator + "3.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.HASHED,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // @Test
    // public void testStructuralDiffEighth() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator
    // + "revXMLsDelete4" + File.separator + "1.xml")),
    // EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsDelete4"
    // + File.separator + "2.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.NO,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // @Test
    // public void testStructuralDiffOptimizedEighth() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator
    // + "revXMLsDelete4" + File.separator + "1.xml")),
    // EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsDelete4"
    // + File.separator + "2.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.HASHED,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // @Test
    // public void testStructuralDiffNeinth() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator + "revXMLsAll7"
    // + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsAll7" +
    // File.separator + "2.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.NO,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // @Test
    // public void testStructuralDiffOptimizedNeinth() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // TestHelper.closeEverything();
    // TestHelper.deleteEverything();
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // final IWriteTransaction wtx =
    // database.getSession(new
    // SessionConfiguration.Builder()).beginWriteTransaction();
    // final XMLShredder init =
    // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator + "revXMLsAll7"
    // + File.separator + "1.xml")), EShredderInsert.ADDASFIRSTCHILD);
    // init.call();
    // final File file = new File(RESOURCES + File.separator + "revXMLsAll7" +
    // File.separator + "2.xml");
    // final XMLShredder shredder =
    // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // EShredderCommit.COMMIT);
    // shredder.call();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.HASHED,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
    //
    // //
    // // @Test
    // // public void testStructuralDiffTenth() throws Exception {
    // // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class));
    // // listener.diffListener(EDiff.DONE, null, null);
    // //
    // // expectLastCall().andAnswer(new IAnswer<Void>() {
    // // @Override
    // // public Void answer() throws Throwable {
    // // mStart.countDown();
    // // return null;
    // // }
    // // });
    // // replay(listener);
    // //
    // // TestHelper.closeEverything();
    // // TestHelper.deleteEverything();
    // // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // // final IWriteTransaction wtx =
    // database.getSession().beginWriteTransaction();
    // // final XMLShredder init =
    // // new XMLShredder(wtx, XMLShredder.createReader(new File(RESOURCES +
    // File.separator
    // // + "revXMLsAll8" + File.separator + "1.xml")),
    // EShredderInsert.ADDASFIRSTCHILD);
    // // init.call();
    // // final File file = new File(RESOURCES + File.separator + "revXMLsAll8"
    // + File.separator + "2.xml");
    // // final XMLShredder shredder =
    // // new XMLUpdateShredder(wtx, XMLShredder.createReader(file),
    // EShredderInsert.ADDASFIRSTCHILD, file,
    // // EShredderCommit.COMMIT);
    // // shredder.call();
    // //
    // // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // // observer.add(listener);
    // // DiffFactory.invokeStructuralDiff(database, 0, 1, 0, EDiffKind.NORMAL,
    // observer);
    // //
    // // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // // verify(listener);
    // // }
    //
    // @Test
    // public void testStructuralDiffTenth() throws Exception {
    // final IDiffObserver listener = createStrictMock(IDiffObserver.class);
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class),
    // isA(IStructuralItem.class),
    // isA(DiffDepth.class));
    // listener.diffDone();
    //
    // expectLastCall().andAnswer(new IAnswer<Void>() {
    // @Override
    // public Void answer() throws Throwable {
    // mStart.countDown();
    // return null;
    // }
    // });
    // replay(listener);
    //
    // DocumentCreater.createRevisioned();
    //
    // final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
    // observer.add(listener);
    // final IDatabase database =
    // TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    // DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(database, 0, 1,
    // 0, EDiffOptimized.NO,
    // observer));
    //
    // mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
    // verify(listener);
    // }
}
