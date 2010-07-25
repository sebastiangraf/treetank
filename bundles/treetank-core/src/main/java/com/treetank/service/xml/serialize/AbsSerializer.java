package com.treetank.service.xml.serialize;

import java.util.concurrent.Callable;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.FastStack;

/**
 * Class implements main serialization algorithm. Other classes can extend it.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsSerializer implements Callable<Void> {

    /** Treetank reading transaction {@link ISession}. */
    protected final ISession mSession;

    /** Stack for reading end element. */
    protected final FastStack<Long> mStack;

    /** Array with versions to print. */
    protected final long[] mVersions;

    /**
     * Constructor.
     * 
     * @param session
     *            {@link ISession}.
     * @param versions
     *            versions which should be serialized: -
     */
    public AbsSerializer(final ISession session, final long... versions) {
        mStack = new FastStack<Long>();
        mVersions = versions;
        mSession = session;
    }

    /**
     * Serialize the storage.
     * 
     * @throws Exception
     */
    public Void call() throws Exception {

        emitStartDocument();

        long[] versionsToUse;
        IReadTransaction rtx = mSession.beginReadTransaction();
        final long lastRevisionNumber = rtx.getRevisionNumber();
        rtx.close();

        // if there is one negative number in there, serialize all versions
        if (mVersions.length == 0) {
            versionsToUse = new long[] { lastRevisionNumber };
        } else {
            if (mVersions.length == 1 && mVersions[0] < 0) {
                versionsToUse = null;
            } else {
                versionsToUse = mVersions;
            }
        }

        for (long i = 0; versionsToUse == null ? i < lastRevisionNumber
                : i < versionsToUse.length; i++) {

            rtx = mSession.beginReadTransaction(versionsToUse == null ? i
                    : versionsToUse[(int) i]);
            if(versionsToUse==null || mVersions.length>1) {
                emitStartManualElement(i);
            }
            
            
            final IAxis descAxis = new DescendantAxis(rtx);

            // Setup primitives.
            boolean closeElements = false;
            long key = rtx.getNode().getNodeKey();

            // Iterate over all nodes of the subtree including self.
            while (descAxis.hasNext()) {
                key = descAxis.next();

                // Emit all pending end elements.
                if (closeElements) {
                    while (!mStack.empty()
                            && mStack.peek() != ((AbsStructNode) rtx.getNode())
                                    .getLeftSiblingKey()) {
                        rtx.moveTo(mStack.pop());
                        emitStartElement(rtx);
                        rtx.moveTo(key);
                    }
                    if (!mStack.empty()) {
                        rtx.moveTo(mStack.pop());
                        emitStartElement(rtx);
                    }
                    rtx.moveTo(key);
                    closeElements = false;
                }

                // Emit node.
                emitEndElement(rtx);

                // Push end element to stack if we are a start element with
                // children.
                if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND
                        && ((AbsStructNode) rtx.getNode()).hasFirstChild()) {
                    mStack.push(rtx.getNode().getNodeKey());
                }

                // Remember to emit all pending end elements from stack if
                // required.
                if (!((AbsStructNode) rtx.getNode()).hasFirstChild()
                        && !((AbsStructNode) rtx.getNode()).hasRightSibling()) {
                    closeElements = true;
                }

            }

            // Finally emit all pending end elements.
            while (!mStack.empty()) {
                rtx.moveTo(mStack.pop());
                emitStartElement(rtx);
            }
            
            if(versionsToUse==null || mVersions.length>1) {
                emitEndManualElement(i);
            }
        }
        emitEndDocument();

        return null;
    }

    protected abstract void emitStartDocument();

    protected abstract void emitStartElement(final IReadTransaction rtx);

    protected abstract void emitEndElement(final IReadTransaction rtx);

    protected abstract void emitStartManualElement(final long version);

    protected abstract void emitEndManualElement(final long version);

    protected abstract void emitEndDocument();
}
