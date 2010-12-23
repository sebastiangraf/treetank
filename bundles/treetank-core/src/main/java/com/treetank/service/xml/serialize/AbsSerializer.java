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

package com.treetank.service.xml.serialize;

import java.util.concurrent.Callable;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
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

    /** Treetank session {@link ISession}. */
    protected final ISession mSession;

    /** Stack for reading end element. */
    protected final FastStack<Long> mStack;

    /** Array with versions to print. */
    protected final long[] mVersions;

    /** Root node key of subtree to shredder. */
    protected final long mNodeKey;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession}.
     * @param paramVersions
     *            versions which should be serialized: -
     */
    public AbsSerializer(final ISession paramSession, final long... paramVersions) {
        mStack = new FastStack<Long>();
        mVersions = paramVersions;
        mSession = paramSession;
        mNodeKey = 0;
    }

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession}.
     * @param paramKey
     *            Key of root node from which to shredder the subtree.
     * @param paramVersions
     *            versions which should be serialized: -
     */
    public AbsSerializer(final ISession paramSession, final long paramKey, final long... paramVersions) {
        mStack = new FastStack<Long>();
        mVersions = paramVersions;
        mSession = paramSession;
        mNodeKey = paramKey;
    }

    /**
     * Serialize the storage.
     * 
     * @return null.
     * @throws TTException
     *             if can't call serailzer
     */
    public Void call() throws TTException {
        emitStartDocument();

        long[] versionsToUse;
        IReadTransaction rtx = mSession.beginReadTransaction();
        rtx.moveTo(mNodeKey);
        final long lastRevisionNumber = rtx.getRevisionNumber();
        rtx.close();

        // if there is one negative number in there, serialize all versions
        if (mVersions.length == 0) {
            versionsToUse = new long[] {
                lastRevisionNumber
            };
        } else {
            if (mVersions.length == 1 && mVersions[0] < 0) {
                versionsToUse = null;
            } else {
                versionsToUse = mVersions;
            }
        }

        for (long i = 0; versionsToUse == null ? i < lastRevisionNumber : i < versionsToUse.length; i++) {

            rtx = mSession.beginReadTransaction(versionsToUse == null ? i : versionsToUse[(int)i]);
            if (versionsToUse == null || mVersions.length > 1) {
                emitStartManualElement(i);
            }

            rtx.moveTo(mNodeKey);

            final IAxis descAxis = new DescendantAxis(rtx, true);

            // Setup primitives.
            boolean closeElements = false;
            long key = rtx.getNode().getNodeKey();

            // Iterate over all nodes of the subtree including self.
            while (descAxis.hasNext()) {
                key = descAxis.next();

                // Emit all pending end elements.
                if (closeElements) {
                    while (!mStack.empty()
                        && mStack.peek() != ((AbsStructNode)rtx.getNode()).getLeftSiblingKey()) {
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
                    && ((AbsStructNode)rtx.getNode()).hasFirstChild()) {
                    mStack.push(rtx.getNode().getNodeKey());
                }

                // Remember to emit all pending end elements from stack if
                // required.
                if (!((AbsStructNode)rtx.getNode()).hasFirstChild()
                    && !((AbsStructNode)rtx.getNode()).hasRightSibling()) {
                    closeElements = true;
                }

            }

            // Finally emit all pending end elements.
            while (!mStack.empty()) {
                rtx.moveTo(mStack.pop());
                emitStartElement(rtx);
            }

            if (versionsToUse == null || mVersions.length > 1) {
                emitEndManualElement(i);
            }
        }
        emitEndDocument();

        return null;
    }

    /** Emit start document. */
    protected abstract void emitStartDocument();

    /**
     * Emit start tag.
     * 
     * @param paramRTX
     *            Treetank reading transaction {@link IReadTransaction}.
     */
    protected abstract void emitStartElement(final IReadTransaction paramRTX);

    /**
     * Emit end tag.
     * 
     * @param paramRTX
     *            Treetank reading transaction {@link IReadTransaction}.
     */
    protected abstract void emitEndElement(final IReadTransaction paramRTX);

    /**
     * Emit a start tag, which specifies a revision.
     * 
     * @param paramVersion
     *            The revision to serialize.
     */
    protected abstract void emitStartManualElement(final long paramVersion);

    /**
     * Emit an end tag, which specifies a revision.
     * 
     * @param paramVersion
     *            The revision to serialize.
     */
    protected abstract void emitEndManualElement(final long paramVersion);

    /** Emit end document. */
    protected abstract void emitEndDocument();
}
