package com.treetank.service.xml.serialize;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
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

    /** Descendant-or-self axis used to traverse subtree. */
    final IAxis mAxis;

    /** Treetank reading transaction {@link IReadTransaction}. */
    final IReadTransaction mRTX;

    /** Stack for reading end element. */
    final FastStack<Long> mStack;

    /**
     * Constructor.
     * 
     * @param axis
     *            {@link IAxis}.
     * @param builder
     *            container of type {@link SerializerBuilder} to store the
     *            setting for serialization.
     */
    public AbsSerializer(final IAxis axis) {
        mAxis = axis;
        mRTX = axis.getTransaction();
        mStack = new FastStack<Long>();
    }

    /**
     * Serialize the storage.
     * 
     * @throws Exception
     */
    public Void call() throws Exception {
        // Setup primitives.
        boolean closeElements = false;
        long key = mRTX.getNode().getNodeKey();

        // Iterate over all nodes of the subtree including self.
        while (mAxis.hasNext()) {
            key = mAxis.next();

            // Emit all pending end elements.
            if (closeElements) {
                while (!mStack.empty()
                        && mStack.peek() != ((AbsStructNode) mRTX.getNode())
                                .getLeftSiblingKey()) {
                    mRTX.moveTo(mStack.pop());
                    emitEndElement();
                    mRTX.moveTo(key);
                }
                if (!mStack.empty()) {
                    mRTX.moveTo(mStack.pop());
                    emitEndElement();
                }
                mRTX.moveTo(key);
                closeElements = false;
            }

            // Emit node.
            emitNode();

            // Push end element to stack if we are a start element with
            // children.
            if (mAxis.getTransaction().getNode().getKind() == ENodes.ELEMENT_KIND
                    && ((AbsStructNode) mRTX.getNode()).hasFirstChild()) {
                mStack.push(mRTX.getNode().getNodeKey());
            }

            // Remember to emit all pending end elements from stack if
            // required.
            if (!((AbsStructNode) mRTX.getNode()).hasFirstChild()
                    && !((AbsStructNode) mRTX.getNode()).hasRightSibling()) {
                closeElements = true;
            }
        }

        // Finally emit all pending end elements.
        while (!mStack.empty()) {
            mAxis.getTransaction().moveTo(mStack.pop());
            emitEndElement();
        }

        return null;
    }

    protected abstract void emitEndElement() throws IOException;

    protected abstract void emitNode() throws IOException;
}
