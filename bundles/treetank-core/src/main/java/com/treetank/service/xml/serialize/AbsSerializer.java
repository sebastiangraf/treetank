package com.treetank.service.xml.serialize;

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
abstract class AbsSerializer implements ISerialize {

    /** Descendant-or-self axis used to traverse subtree. */
    final IAxis mAxis;

    final IReadTransaction mRTX;

    /** Stack for reading end element. */
    final FastStack<Long> mStack;

    /** Serialize XML declaration. */
    final boolean mSerializeXMLDeclaration;

    /** Serialize rest header and closer and rest:id */
    final boolean mSerializeRest;

    /** Serialize id */
    final boolean mSerializeId;

    /**
     * Constructor.
     * 
     * @param axis
     *            {@link IAxis}.
     * @param builder
     *            container of type {@link SerializerBuilder} to store the
     *            setting for serialization.
     */
    public AbsSerializer(final IAxis axis, final SerializerBuilder builder) {
        mAxis = axis;
        mRTX = axis.getTransaction();
        mStack = new FastStack<Long>();
        mSerializeXMLDeclaration = builder.mDeclaration;
        mSerializeRest = builder.mREST;
        mSerializeId = builder.mID;
    }

    /**
     * Serialize the storage.
     * 
     * @throws Exception
     */
    public void serialize() throws Exception {
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
    }
}
