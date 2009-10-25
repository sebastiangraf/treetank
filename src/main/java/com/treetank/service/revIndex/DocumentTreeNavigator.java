package com.treetank.service.revIndex;

import java.util.Stack;

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.utils.IConstants;
import com.treetank.utils.NamePageHash;

public final class DocumentTreeNavigator {

    static long adaptDocTree(final IWriteTransaction wtx,
            final Stack<String> names) {
        moveToDocumentStructureRoot(wtx);
        long currentDocKey = IConstants.UNKNOWN;
        // iterating over all names in hierarchical order
        while (!names.empty()) {
            final String name = names.pop();

            // if firstChild is not existing,...
            if (!wtx.moveToFirstChild()) {
                // ..inserting it...
                wtx.insertElementAsFirstChild(RevIndex.DOCUMENT_ELEMENT,
                        RevIndex.EMPTY_STRING);
                wtx.insertAttribute(RevIndex.DOCUMENT_NODE_ATTIBUTEKEY,
                        RevIndex.EMPTY_STRING, name);
                wtx.moveToParent();
            }
            // Check if there was already a document on the sibling axis...
            boolean found = false;
            do {
                // ...and check the name against the current document name
                // for each sibling..
                if (wtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(RevIndex.DOCUMENT_ELEMENT)) {
                    // ..and break up if it is canceled
                    if (!wtx.moveToAttribute(0)) {

                        throw new IllegalStateException();
                    }
                    if (wtx.getValueOfCurrentNode().hashCode() == name
                            .hashCode()) {
                        found = true;
                    }
                    wtx.moveToParent();
                    if (found) {
                        currentDocKey = wtx.getNode().getNodeKey();
                        break;
                    }
                }
            } while (wtx.moveToRightSibling());
            // ...if there hasn't be an element, insert the
            if (!found) {
                wtx.insertElementAsRightSibling(RevIndex.DOCUMENT_ELEMENT,
                        RevIndex.EMPTY_STRING);
                wtx.insertAttribute(RevIndex.DOCUMENT_NODE_ATTIBUTEKEY,
                        RevIndex.EMPTY_STRING, name);
                wtx.moveToParent();

                currentDocKey = wtx.getNode().getNodeKey();
            }
        }
        return currentDocKey;
    }

    static final Stack<String> getDocElements(final IReadTransaction rtx) {
        final Stack<String> returnVal = new Stack<String>();
        do {
            rtx.moveToAttribute(0);
            returnVal.add(rtx.getValueOfCurrentNode());
            rtx.moveToParent();
        } while (rtx.moveToParent()
                && NamePageHash
                        .generateHashForString(RevIndex.DOCUMENTROOT_ELEMENTNAME) != rtx
                        .getNode().getNameKey());

        return returnVal;
    }

    /**
     * Moving to documentstructure root
     */
    private static final void moveToDocumentStructureRoot(
            final IReadTransaction rtx) {
        rtx.moveToDocumentRoot();
        if (!rtx.getNode().hasFirstChild()) {
            RevIndex.initialiseBasicStructure(rtx);
        } else {
            rtx.moveToFirstChild();
            rtx.moveToRightSibling();
            rtx.moveToRightSibling();
        }
    }
}
