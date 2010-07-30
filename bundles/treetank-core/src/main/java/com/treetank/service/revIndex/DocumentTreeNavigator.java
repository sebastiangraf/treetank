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

package com.treetank.service.revIndex;

import java.util.Stack;

import javax.xml.namespace.QName;

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.NamePageHash;

public final class DocumentTreeNavigator {

    static long adaptDocTree(final IWriteTransaction mWtx, final Stack<String> mNames) 
        throws TreetankException {
        moveToDocumentStructureRoot(mWtx);
        long currentDocKey = ENodes.UNKOWN_KIND.getNodeIdentifier();
        // iterating over all names in hierarchical order
        while (!mNames.empty()) {
            final String name = mNames.pop();

            // if firstChild is not existing,...
            if (!mWtx.moveToFirstChild()) {
                // ..inserting it...
                mWtx.insertElementAsFirstChild(new QName(RevIndex.DOCUMENT_ELEMENT));
                mWtx.insertAttribute(new QName(RevIndex.DOCUMENT_NODE_ATTIBUTEKEY), name);
                mWtx.moveToParent();
            }
            // Check if there was already a document on the sibling axis...
            boolean found = false;
            do {
                // ...and check the name against the current document name
                // for each sibling..
                if (mWtx.getNode().getNameKey() == NamePageHash
                    .generateHashForString(RevIndex.DOCUMENT_ELEMENT)) {
                    // ..and break up if it is canceled
                    if (!mWtx.moveToAttribute(0)) {

                        throw new IllegalStateException();
                    }
                    if (mWtx.getValueOfCurrentNode().hashCode() == name.hashCode()) {
                        found = true;
                    }
                    mWtx.moveToParent();
                    if (found) {
                        currentDocKey = mWtx.getNode().getNodeKey();
                        break;
                    }
                }
            } while(mWtx.moveToRightSibling());
            // ...if there hasn't be an element, insert the
            if (!found) {
                mWtx.insertElementAsRightSibling(new QName(RevIndex.DOCUMENT_ELEMENT));
                mWtx.insertAttribute(new QName(RevIndex.DOCUMENT_NODE_ATTIBUTEKEY), name);
                mWtx.moveToParent();

                currentDocKey = mWtx.getNode().getNodeKey();
            }
        }
        return currentDocKey;
    }

    static Stack<String> getDocElements(final IReadTransaction rtx) {
        final Stack<String> returnVal = new Stack<String>();
        do {
            rtx.moveToAttribute(0);
            returnVal.add(rtx.getValueOfCurrentNode());
            rtx.moveToParent();
        } while(rtx.moveToParent()
        && NamePageHash.generateHashForString(RevIndex.DOCUMENTROOT_ELEMENTNAME) != rtx.getNode()
            .getNameKey());

        return returnVal;
    }

    /**
     * Moving to documentstructure root.
     * 
     * @param mRtx
     *            Read Transaction session.
     * @throws TreetankException
     *            If can't move to Root of document.
     */
    private static void moveToDocumentStructureRoot(final IReadTransaction mRtx) throws TreetankException {
        mRtx.moveToDocumentRoot();
        if (!((AbsStructNode)mRtx.getNode()).hasFirstChild()) {
            RevIndex.initialiseBasicStructure(mRtx);
        } else {
            mRtx.moveToFirstChild();
            mRtx.moveToRightSibling();
            mRtx.moveToRightSibling();
        }
    }
}
