package com.treetank.service.revIndex;

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.utils.NamePageHash;

public final class MetaTreeNavigator {

    /**
     * Getting the treetankRev for a given index rev from the metadata structure
     * 
     * @param rtx
     *            to be searched with
     * @param indexRev
     *            the rev of the index
     * @return the index of the treetank
     */
    static long getIndexRev(final IReadTransaction rtx, final long indexRev)
            throws TreetankException {
        moveToMetaRevRoot(rtx);

        long ttRev = -1;

        boolean correctIndexFound = false;
        do {
            boolean correctAttrFound = false;
            for (int i = 0; i < rtx.getNode().getAttributeCount()
                    && !correctAttrFound; i++) {
                rtx.moveToAttribute(i);
                if (rtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(RevIndex.INDEXREV_ATTRIBUTEKEY)) {
                    final long currentIndexRev = Long.parseLong(rtx
                            .getValueOfCurrentNode());
                    if (currentIndexRev == indexRev) {
                        correctIndexFound = true;
                    }
                    correctAttrFound = true;

                }
                rtx.moveToParent();
            }
        } while (!correctIndexFound && rtx.moveToRightSibling());

        for (int i = 0; i < rtx.getNode().getAttributeCount(); i++) {
            rtx.moveToAttribute(i);
            if (rtx.getNode().getNameKey() == NamePageHash
                    .generateHashForString(RevIndex.LASTTTREV_ATTRIBUTEKEY)) {
                ttRev = Long.parseLong(rtx.getValueOfCurrentNode());
                break;
            }
            rtx.moveToParent();
        }
        return ttRev;
    }

    static long adaptMetaTree(final IWriteTransaction wtx, final long revision)
            throws TreetankException {
        moveToMetaRevRoot(wtx);
        long indexRev = revision;
        wtx.insertElementAsFirstChild(RevIndex.META_ELEMENT, "");

        long lastRev = -1;
        if (wtx.getNode().hasRightSibling()) {
            wtx.moveToRightSibling();
            for (int i = 0; i < wtx.getNode().getAttributeCount(); i++) {
                wtx.moveToAttribute(i);
                if (wtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(RevIndex.LASTTTREV_ATTRIBUTEKEY)) {
                    lastRev = Long.parseLong(wtx.getValueOfCurrentNode());
                    wtx.moveToParent();
                    wtx.moveToLeftSibling();
                    break;
                }
                wtx.moveToParent();
            }
        }

        wtx.insertAttribute(RevIndex.INDEXREV_ATTRIBUTEKEY,
                RevIndex.EMPTY_STRING, new StringBuilder().append(indexRev + 1)
                        .toString());
        wtx.moveToParent();
        wtx.insertAttribute(RevIndex.FIRSTTTREV_ATTRIBUTEKEY,
                RevIndex.EMPTY_STRING, new StringBuilder().append(lastRev + 1)
                        .toString());
        wtx.moveToParent();
        wtx.insertAttribute(RevIndex.LASTTTREV_ATTRIBUTEKEY,
                RevIndex.EMPTY_STRING, new StringBuilder().append(
                        wtx.getRevisionNumber() + 1).toString());
        wtx.moveToParent();
        try {
            wtx.commit();
        } catch (final TreetankIOException exc) {
            throw new IllegalStateException(exc);
        }

        indexRev++;
        return indexRev;

    }

    static long getPersistentNumber(final IReadTransaction rtx)
            throws TreetankException {
        moveToMetaRevRoot(rtx);
        long indexRev = -1;
        if (!rtx.getNode().hasFirstChild()) {
            indexRev = 0;
        } else {
            // move to latest revision tag
            rtx.moveToFirstChild();
            for (int i = 0; i < rtx.getNode().getAttributeCount(); i++) {
                rtx.moveToAttribute(i);
                if (rtx.getNode().getNameKey() == NamePageHash
                        .generateHashForString(RevIndex.INDEXREV_ATTRIBUTEKEY)) {
                    indexRev = Long.parseLong(rtx.getValueOfCurrentNode());
                    break;
                }
            }
        }
        return indexRev;
    }

    /**
     * Moving to meta rec root. Inserting basic structure if not
     */
    private static void moveToMetaRevRoot(final IReadTransaction rtx)
            throws TreetankException {
        rtx.moveToDocumentRoot();
        if (!rtx.getNode().hasFirstChild()) {
            RevIndex.initialiseBasicStructure(rtx);
            rtx.moveToLeftSibling();
            rtx.moveToLeftSibling();
        } else {
            rtx.moveToFirstChild();
        }
    }

}
