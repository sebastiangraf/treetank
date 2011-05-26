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

package org.treetank.gui.view.tree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.namespace.QName;

import org.treetank.access.FileDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.node.ElementNode;

import static org.treetank.gui.GUIConstants.ATTRIBUTE_COLOR;
import static org.treetank.gui.GUIConstants.DOC_COLOR;
import static org.treetank.gui.GUIConstants.ELEMENT_COLOR;

/**
 * <h1>TreeCellRenderer</h1>
 * 
 * <p>
 * Customized tree cell renderer to render nodes nicely.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Generated UID.
     */
    private static final long serialVersionUID = -6242168246410260644L;


    /** Treetant reading transaction {@link IReadTransaction}. */
    private transient IReadTransaction mRTX;

    /** Path to file. */
    private final String mPATH;

    /**
     * Constructor.
     * 
     * @param paramReadDB
     *            {@link ReadDB}.
     */
    TreeCellRenderer(final ReadDB paramReadDB) {
        setOpenIcon(null);
        setClosedIcon(null);
        setLeafIcon(null);
        setBackgroundNonSelectionColor(null);
        setTextSelectionColor(Color.red);

        try {
            mRTX = paramReadDB.getSession().beginReadTransaction(paramReadDB.getRevisionNumber());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
        }

        mRTX.moveTo(paramReadDB.getNodeKey());
        mPATH = ((FileDatabase)paramReadDB.getDatabase()).mFile.getName();
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree paramTree, Object paramValue,
        final boolean paramSel, final boolean paramExpanded, final boolean paramLeaf, final int paramRow,
        final boolean paramHasFocus) throws IllegalStateException {
        final IItem node = (IItem)paramValue;

        final long key = node.getNodeKey();

        switch (node.getKind()) {
        case ELEMENT_KIND:
            mRTX.moveTo(node.getNodeKey());
            final String prefix = mRTX.getQNameOfCurrentNode().getPrefix();
            final QName qName = mRTX.getQNameOfCurrentNode();

            if (prefix == null || prefix.equals("")) {
                final String localPart = qName.getLocalPart();

                if (((ElementNode)mRTX.getNode()).hasFirstChild()) {
                    paramValue = '<' + localPart + '>';
                } else {
                    paramValue = '<' + localPart + "/>";
                }
            } else {
                paramValue = '<' + prefix + ":" + qName.getLocalPart() + '>';
            }

            break;
        case ATTRIBUTE_KIND:
            // Move transaction to parent of the attribute node.
            mRTX.moveTo(node.getParentKey());
            final long aNodeKey = node.getNodeKey();
            for (int i = 0, attsCount = ((ElementNode)mRTX.getNode()).getAttributeCount(); i < attsCount; i++) {
                mRTX.moveToAttribute(i);
                if (mRTX.getNode().equals(node)) {
                    break;
                }
                mRTX.moveTo(aNodeKey);
            }

            // Display value.
            final String attPrefix = mRTX.getQNameOfCurrentNode().getPrefix();
            final QName attQName = mRTX.getQNameOfCurrentNode();

            if (attPrefix == null || attPrefix.equals("")) {
                paramValue = '@' + attQName.getLocalPart() + "='" + mRTX.getValueOfCurrentNode() + "'";
            } else {
                paramValue =
                    '@' + attPrefix + ":" + attQName.getLocalPart() + "='" + mRTX.getValueOfCurrentNode()
                        + "'";
            }

            break;
        case NAMESPACE_KIND:
            // Move transaction to parent the namespace node.
            mRTX.moveTo(node.getParentKey());
            final long nNodeKey = node.getNodeKey();
            for (int i = 0, namespCount = ((ElementNode)mRTX.getNode()).getNamespaceCount(); i < namespCount; i++) {
                mRTX.moveToNamespace(i);
                if (mRTX.getNode().equals(node)) {
                    break;
                }
                mRTX.moveTo(nNodeKey);
            }

            if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                paramValue = "xmlns='" + mRTX.nameForKey(mRTX.getNode().getURIKey()) + "'";
            } else {
                paramValue =
                    "xmlns:" + mRTX.nameForKey(mRTX.getNode().getNameKey()) + "='"
                        + mRTX.nameForKey(mRTX.getNode().getURIKey()) + "'";
            }
            break;
        case TEXT_KIND:
            mRTX.moveTo(node.getNodeKey());
            paramValue = mRTX.getValueOfCurrentNode();
            break;
        case COMMENT_KIND:
            mRTX.moveTo(node.getNodeKey());
            paramValue = "<!-- " + mRTX.getValueOfCurrentNode() + " -->";
            break;
        case PROCESSING_KIND:
            mRTX.moveTo(node.getNodeKey());
            paramValue = "<? " + mRTX.getValueOfCurrentNode() + " ?>";
            break;
        case ROOT_KIND:
            paramValue = "Doc: " + mPATH;
            break;
        case WHITESPACE_KIND:
            break;
        default:
            throw new IllegalStateException("Node kind not known!");
        }

        paramValue = paramValue + " [" + key + "]";

        super.getTreeCellRendererComponent(paramTree, paramValue, paramSel, paramExpanded, paramLeaf,
            paramRow, paramHasFocus);
        if (!selected) {
            switch (node.getKind()) {
            case ROOT_KIND:
                setForeground(DOC_COLOR);
                break;
            case ELEMENT_KIND:
                setForeground(ELEMENT_COLOR);
                break;
            case ATTRIBUTE_KIND:
                setForeground(ATTRIBUTE_COLOR);
                break;
            default:
                // Do nothing.
            }
        }

        return this;
    }

    @Override
    public Color getBackground() {
        return null;
    }
}
