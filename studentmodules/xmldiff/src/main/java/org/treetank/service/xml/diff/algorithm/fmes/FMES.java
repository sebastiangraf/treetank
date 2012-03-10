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
package org.treetank.service.xml.diff.algorithm.fmes;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.treetank.access.Database;
import org.treetank.access.WriteTransactionState;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IVisitor;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.ChildAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.LevelOrderAxis;
import org.treetank.axis.PostOrderAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENode;
import org.treetank.node.ElementNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.service.xml.diff.DiffFactory;
import org.treetank.service.xml.diff.algorithm.IImportDiff;

/**
 * Provides the fast match / edit script (fmes) tree to tree correction
 * algorithm as described in "Change detection in hierarchically structured
 * information" by S. Chawathe, A. Rajaraman, H. Garcia-Molina and J. Widom
 * Stanford University, 1996 ([CRGMW95]) <br>
 * FMES is used by the <a href="http://www.logilab.org/projects/xmldiff">python
 * script</a> xmldiff from Logilab. <br>
 * 
 * Based on the FMES version of Daniel Hottinger and Franziska Meyer.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class FMES implements IImportDiff {

    /** Determines if a reverse lookup has to be made. */
    enum EReverseMap {
        /** Yes, reverse lookup. */
        TRUE,

        /** No, normal lookup. */
        FALSE
    }

    /** Algorithm name. */
    private static final String NAME = "Fast Matching / Edit Script";

    /**
     * Matching Criterion 1. For the "good matching problem", the following
     * conditions must hold for leafs x and y:
     * <ul>
     * <li>label(x) == label(y)</li>
     * <li>compare(value(x), value(y)) <= FMESF</li>
     * </ul>
     * where FMESF is in the range [0,1] and compare() computes the cost of
     * updating a leaf node.
     */
    private static final double FMESF = 0.6;

    /**
     * Matching Criterion 2. For the "good matching problem", the following
     * conditions must hold inner nodes x and y:
     * <ul>
     * <li>label(x) == label(y)</li>
     * <li>|common(x,y)| / max(|x|, |y|) > FMESTHRESHOLD</li>
     * </ul>
     * where FMESTHRESHOLD is in the range [0.5, 1] and common(x,y) computes the
     * number of leafs that can be matched between x and y.
     */
    private static final double FMESTHRESHOLD = 0.4;

    // General note: we use IdentityHashMaps to annotate the nodes since == is
    // less expensive and more accurate for our use than x.equals(y).

    /**
     * Used by emitInsert: when inserting a whole subtree - keep track that
     * nodes are not inserted multiple times.
     */
    private final Map<INode, Boolean> mAlreadyInserted;

    /**
     * This is the matching M between nodes as described in the paper.
     */
    private transient Matching mFastMatching;

    /**
     * This is the total matching M' between nodes as described in the paper.
     */
    private transient Matching mTotalMatching;

    /**
     * Stores the in-order property for each node One map is sufficient for both
     * trees since the reference (identity) is compared.
     */
    private final Map<INode, Boolean> mInOrder;

    /**
     * Number of descendants in subtree of node.
     */
    private final Map<INode, Long> mDescendants;

    /** {@link IVisitor} implementation on old revision. */
    private final IVisitor mOldRevVisitor;

    /** {@link IVisitor} implementation on new revision. */
    private final IVisitor mNewRevVisitor;

    /** {@link IVisitor} implementation to collect label/nodes on old revision. */
    private final LabelFMESVisitor mLabelOldRevVisitor;

    /** {@link IVisitor} implementation to collect label/nodes on new revision. */
    private final LabelFMESVisitor mLabelNewRevVisitor;

    /**
     * Constructor.
     * 
     * @param paramOldFile
     *            Treetank {@link File} of the resource to update
     * @param paramNewFile
     *            Treetank {@link File} of the new shreddered revision
     * @throws AbsTTException
     *             if setup of Treetank fails
     */
    public FMES(final File paramOldFile, final File paramNewFile) throws AbsTTException {
        mDescendants = new IdentityHashMap<INode, Long>();
        mInOrder = new IdentityHashMap<INode, Boolean>();
        mAlreadyInserted = new IdentityHashMap<INode, Boolean>();

        final DatabaseConfiguration oldConfig = new DatabaseConfiguration(paramOldFile);
        Database.createDatabase(oldConfig);
        final IDatabase databaseOld = Database.openDatabase(paramOldFile);
        databaseOld.createResource(new ResourceConfiguration.Builder(DiffFactory.RESOURCENAME, oldConfig)
            .build());
        final ISession sessionOld =
            databaseOld.getSession(new SessionConfiguration.Builder(DiffFactory.RESOURCENAME).build());
        final IWriteTransaction wtx = sessionOld.beginWriteTransaction();

        final DatabaseConfiguration newConfig = new DatabaseConfiguration(paramNewFile);
        Database.createDatabase(new DatabaseConfiguration(paramNewFile));
        final IDatabase databaseNew = Database.openDatabase(paramNewFile);
        databaseNew.createResource(new ResourceConfiguration.Builder(DiffFactory.RESOURCENAME, newConfig)
            .build());
        final ISession sessionNew =
            databaseNew.getSession(new SessionConfiguration.Builder(DiffFactory.RESOURCENAME).build());
        final IReadTransaction rtx = sessionNew.beginWriteTransaction();

        mOldRevVisitor = new FMESVisitor(sessionOld, mInOrder, mDescendants);
        mNewRevVisitor = new FMESVisitor(sessionNew, mInOrder, mDescendants);

        mLabelOldRevVisitor = new LabelFMESVisitor(sessionOld);
        mLabelNewRevVisitor = new LabelFMESVisitor(sessionNew);

        diff(wtx, rtx);
    }

    /**
     * Do the diff.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    @Override
    public void diff(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        init(paramWtx, paramRtx);
        mFastMatching = fastMatch(paramWtx, paramRtx);
        mTotalMatching = new Matching(mFastMatching);
        firstFMESStep(paramWtx, paramRtx);
        secondFMESStep(paramWtx, paramRtx);
    }

    /**
     * First step of the edit script algorithm. Combines the update, insert,
     * align and move phases.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void firstFMESStep(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        // 2. Iterate over new shreddered file
        for (final AbsAxis axis = new LevelOrderAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final IStructNode node = axis.getTransaction().getStructuralNode();
            final long nodeKey = node.getNodeKey();
            if (node.getKind() == ENode.ELEMENT_KIND) {
                final ElementNode element = (ElementNode)node;
                if (element.getAttributeCount() > 0) {
                    for (int i = 0; i < element.getAttributeCount(); i++) {
                        axis.getTransaction().moveToAttribute(i);
                        doFirstFSMEStep(paramWtx, paramRtx);
                        axis.getTransaction().moveTo(nodeKey);
                    }
                }
                if (element.getNamespaceCount() > 0) {
                    for (int i = 0; i < element.getNamespaceCount(); i++) {
                        axis.getTransaction().moveToNamespace(i);
                        doFirstFSMEStep(paramWtx, paramRtx);
                        axis.getTransaction().moveTo(nodeKey);
                    }
                }
            }

            axis.getTransaction().moveTo(nodeKey);
        }
    }

    /**
     * Do the actual first step of FSME.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void doFirstFSMEStep(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        // 2(a) - Parent of x.
        final INode x = paramRtx.getNode();
        paramRtx.moveToParent();
        assert paramRtx.getNode() instanceof IStructNode;
        final INode y = paramRtx.getStructuralNode();

        final INode z = mTotalMatching.reversePartner(y);
        INode w = mTotalMatching.reversePartner(x);

        paramWtx.moveToDocumentRoot();
        // 2(b) - insert
        if (w == null) {
            mInOrder.put(x, true);
            final int k = findPos(x, paramWtx, paramRtx);
            // System.err.println("Node: " + x.getNodeType() + ", " +
            // x.getNodeName() + " -> " +
            // x.getNodeValue() + ", Position: " + k);
            w = emitInsert(x, z, k, paramWtx, paramRtx);
        } else if (!x.equals(paramWtx.getNode())) {
            // 2(c) not the root.
            paramRtx.moveTo(w.getNodeKey());
            paramRtx.moveToParent();
            final INode v = paramRtx.getNode();
            if (!nodeValuesEqual(w, x, paramWtx, paramRtx)) {
                emitUpdate(w, x, paramWtx, paramRtx);
            }
            if (!mTotalMatching.contains(v, y)) {
                mTotalMatching.add(w, w); // FIXME?
                mInOrder.put(x, true);
                final int k = findPos(x, paramWtx, paramRtx);
                // System.err.println("MatchContainedMove: " + k);
                emitMove(w, z, k, paramWtx, paramRtx);
            }
        }

        alignChildren(w, x, paramWtx, paramRtx);
    }

    /**
     * Second step of the edit script algorithm. This is the delete phase.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void secondFMESStep(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        try {
            for (final AbsAxis axis = new DescendantAxis(paramWtx, true); axis.hasNext(); axis.next()) {
                final IStructNode node = axis.getTransaction().getStructuralNode();
                if (mTotalMatching.partner(node) == null) {
                    paramWtx.remove();
                }

                if (node.getKind() == ENode.ELEMENT_KIND) {
                    final long nodeKey = node.getNodeKey();
                    final ElementNode element = (ElementNode)node;
                    paramWtx.moveTo(nodeKey);
                    for (int i = 0; i < element.getAttributeCount(); i++) {
                        paramWtx.moveToAttribute(i);
                        if (mTotalMatching.partner(node) == null) {
                            paramWtx.remove();
                        }
                        paramWtx.moveTo(nodeKey);
                    }
                    for (int i = 0; i < element.getNamespaceCount(); i++) {
                        paramWtx.moveToNamespace(i);
                        if (mTotalMatching.partner(node) == null) {
                            mInOrder.put(paramWtx.getNode(), true);
                        }
                        paramWtx.moveTo(nodeKey);
                    }
                }
            }
        } catch (final AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Alignes the children of a node x according the the children of node w.
     * 
     * @param paramW
     *            node in the first document
     * @param paramX
     *            node in the second document
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void alignChildren(final INode paramW, final INode paramX, final IWriteTransaction paramWtx,
        final IReadTransaction paramRtx) {
        assert paramW != null;
        assert paramX != null;
        assert paramWtx != null;
        assert paramRtx != null;

        paramWtx.moveTo(paramW.getNodeKey());
        paramRtx.moveTo(paramX.getNodeKey());

        // Mark all children of w and all children of x "out of order".
        for (final AbsAxis axis = new ChildAxis(paramWtx); axis.hasNext(); axis.next()) {
            mInOrder.put(axis.getTransaction().getNode(), false);
        }
        for (final AbsAxis axis = new ChildAxis(paramRtx); axis.hasNext(); axis.next()) {
            mInOrder.put(axis.getTransaction().getNode(), false);
        }

        // 2
        final List<INode> first = commonChildren(paramW, paramX, paramWtx, paramRtx, EReverseMap.FALSE);
        final List<INode> second = commonChildren(paramX, paramW, paramRtx, paramWtx, EReverseMap.TRUE);
        // 3 && 4
        List<Pair<INode, INode>> s = Util.longestCommonSubsequence(first, second, new IComparator<INode>() {
            /** {@inheritDoc} */
            @Override
            public boolean isEqual(final INode paramX, final INode paramY) {
                return mTotalMatching.contains(paramX, paramY);
            }
        });
        // 5
        final Map<INode, INode> seen = new IdentityHashMap<INode, INode>();
        for (final Pair<INode, INode> p : s) {
            mInOrder.put(p.mFirst, true);
            mInOrder.put(p.mSecond, true);
            seen.put(p.mFirst, p.mSecond);
        }
        // 6
        for (final INode a : first) {
            final INode b = mFastMatching.partner(a);
            if (!(seen.get(a) == b) && !mInOrder.get(a)) { // (a, b) \notIn S
                final int k = findPos(b, paramWtx, paramRtx);
                // System.err.println("Move in align children: " + k);
                emitMove(a, paramW, k, paramWtx, paramRtx);
                mInOrder.put(a, true);
                mInOrder.put(b, true);
            }
        }
    }

    /**
     * The sequence of children of n whose partners are children of o. This is
     * used by alignChildren().
     * 
     * @param paramN
     *            parent node in a document tree
     * @param paramO
     *            corresponding parent node in the other tree
     * @param paramFirstRtx
     *            {@link IReadTransaction} on paramN node
     * @param paramSecondRtx
     *            {@link IReadTransaction} on paramO node
     * @param paramReverse
     *            determines if...
     * @return {@link List} of common child nodes
     */
    private List<INode> commonChildren(final INode paramN, final INode paramO,
        final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx,
        final EReverseMap paramReverse) {
        assert paramN != null;
        assert paramO != null;
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;
        assert paramReverse != null;
        final List<INode> retVal = new LinkedList<INode>();
        paramFirstRtx.moveTo(paramN.getNodeKey());
        if (paramFirstRtx.getStructuralNode().hasFirstChild()) {
            paramFirstRtx.moveToFirstChild();

            do {
                INode partner;
                if (paramReverse == EReverseMap.TRUE) {
                    partner = mTotalMatching.reversePartner(paramFirstRtx.getNode());
                } else {
                    partner = mTotalMatching.partner(paramFirstRtx.getNode());
                }

                if (partner != null) {
                    paramSecondRtx.moveTo(partner.getNodeKey());
                    paramSecondRtx.moveToParent();
                    if (paramSecondRtx.getNode().equals(paramO)) {
                        retVal.add(paramFirstRtx.getNode());
                    }
                }
            } while (paramFirstRtx.getStructuralNode().hasRightSibling()
                && paramFirstRtx.moveToRightSibling());
        }
        return retVal;
    }

    /**
     * Emits the move of node "child" to the paramPos-th child of node "parent".
     * 
     * @param paramChild
     *            child node to move
     * @param paramParent
     *            node where to insert the moved subtree
     * @param paramPos
     *            position among the childs to move to
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void emitMove(final INode paramChild, final INode paramParent, final int paramPos,
        final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        assert paramChild != null;
        assert paramParent != null;
        assert paramPos >= 0;
        assert paramWtx != null;
        assert paramRtx != null;

        paramRtx.moveTo(paramChild.getNodeKey());
        paramWtx.moveTo(paramParent.getNodeKey());

        if (paramRtx.getNode().getKind() == ENode.ATTRIBUTE_KIND
            || paramRtx.getNode().getKind() == ENode.NAMESPACE_KIND) {
            // Attribute- and namespace-nodes can't be moved.
            return;
        }

        try {
            if (paramPos == 0) {
                paramWtx.moveSubtreeToFirstChild(paramChild.getNodeKey());

            } else {
                assert paramWtx.getStructuralNode().hasFirstChild();
                paramWtx.moveToFirstChild();

                for (int i = 0; i < paramPos - 1; i++) {
                    assert paramWtx.getStructuralNode().hasRightSibling();
                    paramWtx.moveToRightSibling();
                }

                paramWtx.moveSubtreeToRightSibling(paramChild.getNodeKey());
            }
        } catch (final AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Emit an update.
     * 
     * @param paramFromNode
     *            the node to update
     * @param paramToNode
     *            the new node
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void emitUpdate(final INode paramFromNode, final INode paramToNode,
        final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        assert paramFromNode != null;
        assert paramToNode != null;
        assert paramWtx != null;
        assert paramRtx != null;

        paramWtx.moveTo(paramFromNode.getNodeKey());
        paramRtx.moveTo(paramToNode.getNodeKey());

        try {
            switch (paramToNode.getKind()) {
            case ELEMENT_KIND:
            case ATTRIBUTE_KIND:
                assert paramFromNode.getKind() == ENode.ELEMENT_KIND
                    || paramFromNode.getKind() == ENode.ATTRIBUTE_KIND;
                paramWtx.setQName(paramRtx.getQNameOfCurrentNode());

                if (paramFromNode.getKind() == ENode.ATTRIBUTE_KIND) {
                    paramWtx.setValue(paramRtx.getValueOfCurrentNode());
                }
                break;
            case TEXT_KIND:
                assert paramFromNode.getKind() == ENode.TEXT_KIND;
                paramWtx.setValue(paramRtx.getValueOfCurrentNode());
                break;
            default:
            }
        } catch (final AbsTTException e) {
            e.getStackTrace();
        }
    }

    /**
     * Emit an insert operation.
     * 
     * @param paramParent
     *            parent of the current {@link INode} implementation reference
     *            to insert
     * @param paramChild
     *            the current node to insert
     * @param paramPos
     *            position of the insert
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     * @return inserted {@link INode} implementation reference
     */
    private INode emitInsert(final INode paramParent, final INode paramChild, final int paramPos,
        final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        paramWtx.moveTo(paramParent.getNodeKey());
        paramRtx.moveTo(paramChild.getNodeKey());

        // Determines if node has been already inserted (for subtrees).
        if (mAlreadyInserted.containsKey(paramChild)) {
            return paramChild; // actually child'
        }

        try {
            switch (paramChild.getKind()) {
            case ATTRIBUTE_KIND:
                paramWtx.insertAttribute(paramRtx.getQNameOfCurrentNode(), paramRtx.getValueOfCurrentNode());
                break;
            case NAMESPACE_KIND:
                paramWtx.insertNamespace(paramRtx.getQNameOfCurrentNode());
                break;
            default:
                // In case of other node types.
                if (paramPos == 0) {
                    switch (paramChild.getKind()) {
                    case ELEMENT_KIND:
                        paramWtx.insertElementAsFirstChild(paramRtx.getQNameOfCurrentNode());
                        insertNonStructural(paramRtx, paramWtx);
                        // TODO: Insert whole subtree.
                        break;
                    case TEXT_KIND:
                        paramWtx.insertTextAsFirstChild(paramRtx.getValueOfCurrentNode());
                        break;
                    default:
                        // Already inserted.
                    }
                } else {
                    assert paramWtx.getStructuralNode().hasFirstChild();
                    paramWtx.moveToFirstChild();
                    for (int i = 1; i < paramPos - 1; i++) {
                        assert paramWtx.getStructuralNode().hasRightSibling();
                        assert mInOrder.get(paramWtx.getNode()) != null;
                        assert mInOrder.get(paramWtx.getNode());
                        paramWtx.moveToRightSibling();
                    }
                    switch (paramChild.getKind()) {
                    case ELEMENT_KIND:
                        paramWtx.insertElementAsRightSibling(paramRtx.getQNameOfCurrentNode());
                        insertNonStructural(paramRtx, paramWtx);
                        // TODO: Insert whole subtree.
                        break;
                    case TEXT_KIND:
                        paramWtx.insertTextAsRightSibling(paramRtx.getValueOfCurrentNode());
                        break;
                    default:
                        // Already inserted.
                    }
                }
            }
        } catch (final AbsTTException e) {
            e.printStackTrace();
        }

        // Mark all nodes in subtree as inserted.
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final IStructNode node = axis.getTransaction().getStructuralNode();
            mAlreadyInserted.put(node, true);
            mInOrder.put(node, true);
            final long nodeKey = node.getNodeKey();
            if (node.getKind() == ENode.ELEMENT_KIND) {
                final ElementNode element = (ElementNode)node;
                if (element.getAttributeCount() > 0) {
                    for (int i = 0; i < element.getAttributeCount(); i++) {
                        axis.getTransaction().moveToAttribute(i);
                        mAlreadyInserted.put(axis.getTransaction().getNode(), true);
                        mInOrder.put(axis.getTransaction().getNode(), true);
                        axis.getTransaction().moveTo(nodeKey);
                    }
                }
                if (element.getNamespaceCount() > 0) {
                    for (int i = 0; i < element.getNamespaceCount(); i++) {
                        axis.getTransaction().moveToNamespace(i);
                        mAlreadyInserted.put(axis.getTransaction().getNode(), true);
                        mInOrder.put(axis.getTransaction().getNode(), true);
                        axis.getTransaction().moveTo(nodeKey);
                    }
                }
            }

            axis.getTransaction().moveTo(nodeKey);
        }

        return paramWtx.getNode();
    }

    /**
     * Insert attributes or namespaces.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     * @throws AbsTTException
     *             if insertion fails
     */
    private void insertNonStructural(final IReadTransaction paramRtx, final IWriteTransaction paramWtx)
        throws AbsTTException {
        assert paramRtx != null;
        assert paramWtx != null;
        assert paramRtx.getStructuralNode().getKind() == ENode.ELEMENT_KIND;
        assert paramWtx.getStructuralNode().getKind() == ENode.ELEMENT_KIND;
        final ElementNode element = (ElementNode)paramRtx.getStructuralNode();
        if (element.getAttributeCount() > 0) {
            for (int i = 0; i < element.getAttributeCount(); i++) {
                paramRtx.moveToAttribute(i);
                paramWtx.insertAttribute(paramRtx.getQNameOfCurrentNode(), paramRtx.getValueOfCurrentNode());
                paramRtx.moveToParent();
            }
        }
        if (element.getNamespaceCount() > 0) {
            for (int i = 0; i < element.getNamespaceCount(); i++) {
                paramRtx.moveToNamespace(i);
                paramWtx.insertNamespace(paramRtx.getQNameOfCurrentNode());
                paramRtx.moveToParent();
            }
        }

    }

    /**
     * The position of node x in the destination tree (tree2).
     * 
     * @param paramX
     *            a node in the second document
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     * @return it's position, with respect to already inserted/deleted nodes
     */
    private int
        findPos(final INode paramX, final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        final long nodeKey = paramRtx.getStructuralNode().getNodeKey();
        if (paramX.getKind() == ENode.ATTRIBUTE_KIND) {
            return -1;
        } else {
            // 1 - Let y = p(x) in T2.
            paramRtx.moveToParent();

            // 2 - If x is the leftmost child of y that is marked "in order",
            // return 0
            if (paramRtx.getStructuralNode().hasFirstChild()) {
                paramRtx.moveToFirstChild();
                boolean found = false;

                do {
                    final INode v = paramRtx.getStructuralNode();
                    if (mInOrder.get(v)) {
                        if (v == paramX) {
                            return 0;
                        } else {
                            found = true;
                        }
                    }
                } while (!found && paramRtx.getStructuralNode().hasRightSibling()
                    && paramRtx.moveToRightSibling());
            }

            // 3 - Find v \in T2 where v is the rightmost sibling of x
            // that is to the left of x and is marked "in order".
            paramRtx.moveTo(nodeKey);
            paramRtx.moveToLeftSibling();
            INode v = paramRtx.getStructuralNode();
            while (paramRtx.getStructuralNode().hasLeftSibling() && !mInOrder.get(v)) {
                paramRtx.moveToLeftSibling();
                v = paramRtx.getStructuralNode();
            }

            // Step 2 states that in ``in order'' node exists, but this is not
            // true.
            if (mInOrder.get(v) == null) {
                // Assume it is the first node (undefined in the paper).
                return 0;
            }

            // 4 - Let u be the partner of v in T1
            INode u = mTotalMatching.reversePartner(v);
            paramWtx.moveTo(u.getNodeKey());

            // Suppose u is the i-th child of its parent (counting from left to
            // right)
            // that is marked "in order". Return i+1
            int i = -1;
            while (u != null) {
                // true for original u? only count nodes marked as inOrder?!
                if (mInOrder.get(u) != null && mInOrder.get(u)) {
                    i++;
                }

                if (paramWtx.getStructuralNode().hasLeftSibling()) {
                    paramWtx.moveToLeftSibling();
                    u = paramWtx.getStructuralNode();
                } else {
                    u = null;
                }
            }
            return i + 1;
        }
    }

    /**
     * The fast match algorithm. Try to resolve the "good matching problem".
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     * @return {@link Matching} reference with matched nodes
     */
    private Matching fastMatch(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        assert paramWtx != null;
        assert paramRtx != null;

        // Chain all nodes with a given label l in tree T together.
        getLabels(paramWtx, mLabelOldRevVisitor);
        getLabels(paramRtx, mLabelNewRevVisitor);

        // Do the matching job on the leaf nodes.
        final Matching matching = new Matching(paramWtx, paramRtx);
        match(mLabelOldRevVisitor.getLeafLabels(), mLabelNewRevVisitor.getLeafLabels(), matching,
            new LeafEqual(paramWtx, paramRtx));

        // Remove roots ('/') from labels and append them to mapping.
        final Map<ENode, List<INode>> oldLabels = mLabelOldRevVisitor.getLabels();
        final Map<ENode, List<INode>> newLabels = mLabelNewRevVisitor.getLabels();
        oldLabels.remove(ENode.ROOT_KIND);
        newLabels.remove(ENode.ROOT_KIND);

        paramWtx.moveToDocumentRoot();
        paramRtx.moveToDocumentRoot();
        matching.add(paramWtx.getNode(), paramRtx.getNode());

        match(oldLabels, newLabels, matching, new InnerNodeEqual(matching, paramWtx, paramRtx));

        return matching;
    }

    /**
     * Actual matching.
     * 
     * @param paramOldLabels
     *            nodes in tree1, sorted by node type (element, attribute, text,
     *            comment, ...)
     * @param paramNewLabels
     *            nodes in tree2, sorted by node type (element, attribute, text,
     *            comment, ...)
     * @param paramMatching
     *            {@link Matching} reference
     * @param paramCmp
     *            functional class
     */
    private void match(final Map<ENode, List<INode>> paramOldLabels,
        final Map<ENode, List<INode>> paramNewLabels, final Matching paramMatching,
        final IComparator<INode> paramCmp) {
        final Set<ENode> labels = paramOldLabels.keySet();
        labels.retainAll(paramNewLabels.keySet()); // intersection

        // 2 - for each label do
        for (final ENode label : labels) {
            final List<INode> first = paramOldLabels.get(label); // 2(a)
            final List<INode> second = paramNewLabels.get(label); // 2(b)

            // 2(c)
            final List<Pair<INode, INode>> common = Util.longestCommonSubsequence(first, second, paramCmp);
            // Used to remove the nodes in common from s1 and s2 in step 2(e).
            final Map<INode, Boolean> seen = new IdentityHashMap<INode, Boolean>();

            // 2(d) - for each pair of nodes in the lcs: add to matching.
            for (Pair<INode, INode> p : common) {
                paramMatching.add(p.mFirst, p.mSecond);
                seen.put(p.mFirst, true);
                seen.put(p.mSecond, true);
            }

            // 2(e) (prepare) - remove nodes in common from s1, s2.
            for (Iterator<INode> i : new Iterator[] {
                first.iterator(), second.iterator()
            }) {
                while (i.hasNext()) {
                    if (seen.containsKey(i.next())) {
                        i.remove();
                    }
                }
            }

            // 2(e) - For each unmatched node x \in s1.
            for (final INode firstItem : first) {
                // If there is an unmatched node y \in s2.
                for (final INode secondItem : second) {
                    // Such that equal.
                    if (paramCmp.isEqual(firstItem, secondItem)) {
                        // 2(e)A
                        paramMatching.add(firstItem, secondItem);

                        // 2(e)B really needed?
                        first.remove(firstItem);
                        second.remove(secondItem);
                    }
                }
            }
        }
    }

    /**
     * Initialize data structures.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference on old
     *            revision
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference on new
     *            revision
     */
    private void init(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
        for (final AbsAxis axis = new PostOrderAxis(paramWtx); axis.hasNext(); axis.next()) {
            axis.getTransaction().getNode().acceptVisitor(mOldRevVisitor);
        }
        for (final AbsAxis axis = new PostOrderAxis(paramRtx); axis.hasNext(); axis.next()) {
            axis.getTransaction().getNode().acceptVisitor(mNewRevVisitor);
        }
    }

    /**
     * Creates a flat list of all nodes by doing an in-order-traversal. NOTE:
     * Since this is not a binary tree, we use post-order-traversal (wrong in
     * paper). For each node type (element, attribute, text, comment, ...) there
     * is a separate list.
     * 
     * @param paramRtx
     *            {@link IReadTransaction} reference
     * @param paramVisitor
     *            visitor used to save node type/list
     */
    private void getLabels(final IReadTransaction paramRtx, final LabelFMESVisitor paramVisitor) {
        assert paramRtx != null;

        for (final AbsAxis axis = new PostOrderAxis(paramRtx); axis.hasNext(); axis.next()) {
            axis.getTransaction().getNode().acceptVisitor(paramVisitor);
        }
    }

    /**
     * Compares the values of two nodes. Values are the text content, if the
     * nodes do have child nodes or the name for inner nodes such as element or
     * attribute (an attribute has one child: the value).
     * 
     * @param paramX
     *            first node
     * @param paramY
     *            second node
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference
     * @param paramWtx
     *            {@link IWriteTransaction} implementation reference
     * @return true iff the values of the nodes are equal
     */
    public boolean nodeValuesEqual(final INode paramX, final INode paramY, final IWriteTransaction paramWtx,
        final IReadTransaction paramRtx) {
        assert paramX != null;
        assert paramY != null;
        assert paramRtx != null;
        assert paramX.getKind() == paramY.getKind();

        final String a = getNodeValue(paramX, paramWtx);
        final String b = getNodeValue(paramY, paramRtx);

        return a == null ? b == null : a.equals(b);
    }

    /**
     * Get node value of current node.
     * 
     * @param paramNode
     *            node from which to get the value
     * @param paramRtx
     *            {@link IReadTransaction} implementation reference
     * @return string value of current node
     */
    private String getNodeValue(final INode paramNode, final IReadTransaction paramRtx) {
        assert paramNode != null;
        assert paramRtx != null;
        paramRtx.moveTo(paramNode.getNodeKey());
        String retVal = null;
        switch (paramNode.getKind()) {
        case ELEMENT_KIND:
        case ATTRIBUTE_KIND:
            retVal = WriteTransactionState.buildName(paramRtx.getQNameOfCurrentNode());
            break;
        case TEXT_KIND:
            retVal = paramRtx.getValueOfCurrentNode();
            break;
        default:
            // Do nothing.
        }
        return retVal;
    }

    /**
     * This functional class is used to compare leaf nodes. The comparison is
     * done by comparing the (characteristic) string for two nodes. If the
     * strings are sufficient similar, the nodes are considered to be equal.
     */
    private class LeafEqual implements IComparator<INode> {

        /** {@link IWriteTransaction} implementation on old revision. */
        private final IWriteTransaction mWtx;

        /** {@link IReadTransaction} implementation on new revision. */
        private final IReadTransaction mRtx;

        /**
         * Constructor.
         * 
         * @param paramWtx
         *            {@link IWriteTransaction} implementation on old revision
         * @param paramRtx
         *            {@link IReadTransaction} implementation on new revision
         */
        LeafEqual(final IWriteTransaction paramWtx, final IReadTransaction paramRtx) {
            assert paramWtx != null;
            assert paramRtx != null;
            mWtx = paramWtx;
            mRtx = paramRtx;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEqual(final INode paramFirstNode, final INode paramSecondNode) {
            final double ratio =
                Util.quickRatio(getNodeValue(paramFirstNode, mWtx), getNodeValue(paramSecondNode, mRtx));
            return ratio > FMESF;
        }
    }

    /**
     * This functional class is used to compare inner nodes. FMES uses different
     * comparison criteria for leaf nodes and inner nodes. This class compares
     * two nodes by calculating the number of common children (i.e. children
     * contained in the matching) in relation to the total number of children.
     */
    private class InnerNodeEqual implements IComparator<INode> {

        /** {@link Matching} reference. */
        private final Matching mMatching;

        /** {@link IWriteTransaction} implementation reference on old revision. */
        private final IWriteTransaction mWtx;

        /** {@link IReadTransaction} implementation reference on new revision. */
        private final IReadTransaction mRtx;

        /**
         * Constructor.
         * 
         * @param paramMatching
         *            {@link Matching} reference
         * @param paramWtx
         *            {@link IWriteTransaction} implementation reference on old
         *            revision
         * @param paramRtx
         *            {@link IReadTransaction} implementation reference on new
         *            revision
         */
        public InnerNodeEqual(final Matching paramMatching, final IWriteTransaction paramWtx,
            final IReadTransaction paramRtx) {
            assert paramMatching != null;
            assert paramWtx != null;
            assert paramRtx != null;
            mMatching = paramMatching;
            mRtx = paramRtx;
            mWtx = paramWtx;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEqual(final INode paramFirstNode, final INode paramSecondNode) {
            assert paramFirstNode != null;
            assert paramSecondNode != null;
            long common = 0;

            if (paramFirstNode.getKind() == ENode.ATTRIBUTE_KIND
                && nodeValuesEqual(paramFirstNode, paramSecondNode, mWtx, mRtx)) {
                // This allows us to detect the update of values for attributes
                // because when a value is changed, 100% of the children have
                // changed
                // and the attribute node would not be considered to be same.
                common = 1;
            } else {
                common = mMatching.containedChildren(paramFirstNode, paramSecondNode);
            }

            final long maxFamilySize =
                Math.max(mDescendants.get(paramFirstNode), mDescendants.get(paramSecondNode));
            return (2.5 * common / maxFamilySize) >= FMESTHRESHOLD;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return NAME;
    }

}
