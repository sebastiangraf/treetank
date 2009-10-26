package com.treetank.service.revIndex;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankFrameworkException;
import com.treetank.exception.TreetankIOException;
import com.treetank.session.Session;
import com.treetank.utils.IConstants;
import com.treetank.utils.NamePageHash;

/**
 * Revisioned Index Structure. Consisting of a trie and a document-trie. Both
 * structures are merged on one treetank structure and can be distinguished with
 * the help of different namespaces.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class RevIndex {

    final static String EMPTY_STRING = "";

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

    private long indexRev;

    private long currentDocKey = -1;

    /**
     * {@link ISession} to the index structure
     */
    private final ISession indexSession;
    /**
     * {@link IWriteTransaction} to trie session.
     */
    private final IReadTransaction rtx;

    /**
     * Private constructor, Access should take place over
     * {@link RevIndex#getIndex(File)}.
     * 
     * @param paramIndexFolder
     *            folder to be access.
     * @throws TreetankFrameworkException
     */
    public RevIndex(final File index, final long rev)
            throws TreetankFrameworkException, IOException {
        indexSession = Session.beginSession(index);
        if (rev < 0) {
            rtx = indexSession.beginWriteTransaction(
                    IConstants.COMMIT_THRESHOLD, 0);
        } else {
            final IReadTransaction rtx = indexSession.beginReadTransaction();
            long ttRev = MetaTreeNavigator.getIndexRev(rtx, rev);
            final long bla = rtx.getRevisionNumber();
            // TODO quick fix, has to be verified!!
            if (ttRev > bla) {
                ttRev = bla;
            }
            rtx.close();
            this.rtx = indexSession.beginReadTransaction(ttRev);
        }
    }

    public final void compact(final double threshold) {

    }

    /**
     * Inserting the next node in the document tree
     * 
     * @param docs
     *            stack with order to be inserted.
     */
    public final void insertNextNode(final Stack<String> docs) {
        indexRev = MetaTreeNavigator.getPersistentNumber(rtx);
        if (rtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction) rtx;
            currentDocKey = DocumentTreeNavigator.adaptDocTree(wtx, docs);
        }
    }

    /**
     * Indexing a term with the given Stack full of uuids of an hierarchical
     * structure.
     * 
     * @param term
     *            the term to be indexed.
     * @param names
     *            elements of the document to be indexed
     */
    public final void insertNextTermForCurrentNode(final String term) {
        // check if rtx is instance of WriteTransaction
        if (rtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction) rtx;

            // Navigating in the trie
            TrieNavigator.adaptTrie(wtx, term);

            // If the trienode has no child, insert the root for the
            // references..
            if (!wtx.moveToFirstChild()) {
                wtx.insertElementAsFirstChild(DOCUMENTS_REF_ROOTELEMENT,
                        EMPTY_STRING);
            } else
            // ..otherwise go to the first child..
            {
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
                    wtx.insertElementAsFirstChild(DOCUMENTS_REF_ROOTELEMENT,
                            EMPTY_STRING);
                }

            }
            if (wtx.moveToFirstChild()) {
                if (!wtx.moveToFirstChild()) {
                    throw new IllegalStateException(
                            "At each reference, there must be a corresponding text containing the reference!");
                }
                final long lastKey = Long
                        .parseLong(wtx.getValueOfCurrentNode());
                if (lastKey != currentDocKey) {
                    wtx.moveToParent();
                    wtx.moveToParent();
                    wtx.insertElementAsFirstChild(
                            DOCUMENTREFERENCE_ELEMENTNAME, EMPTY_STRING);
                    wtx.insertTextAsFirstChild(new StringBuilder().append(
                            currentDocKey).toString());
                }
            } else {
                wtx.insertElementAsFirstChild(DOCUMENTREFERENCE_ELEMENTNAME,
                        EMPTY_STRING);
                wtx.insertTextAsFirstChild(new StringBuilder().append(
                        currentDocKey).toString());
            }
        }
    }

    /**
     * Finishing the input for indexing one source. This results in a change of
     * the identifier stored with every field in the structure to provide an
     * unique identifier.
     * 
     * @return the index revision number.
     */
    public final long finishIndexInput() {
        if (rtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction) rtx;
            try {
                wtx.commit();
            } catch (final TreetankIOException exc) {
                throw new IllegalStateException(exc);
            }
            indexRev = MetaTreeNavigator.adaptMetaTree(wtx, indexRev);
            wtx.moveToDocumentRoot();
            return indexRev;
        } else {
            return -1;
        }
    }

    /**
     * Closing the index structure. No automatic commit is performed.
     */
    public final void close() {
        if (rtx instanceof IWriteTransaction) {
            try {
                ((IWriteTransaction) rtx).commit();
            } catch (final TreetankIOException exc) {
                throw new IllegalStateException(exc);
            }
        }
        rtx.close();
        indexSession.close();
    }

    /**
     * Getting all leave nodes for a doc-root node. If a leave in the trie is
     * reached, the following method is getting all the leaves in the document
     * structure and gives back the results as a list of keys.
     * 
     * @param docLong
     *            the root of the document-root structure
     * @return the results as a list of nodes
     */
    public final LinkedList<Long> getDocumentsForDocRoot(final long docLong) {

        final LinkedList<Long> returnVal = new LinkedList<Long>();
        rtx.moveTo(docLong);
        rtx.moveToFirstChild();
        do {
            // got doc-ref-root, taking all childs
            if (rtx.getNode().getNameKey() == NamePageHash
                    .generateHashForString(DOCUMENTS_REF_ROOTELEMENT)) {
                rtx.moveToFirstChild();
                do {
                    rtx.moveToFirstChild();
                    final long key = Long
                            .parseLong(rtx.getValueOfCurrentNode());
                    returnVal.add(key);
                    rtx.moveToParent();
                } while (rtx.moveToRightSibling());
                break;
            }
        } while (rtx.moveToRightSibling());

        return returnVal;

    }

    /**
     * Getting the root of the document tree structure for a given term. The
     * search start at the root of the trie and searches the whole term in the
     * trie. If nothing is found, the {@link IConstants#UNKNOWN} key is given
     * back.
     * 
     * @param term
     *            to be searched in the trie structure
     * @return the root for the document key
     */
    public final long getDocRootForTerm(final String term) {
        return TrieNavigator.getDocRootInTrie(rtx, term);

    }

    /**
     * Getting all the ancestors related to a key in the document structure
     * 
     * @param key
     *            the key in the structure
     * @return a stack containing all the ancestors
     */
    public final Stack<String> getAncestors(final long key) {
        this.rtx.moveTo(key);
        return DocumentTreeNavigator.getDocElements(rtx);
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
        return rtx;
    }

    /**
     * Initialising basic structure.
     */
    static final void initialiseBasicStructure(final IReadTransaction rtx) {
        if (rtx instanceof IWriteTransaction) {
            final IWriteTransaction wtx = (IWriteTransaction) rtx;
            wtx.moveToDocumentRoot();
            wtx.insertElementAsFirstChild(METAROOT_ELEMENTNAME, "");
            wtx.insertElementAsRightSibling(TRIEROOT_ELEMENTNAME, "");
            wtx.insertElementAsRightSibling(DOCUMENTROOT_ELEMENTNAME, "");
            try {
                wtx.commit();
            } catch (TreetankIOException exc) {
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