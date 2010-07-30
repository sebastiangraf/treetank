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
package com.treetank.gui.view.tree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.namespace.QName;

import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.ElementNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1TreetankTreeCellRenderer</h1>
 * 
 * <p>
 * Customized tree cell renderer to render nodes nicely.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TreetankTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Generated UID.
     */
    private static final long serialVersionUID =
        -6242168246410260644L;

    /** Logger. */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(TreetankTreeCellRenderer.class);

    /** Element color. */
    private final Color mElementColor = new Color(0, 0, 128);

    /** Attribute color. */
    private final Color mAttributeColor = new Color(0, 128, 0);

    /** Treetant reading transaction. */
    private transient IReadTransaction mRTX;

    /** Treetank databse. */
    protected transient IDatabase mDatabase;

    /** Path to file. */
    private static String mPATH;

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            Treetank database {@link IDatabase}.
     * 
     */
    public TreetankTreeCellRenderer(final IDatabase paramDatabase) {
        this(paramDatabase, 0);
    }

    /**
     * Constructor.
     * 
     * @param database
     *            Treetank database {@link IDatabase}.
     * @param nodekeyToStart
     *            Starting point of transaction (node key).
     * 
     */
    public TreetankTreeCellRenderer(final IDatabase database, final long nodekeyToStart) {
        setOpenIcon(null);
        setClosedIcon(null);
        setLeafIcon(null);
        setBackgroundNonSelectionColor(null);
        setTextSelectionColor(Color.red);

        try {
            if (mDatabase == null || mDatabase.getFile() == null
                || !(mDatabase.getFile().equals(database.getFile()))) {
                mDatabase =
                    database;

                if (mRTX != null && !mRTX.isClosed()) {
                    mRTX.close();
                }
            }

            if (mRTX == null || mRTX.isClosed()) {
                mRTX =
                    mDatabase.getSession().beginReadTransaction();
            }
            mRTX.moveTo(nodekeyToStart);
        } catch (final TreetankException e) {
            LOGGER.error("TreetankException: " + e.getMessage(), e);
        }

        mPATH =
            database.getFile().getAbsolutePath();
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, Object value, final boolean sel,
        final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        final IItem node =
            (IItem)value;

        final long key =
            node.getNodeKey();

        switch (node.getKind()) {
        case ELEMENT_KIND:
            mRTX.moveTo(node.getNodeKey());
            final String prefix =
                mRTX.getQNameOfCurrentNode().getPrefix();
            final QName qName =
                mRTX.getQNameOfCurrentNode();

            if (prefix == null || prefix.equals("")) {
                final String localPart =
                    qName.getLocalPart();

                if (((ElementNode)mRTX.getNode()).hasFirstChild()) {
                    value =
                        '<' + localPart + '>';
                } else {
                    value =
                        '<' + localPart + "/>";
                }
            } else {
                value =
                    '<' + prefix + ":" + qName.getLocalPart() + '>';
            }

            break;
        case ATTRIBUTE_KIND:
            // Move transaction to parent of the attribute node.
            mRTX.moveTo(node.getParentKey());
            final long aNodeKey =
                node.getNodeKey();
            for (int i =
                0, attsCount =
                ((ElementNode)mRTX.getNode()).getAttributeCount(); i < attsCount; i++) {
                mRTX.moveToAttribute(i);
                if (mRTX.getNode().equals(node)) {
                    break;
                }
                mRTX.moveTo(aNodeKey);
            }

            // Display value.
            final String attPrefix =
                mRTX.getQNameOfCurrentNode().getPrefix();
            final QName attQName =
                mRTX.getQNameOfCurrentNode();

            if (attPrefix == null || attPrefix.equals("")) {
                value =
                    '@' + attQName.getLocalPart() + "='" + mRTX.getValueOfCurrentNode() + "'";
            } else {
                value =
                    '@' + attPrefix + ":" + attQName.getLocalPart() + "='" + mRTX.getValueOfCurrentNode()
                        + "'";
            }

            break;
        case NAMESPACE_KIND:
            // Move transaction to parent the namespace node.
            mRTX.moveTo(node.getParentKey());
            final long nNodeKey =
                node.getNodeKey();
            for (int i =
                0, namespCount =
                ((ElementNode)mRTX.getNode()).getNamespaceCount(); i < namespCount; i++) {
                mRTX.moveToNamespace(i);
                if (mRTX.getNode().equals(node)) {
                    break;
                }
                mRTX.moveTo(nNodeKey);
            }

            if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                value =
                    "xmlns='" + mRTX.nameForKey(mRTX.getNode().getURIKey()) + "'";
            } else {
                value =
                    "xmlns:" + mRTX.nameForKey(mRTX.getNode().getNameKey()) + "='"
                        + mRTX.nameForKey(mRTX.getNode().getURIKey()) + "'";
            }
            break;
        case TEXT_KIND:
            mRTX.moveTo(node.getNodeKey());
            value =
                mRTX.getValueOfCurrentNode();
            break;
        case COMMENT_KIND:
            mRTX.moveTo(node.getNodeKey());
            value =
                "<!-- " + mRTX.getValueOfCurrentNode() + " -->";
            break;
        case PROCESSING_KIND:
            mRTX.moveTo(node.getNodeKey());
            value =
                "<? " + mRTX.getValueOfCurrentNode() + " ?>";
            break;
        case ROOT_KIND:
            value =
                mPATH;
            break;
        case WHITESPACE_KIND:
            break;
        default:
            new IllegalStateException("Node kind not known!");
        }

        value =
            value + " [" + key + "]";

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (!selected) {
            switch (node.getKind()) {
            case ELEMENT_KIND:
                setForeground(mElementColor);
                break;
            case ATTRIBUTE_KIND:
                setForeground(mAttributeColor);
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
