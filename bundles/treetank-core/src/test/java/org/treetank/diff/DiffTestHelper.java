package org.treetank.diff;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.easymock.IAnswer;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.api.IStructuralItem;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffOptimized;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderCommit;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.shredder.XMLUpdateShredder;
import org.treetank.utils.DocumentCreater;

public final class DiffTestHelper {

    protected static transient CountDownLatch mStart;

    protected static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    protected static final long TIMEOUT_S = 5;

    static void setUp() throws AbsTTException {
        mStart = new CountDownLatch(1);
        TestHelper.deleteEverything();
    }

    static void setUpFirst(final Holder paramHolder) throws AbsTTException {
        DocumentCreater.createVersioned(paramHolder.getWtx());
    }

    static void setUpSecond(final Holder paramHolder) throws AbsTTException, IOException, XMLStreamException {
        initializeData(paramHolder, new File(RESOURCES + File.separator + "revXMLsAll4" + File.separator
            + "1.xml"), new File(RESOURCES + File.separator + "revXMLsAll4" + File.separator + "2.xml"));
    }

    static void setUpThird(final Holder paramHolder) throws AbsTTException, IOException, XMLStreamException {
        initializeData(paramHolder, new File(RESOURCES + File.separator + "revXMLsDelete1" + File.separator
            + "1.xml"), new File(RESOURCES + File.separator + "revXMLsDelete1" + File.separator + "2.xml"));
    }

    static void setUpFourth(final Holder paramHolder) throws AbsTTException, IOException, XMLStreamException {
        initializeData(paramHolder, new File(RESOURCES + File.separator + "revXMLsAll3" + File.separator
            + "1.xml"), new File(RESOURCES + File.separator + "revXMLsAll3" + File.separator + "2.xml"));
    }

    static void setUpFifth(final Holder paramHolder) throws AbsTTException, IOException, XMLStreamException {
        initializeData(paramHolder, new File(RESOURCES + File.separator + "revXMLsAll2" + File.separator
            + "1.xml"), new File(RESOURCES + File.separator + "revXMLsAll2" + File.separator + "2.xml"));
    }

    static void setUpSixth(final Holder paramHolder) throws AbsTTException, IOException, XMLStreamException {
        initializeData(paramHolder, new File(RESOURCES + File.separator + "revXMLsDelete2" + File.separator
            + "1.xml"), new File(RESOURCES + File.separator + "revXMLsDelete2" + File.separator + "2.xml"));

    }

    private static void initializeData(final Holder paramHolder, final File... paramFile)
        throws AbsTTException, IOException, XMLStreamException {

        int i = 0;
        for (final File file : paramFile) {
            XMLShredder init;
            if (i == 0) {
                init =
                    new XMLShredder(paramHolder.getWtx(), XMLShredder.createFileReader(file),
                        EShredderInsert.ADDASFIRSTCHILD);
            } else {
                init =
                    new XMLUpdateShredder(paramHolder.getWtx(), XMLShredder.createFileReader(file),
                        EShredderInsert.ADDASFIRSTCHILD, file, EShredderCommit.COMMIT);
            }
            i++;
            init.call();
        }

    }

    static IDiffObserver testDiffFirst() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testOptimizedFirst() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testDiffSecond() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testDiffThird() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testOptimizedThird() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testDiffFourth() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.INSERTED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testDiffFifth() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.UPDATED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static IDiffObserver testDiffSixth() {
        final IDiffObserver listener = createStrictMock(IDiffObserver.class);
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.SAME), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffListener(eq(EDiff.DELETED), isA(IStructuralItem.class), isA(IStructuralItem.class),
            isA(DiffDepth.class));
        listener.diffDone();
        replayInternal(listener);
        return listener;
    }

    static void verifyInternal(final IDiffObserver paramListener) throws InterruptedException {
        DiffTestHelper.mStart.await(DiffTestHelper.TIMEOUT_S, TimeUnit.SECONDS);
        verify(paramListener);
    }

    static void replayInternal(final IDiffObserver paramListener) {
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                mStart.countDown();
                return null;
            }
        });
        replay(paramListener);
    }

    static void check(final Holder paramHolder, final IDiffObserver paramObserver,
        final EDiffOptimized paramOptimized) throws AbsTTException, InterruptedException {
        final Set<IDiffObserver> observers = new HashSet<IDiffObserver>();
        observers.add(paramObserver);
        DiffFactory.invokeFullDiff(new DiffFactory.Builder(paramHolder.getSession(), 0, 1, 0, paramOptimized,
            observers));

        DiffTestHelper.verifyInternal(paramObserver);
    }

}
