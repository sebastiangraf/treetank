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

package org.treetank.service.revIndex;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.treetank.access.FileDatabase;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.utils.LogWrapper;
import org.treetank.utils.NamePageHash;

/**
 * Revisioned Index Structure. Consisting of a trie and a document-trie. Both
 * structures are merged on one treetank structure and can be distinguished with
 * the help of different namespaces.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class RevIndex {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(RevIndex.class));

    protected static final String EMPTY_STRING = "";

    // MetaRoot Elemens
    final static String META_ELEMENT = "indexRevision";

    // MetaRoot Atts
    final static String INDEXREV_ATTRIBUTEKEY = "indexRevision";
    final static String FIRSTTTREV_ATTRIBUTEKEY = "firstTTRevision";
    final static String LASTTTREV_ATTRIBUTEKEY = "lastTTRevision";

    // Trie Elems
    final static String TRIE_ELEMENT = "t";
    final static String DOCUMENTS_REF_ROOTELEMENT = "documentReferences";
    final static String DOCUMENTREFERENCE_ELEMENTNAME = "elementReference";

    // Trie Atts
    final static String TRIE_PREFIX_ATTRIBUTEKEY = "prefix";

    // Document Elems
    final static String DOCUMENT_ELEMENT = "d";

    // Document Atts
    final static String DOCUMENT_NODE_ATTIBUTEKEY = "node";

    // Root Elemens
    final static String METAROOT_ELEMENTNAME = "metaRevRoot";
    final static String TRIEROOT_ELEMENTNAME = "trieRoot";
    final static String DOCUMENTROOT_ELEMENTNAME = "documentRoot";

    // private final static Pattern PATTERN = Pattern.compile("[a-zA-Z]+");

    private long mIndexRev;

    private long mCurrentDocKey = -1;

    /**
     * {@link ISession} to the index structure.
     */
    private final ISession mIndexSession;
    /**
     * {@link IWriteTransaction} to trie session.
     */
    private final IReadTransaction mRtx;

    /**
     * Constructor.
     * 
     * 
     * @param mIndex
     *            folder to be access.
     * @param mRev
     *            revision to be accessed
     * @throws AbsTTException
     *             if any access to Treetank fails
     */
    public RevIndex(final File mIndex, final long mRev) throws AbsTTException {
        final IDatabase db = FileDatabase.openDatabase(mIndex);
        mIndexSession = db.getSession(new SessionConfiguration());
        if (mRev < 0) {
            mRtx = mIndexSession.beginWriteTransaction();
        } else {
            this.mRtx = mIndexSession.beginReadTransaction(mRev);
        }
    }

    public void compact(final double mThreshold) {

    }

    /**
     * Inserting the next node in the document tree.
     * 
     * @param mDocs
     *            stack with order to be inserted.
     * @throws AbsTTException
     *             if can't insert next node.
     */
    public void insertNextNode(final Stack<String> mDocs) throws AbsTTException {
        if (mRtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction)mRtx;
            mCurrentDocKey = DocumentTreeNavigator.adaptDocTree(wtx, mDocs);
        }
    }

    /**
     * Indexing a term with the given Stack full of uuids of an hierarchical
     * structure.
     * 
     * @param mTerm
     *            the term to be indexed.
     * @throws AbsTTException
     *             if can't insert next term in current node.
     */
    public void insertNextTermForCurrentNode(final String mTerm) throws AbsTTException {
        // check if rtx is instance of WriteTransaction
        if (mRtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction)mRtx;

            // Navigating in the trie
            TrieNavigator.adaptTrie(wtx, mTerm);

            // If the trienode has no child, insert the root for the
            // references..
            if (!wtx.moveToFirstChild()) {
                wtx.insertElementAsFirstChild(new QName(DOCUMENTS_REF_ROOTELEMENT));
            } else {
                // ..otherwise go to the first child..
                boolean found = false;
                // over there, search for the document reference root in the
                // combined trie/reference structure
                do {
                    if (wtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(DOCUMENTS_REF_ROOTELEMENT)) {
                        found = true;
                        break;
                    }
                } while (wtx.moveToRightSibling());

                // if no document reference root was found, insert it, otherwise
                // go for it.
                if (!found) {
                    wtx.insertElementAsFirstChild(new QName(DOCUMENTS_REF_ROOTELEMENT));
                }

            }
            if (wtx.moveToFirstChild()) {
                if (!wtx.moveToFirstChild()) {
                    throw new IllegalStateException(
                        "At each reference, there must be a corresponding text containing the reference!");
                }
                final long lastKey = Long.parseLong(wtx.getValueOfCurrentNode());
                if (lastKey != mCurrentDocKey) {
                    wtx.moveToParent();
                    wtx.moveToParent();
                    wtx.insertElementAsFirstChild(new QName(DOCUMENTREFERENCE_ELEMENTNAME));
                    wtx.insertTextAsFirstChild(new StringBuilder().append(mCurrentDocKey).toString());
                }
            } else {
                wtx.insertElementAsFirstChild(new QName(DOCUMENTREFERENCE_ELEMENTNAME, EMPTY_STRING));
                wtx.insertTextAsFirstChild(new StringBuilder().append(mCurrentDocKey).toString());
            }
        }
    }

    /**
     * Finishing the input for indexing one source. This results in a change of
     * the identifier stored with every field in the structure to provide an
     * unique identifier.
     * 
     * @throws AbsTTException
     *             if can't finish index input.
     * @return the index revision number.
     */
    public long finishIndexInput() throws AbsTTException {
        if (mRtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction)mRtx;
            try {
                wtx.commit();
            } catch (final TTIOException exc) {
                LOGWRAPPER.error(exc);
                throw new IllegalStateException(exc);
            }
            wtx.moveToDocumentRoot();
            return mIndexRev;
        } else {
            return -1;
        }
    }

    /**
     * Closing the index structure. No automatic commit is performed.
     */
    public void close() {
        if (mRtx instanceof IWriteTransaction) {
            try {
                ((IWriteTransaction)mRtx).commit();
                mRtx.close();
                mIndexSession.close();
            } catch (final AbsTTException exc) {
                LOGWRAPPER.error(exc);
                throw new IllegalStateException(exc);
            }
        }

    }

    /**
     * Getting all leave nodes for a doc-root node. If a leave in the trie is
     * reached, the following method is getting all the leaves in the document
     * structure and gives back the results as a list of keys.
     * 
     * @param mDocLong
     *            the root of the document-root structure
     * @return the results as a list of nodes
     */
    public List<Long> getDocumentsForDocRoot(final long mDocLong) {

        final List<Long> returnVal = new LinkedList<Long>();
        mRtx.moveTo(mDocLong);
        mRtx.moveToFirstChild();
        do {
            // got doc-ref-root, taking all childs
            if (mRtx.getNode().getNameKey() == NamePageHash.generateHashForString(DOCUMENTS_REF_ROOTELEMENT)) {
                mRtx.moveToFirstChild();
                do {
                    mRtx.moveToFirstChild();
                    final long key = Long.parseLong(mRtx.getValueOfCurrentNode());
                    returnVal.add(key);
                    mRtx.moveToParent();
                } while (mRtx.moveToRightSibling());
                break;
            }
        } while (mRtx.moveToRightSibling());

        return returnVal;

    }

    /**
     * Getting the root of the document tree structure for a given term. The
     * search start at the root of the trie and searches the whole term in the
     * trie. If nothing is found, the <code>ENodes.UNKOWN</code> key is given
     * back.
     * 
     * @param mTerm
     *            to be searched in the trie structure
     * @return the root for the document key
     * @throws AbsTTException
     *             for handling treetank errors
     */
    public long getDocRootForTerm(final String mTerm) throws AbsTTException {
        return TrieNavigator.getDocRootInTrie(mRtx, mTerm);

    }

    /**
     * Getting all the ancestors related to a key in the document structure.
     * 
     * @param mKey
     *            the key in the structure
     * @return a stack containing all the ancestors
     */
    public Stack<String> getAncestors(final long mKey) {
        this.mRtx.moveTo(mKey);
        return DocumentTreeNavigator.getDocElements(mRtx);
    }

    // private final void checkAndUpdateLeafAttribute() {
    // if (rtx instanceof IWriteTransaction) {
    // final IWriteTransaction wtx = (IWriteTransaction) rtx;
    // // if the leaf has already attributes, check against the last revs
    // if (wtx.getNode().getAttributeCount() > 0) {
    // wtx.moveToAttribute(0);
    // final long indexRev = Long.parseLong(wtx
    // .getValueOfCurrentNode());
    // if (indexRev < this.indexRev + 1) {
    // wtx.remove();
    // wtx
    // .insertAttribute(INDEXREV_ATTRIBUTEKEY, "",
    // new StringBuilder().append(indexRev + 1)
    // .toString());
    // }
    // } else {
    // // ...or insert a new attribute
    // wtx.insertAttribute(INDEXREV_ATTRIBUTEKEY, "",
    // new StringBuilder().append(indexRev + 1).toString());
    // }
    // wtx.moveToParent();
    // }
    // }

    IReadTransaction getTrans() {
        return mRtx;
    }

    /**
     * Initialising basic structure.
     * 
     * @param mRtx
     *            Read transaction.
     * @throws AbsTTException
     *             for handling treetank errors.
     */
    static void initialiseBasicStructure(final IReadTransaction mRtx) throws AbsTTException {
        if (mRtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction)mRtx;
            wtx.moveToDocumentRoot();
            wtx.insertElementAsFirstChild(new QName(METAROOT_ELEMENTNAME));
            wtx.insertElementAsRightSibling(new QName(TRIEROOT_ELEMENTNAME));
            wtx.insertElementAsRightSibling(new QName(DOCUMENTROOT_ELEMENTNAME));
            try {
                wtx.commit();
            } catch (final TTIOException exc) {
                LOGWRAPPER.error(exc);
                throw new IllegalStateException(exc);
            }
        }

    }
    // private final void cleanUpStructure() {
    // if (rtx instanceof IWriteTransaction) {
    // final IWriteTransaction wtx = (IWriteTransaction) rtx;
    //
    // moveToTrieRoot();
    //
    // final IAxis postorder = new PostOrderAxis(wtx);
    //
    // while (postorder.hasNext()) {
    //
    // boolean leaf = false;
    // for (int i = 0, inidices = wtx.getNode().getAttributeCount(); i <
    // inidices; i++) {
    // wtx.moveToAttribute(i);
    // if (wtx.getNode().getNameKey() == NamePageHash
    // .generateHashForString(INDEXREV_ATTRIBUTEKEY)) {
    // leaf = true;
    // final long nodeRev = Long.parseLong(wtx
    // .getValueOfCurrentNode());
    // if (nodeRev < indexRev + 1) {
    // wtx.moveToParent();
    // wtx.remove();
    // leaf = false;
    // // where does break end up
    // break;
    // }
    // }
    // wtx.moveToParent();
    // }
    // if (!leaf
    // && !wtx.getNode().hasFirstChild()
    // && !(wtx.getNode().getNameKey() == NamePageHash
    // .generateHashForString(TRIEROOT_ELEMENTNAME))) {
    // wtx.remove();
    // }
    //
    // postorder.next();
    // }
    //
    // }
    // }

}
