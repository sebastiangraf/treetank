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

package org.treetank.service.revIndex;

import java.util.Stack;

import javax.xml.namespace.QName;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.utils.NamePageHash;

public final class DocumentTreeNavigator {

    static long adaptDocTree(final IWriteTransaction mWtx, final Stack<String> mNames)
        throws AbsTTException {
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
            } while (mWtx.moveToRightSibling());
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
        } while (rtx.moveToParent()
            && NamePageHash.generateHashForString(RevIndex.DOCUMENTROOT_ELEMENTNAME) != rtx.getNode()
                .getNameKey());

        return returnVal;
    }

    /**
     * Moving to documentstructure root.
     * 
     * @param mRtx
     *            Read Transaction session.
     * @throws AbsTTException
     *             If can't move to Root of document.
     */
    private static void moveToDocumentStructureRoot(final IReadTransaction mRtx) throws AbsTTException {
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
