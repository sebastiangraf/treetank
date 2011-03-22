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

import javax.xml.namespace.QName;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.utils.NamePageHash;

final class TrieNavigator {

    /**
     * Moving with a writetransaction into the trie and adapting it if the term
     * hasn't been stored in the structure.
     * 
     * @param mWtx
     *            with which should be worked, is moved to the root of the trie
     *            at the beginning
     * @param mTerm
     *            to be inserted
     * @throws AbsTTException
     *             handling treetank exception
     */
    static void adaptTrie(final IWriteTransaction mWtx, final String mTerm) throws AbsTTException {
        moveToTrieRoot(mWtx);
        int endIndexOfTerm = 1;
        String current = "";
        // for each letter in the term to be indexed...
        while (endIndexOfTerm <= mTerm.length()) {
            // ..create a node in the structure and...
            current = mTerm.substring(0, endIndexOfTerm);
            // ..navigate through the trie by inserting directly the prefix
            // if no firstchild is present in the trie...
            if (!mWtx.moveToFirstChild()) {
                mWtx.insertElementAsFirstChild(new QName(RevIndex.TRIE_ELEMENT));
                mWtx.insertAttribute(new QName(RevIndex.TRIE_PREFIX_ATTRIBUTEKEY), current);
                mWtx.moveToParent();
            }
            boolean found = false;
            // ..or checking against the sibling axis of the trie and
            // checking of the prefix is present as a sibling of the
            // firstchild...
            do {
                if (mWtx.getNode().getNameKey() == NamePageHash.generateHashForString(RevIndex.TRIE_ELEMENT)) {
                    if (!mWtx.moveToAttribute(0)) {
                        throw new IllegalStateException();
                    }
                    if (mWtx.getValueOfCurrentNode().hashCode() == current.hashCode()) {
                        found = true;
                    }
                    mWtx.moveToParent();
                    if (found) {
                        break;
                    }
                }
            } while (mWtx.moveToRightSibling());
            // ...of not, insert it...
            if (!found) {
                mWtx.insertElementAsRightSibling(new QName(RevIndex.TRIE_ELEMENT));
                mWtx.insertAttribute(new QName(RevIndex.TRIE_PREFIX_ATTRIBUTEKEY), current);
                mWtx.moveToParent();
            }
            // ..and adapt the prefix with the next letter from the trie..
            current = "";
            endIndexOfTerm++;
        }
    }

    static long getDocRootInTrie(final IReadTransaction mRtx, final String mTerm) throws AbsTTException {
        moveToTrieRoot(mRtx);
        long returnVal = ENodes.UNKOWN_KIND.getNodeIdentifier();
        final StringBuilder toSearch = new StringBuilder();
        for (int i = 0; i < mTerm.length(); i++) {
            if (mRtx.moveToFirstChild()) {
                toSearch.append(mTerm.charAt(i));
                do {
                    if (mRtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(RevIndex.TRIE_ELEMENT)) {
                        if (!mRtx.moveToAttribute(0)) {
                            throw new IllegalStateException();
                        }
                        if (mRtx.getValueOfCurrentNode().hashCode() == toSearch.toString().hashCode()) {
                            mRtx.moveToParent();
                            break;
                        } else {
                            mRtx.moveToParent();
                        }

                    }
                } while (mRtx.moveToRightSibling());

            } else {
                break;
            }
        }
        if (mRtx.getNode().getNameKey() == NamePageHash.generateHashForString(RevIndex.TRIE_ELEMENT)) {
            if (!mRtx.moveToAttribute(0)) {
                throw new IllegalStateException();
            }
            if (mRtx.getValueOfCurrentNode().hashCode() == toSearch.toString().hashCode()) {
                mRtx.moveToParent();
                returnVal = mRtx.getNode().getNodeKey();
            } else {
                mRtx.moveToParent();
            }
        }
        return returnVal;

    }

    /**
     * Private method to the root of the trie. Inserting basic structure if not
     * avaliable.
     * 
     * @param mRtx
     *            Read Transaction
     * @throws AbsTTException
     *             handling treetank exception
     */
    private static void moveToTrieRoot(final IReadTransaction mRtx) throws AbsTTException {
        mRtx.moveToDocumentRoot();
        if (!((AbsStructNode)mRtx.getNode()).hasFirstChild()) {
            RevIndex.initialiseBasicStructure(mRtx);
            mRtx.moveToLeftSibling();
        } else {
            mRtx.moveToFirstChild();
            mRtx.moveToRightSibling();
        }
    }

}
