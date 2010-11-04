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
package com.treetank.gui.view.text;

import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.gui.GUI;
import com.treetank.gui.GUIProp;
import com.treetank.gui.IView;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.serialize.XMLSerializerProperties;
import com.treetank.settings.ECharsForSerializing;
import com.treetank.utils.LogWrapper;

import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;

/**
 * <h1>TextView</h1>
 * 
 * <p>
 * Basic text view.</h1>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TextView extends JScrollPane implements IView {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(TextView.class));

    // ======== Component bounds ==========

    /** Width of text component. */
    private static final int WIDTH = 370;

    /** Height of text component. */
    private static final int HEIGHT = 600;

    /** Columns in text component. */
    private static final int COLUMNS = 80;

    /** Scrollpane width. */
    private static final int PANE_WIDTH = 400;

    /** Scrollpane height. */
    private static final int PANE_HEIGHT = 600;

    // ======= Global member variables =====

    /** {@link JTextArea}, which displays XML data. */
    private final JTextArea mTextArea;

    /** {@link ViewNotifier} which notifies views of changes. */
    private final ViewNotifier mNotifier;

    /** Main {@link GUI} window. */
    private final GUI mGUI;

    /** AdjustmentListener temporal value. */
    private int mTempValue;

    /** Text output stream. */
    private transient OutputStream mOut;

    /** Adjustment Listener for textArea. */
    private transient AdjustmentListener mAdjListener;

    /** Start position of char array for text insertion. */
    private transient int mStartPos;

    /** Line number to append or remove from the text field. */
    private transient int mLineChanges;

    /**
     * Constructor.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     */
    public TextView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;
        mGUI = paramNotifier.getGUI();

        mNotifier.add(this);

        // Create a XML text area.
        mTextArea = new JTextArea();
        mTextArea.setEditable(false);
        mTextArea.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        mTextArea.setColumns(COLUMNS);
        mTextArea.setLineWrap(true);
        mTextArea.setCaretPosition(0);

        // Create a scroll pane and add the XML text area to it.
        setViewportView(mTextArea);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setMinimumSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
    }

    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWTEXT.getValue();
    }

    @Override
    public void refreshInit() {
        final JScrollBar bar = this.getVerticalScrollBar();
        for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
            bar.removeAdjustmentListener(listener);
        }
    }

    @Override
    public void refreshUpdate() {
        // final JScrollBar bar = this.getVerticalScrollBar();
        // for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
        // bar.removeAdjustmentListener(listener);
        // }

        // Serialize file into XML view if it is empty.
        mOut = new ByteArrayOutputStream();
        final XMLSerializerProperties properties = new XMLSerializerProperties();

        // Get references.
        final ReadDB db = mGUI.getReadDB();
        final ISession session = db.getSession();
        final IReadTransaction rtx = db.getRtx();
        final IItem node = rtx.getNode();

        try {
            final long nodeKey = node.getNodeKey();

            System.out.println(nodeKey);

            switch (node.getKind()) {
            case ROOT_KIND:
                new XMLSerializerBuilder(session, nodeKey, mOut, properties).build().call();
                break;
            case ELEMENT_KIND:
                new XMLSerializerBuilder(session, nodeKey, mOut, properties).setDeclaration(false).build()
                    .call();
                break;
            case TEXT_KIND:
                rtx.moveTo(nodeKey);
                mOut.write(rtx.getNode().getRawValue());
                break;
            case NAMESPACE_KIND:
                // Move transaction to parent of given namespace node.
                rtx.moveTo(node.getParentKey());

                final long nNodeKey = node.getNodeKey();
                for (int i = 0, namespCount = ((ElementNode)rtx.getNode()).getNamespaceCount(); i < namespCount; i++) {
                    rtx.moveToNamespace(i);
                    if (rtx.getNode().equals(node)) {
                        break;
                    }
                    rtx.moveTo(nNodeKey);
                }

                if (rtx.nameForKey(rtx.getNode().getNameKey()).length() == 0) {
                    mOut.write(("xmlns='" + rtx.nameForKey(rtx.getNode().getURIKey()) + "'").getBytes());
                } else {
                    mOut.write(("xmlns:" + rtx.nameForKey(rtx.getNode().getNameKey()) + "='"
                        + rtx.nameForKey(rtx.getNode().getURIKey()) + "'").getBytes());
                }
                break;
            case ATTRIBUTE_KIND:
                // Move transaction to parent of given attribute node.
                rtx.moveTo(node.getParentKey());
                final long aNodeKey = node.getNodeKey();
                for (int i = 0, attsCount = ((ElementNode)rtx.getNode()).getAttributeCount(); i < attsCount; i++) {
                    rtx.moveToAttribute(i);
                    if (rtx.getNode().equals(node)) {
                        break;
                    }
                    rtx.moveTo(aNodeKey);
                }

                // Display value.
                final String attPrefix = rtx.getQNameOfCurrentNode().getPrefix();
                final QName attQName = rtx.getQNameOfCurrentNode();

                if (attPrefix == null || attPrefix.equals("")) {
                    mOut.write((attQName.getLocalPart() + "='" + rtx.getValueOfCurrentNode() + "'")
                        .getBytes());
                } else {
                    mOut.write((attPrefix + ":" + attQName.getLocalPart() + "='"
                        + rtx.getValueOfCurrentNode() + "'").getBytes());
                }
                break;
            default:
                throw new IllegalStateException("Node kind not known!");
            }
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final IllegalStateException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        // System.out.println(mOut.toString());

        mTextArea.setText(mOut.toString());
        // text(false);

        final JScrollBar vertScrollBar = getVerticalScrollBar();
        vertScrollBar.setValue(vertScrollBar.getMinimum());

        if (vertScrollBar.getAdjustmentListeners().length == 0) {
            vertScrollBar.addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(final AdjustmentEvent paramEvt) {
                    // /*
                    // * getValueIsAdjusting() returns true if the user is currently dragging
                    // * the scrollbar's knob and has not picked a final value.
                    // */
                    // if (paramEvt.getValueIsAdjusting()) {
                    // // The user is dragging the knob.
                    // return;
                    // }
                    //
                    // final int lineHeight = mTextArea.getFontMetrics(mTextArea.getFont()).getHeight();
                    // final int value = paramEvt.getValue();
                    // System.out.println("VALUE: " + value);
                    // final int result = value - mTempValue;
                    // mLineChanges = result / lineHeight;
                    // System.out.println("Lines: " + mLineChanges);
                    // if (mLineChanges != 0) {
                    // text(false);
                    // }
                    //
                    // mTempValue = value;
                    // mNotifier.update();
                }
            });
        }

        repaint();
    }

    /**
     * Display text.
     * 
     * @param paramInit
     *            Determines if it's the initial invocation.
     */
    private void text(final boolean paramInit) {
        // Remove adjustmnet listeners temporarily.
        final JScrollBar bar = this.getVerticalScrollBar();
        for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
            mAdjListener = listener;
            bar.removeAdjustmentListener(listener);
        }

        // Initialize variables.
        final char[] text = mOut.toString().toCharArray();
        final int lineHeight = mTextArea.getFontMetrics(this.getFont()).getHeight();
        final int frameHeight = mTextArea.getHeight() + mLineChanges * lineHeight;
        int rowsSize = 0;
        final StringBuilder sBuilder = new StringBuilder();
        int indexSepChar = 0;
        final String NL = ECharsForSerializing.NEWLINE.toString();
        // int countNewlines = 0;
        // final StringBuilder insertAtFirstPos = new StringBuilder();
        //
        // if (changeColumns > 0) {
        // // Get start index.
        // for (int i = 0; i < text.length; i++) {
        // final char character = text[i];
        //
        // // Increment rowSize?
        // if (indexSepChar < NL.length() && character ==
        // NL.charAt(indexSepChar)) {
        // if (indexSepChar == NL.length() - 1) {
        // countNewlines++;
        // }
        // }
        //
        // insertAtFirstPos.append(character);
        //
        // if (countNewlines == changeColumns) {
        // startPos = i + 1;
        // break;
        // }
        // }
        //
        // xmlPane.replaceRange("", 0, startPos - 1);
        // } else if (changeColumns < 0) {
        // xmlPane.insert(insertAtFirstPos.toString(), 0);
        // }

        // Build text.
        rowsSize = 0;
        if (paramInit) {
            mStartPos = 0;
        }
        for (int i = mStartPos == 0 ? mStartPos : mStartPos + 1; i < text.length && mLineChanges >= 0
            && mStartPos + 1 != text.length; i++) {
            final char character = text[i];

            // Increment rowsSize?
            if (indexSepChar < NL.length() && character == NL.charAt(indexSepChar)) {
                if (indexSepChar == NL.length() - 1) {
                    rowsSize += lineHeight;
                } else {
                    indexSepChar++;
                }
            }

            if (rowsSize < frameHeight) {
                sBuilder.append(character);
                mStartPos = i;
            } else {
                mStartPos = i;
                System.out.println("START: " + mStartPos);
                break;
            }
        }

        if (mLineChanges >= 0 && mStartPos + 1 <= text.length) {
            if (paramInit) {
                mTextArea.setText(sBuilder.toString());
                mTextArea.setCaretPosition(0);
            } else {
                final int caretPos = mTextArea.getCaretPosition();
                mTextArea.setCaretPosition(mTextArea.getDocument().getLength());
                mTextArea.append(sBuilder.toString());
                // Check and update caret position.
                final int newCaretPos = caretPos + mLineChanges * mTextArea.getColumns();
                final int documentLength = mTextArea.getDocument().getLength();
                if (newCaretPos < documentLength) {
                    mTextArea.setCaretPosition(newCaretPos);
                }
            }
        }

        /*
         * Schedule a job for the event dispatch thread: (Re)adding the
         * adjustment listener.
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                bar.addAdjustmentListener(mAdjListener);
            }
        });
    }
}
