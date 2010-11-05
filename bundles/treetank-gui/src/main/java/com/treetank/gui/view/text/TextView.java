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

import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.LoggerFactory;

import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.gui.GUI;
import com.treetank.gui.GUIProp;
import com.treetank.gui.IView;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.serialize.StAXSerializer;
import com.treetank.utils.LogWrapper;

/**
 * <h1>TextView</h1>
 * 
 * <p>Basic text view.</p>
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
    public void dispose() {
        final JScrollBar bar = this.getVerticalScrollBar();
        for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
            bar.removeAdjustmentListener(listener);
        }
    }  

    @Override
    public void refreshInit() { }

    @Override
    public void refreshUpdate() {
        // Serialize file into XML view if it is empty.
        final StringBuilder out = new StringBuilder();

        // Get references.
        final ReadDB db = mGUI.getReadDB();
        final IReadTransaction rtx = db.getRtx();
        final IItem node = rtx.getNode();
        StAXSerializer serializer = null;

        try {
            final long nodeKey = node.getNodeKey();

            switch (node.getKind()) {
            case ROOT_KIND:
            case ELEMENT_KIND:
                serializer = new StAXSerializer(new DescendantAxis(rtx, true), false);
                break;
            case TEXT_KIND:
                rtx.moveTo(nodeKey);
                out.append(new String(rtx.getNode().getRawValue()));
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
                    out.append("xmlns='").append(rtx.nameForKey(rtx.getNode().getURIKey())).append("'");
                } else {
                    out.append("xmlns:").append(rtx.nameForKey(rtx.getNode().getNameKey())).append("='")
                        .append(rtx.nameForKey(rtx.getNode().getURIKey())).append("'");
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

                if (attPrefix == null || attPrefix.isEmpty()) {
                    out.append(attQName.getLocalPart()).append("='").append(rtx.getValueOfCurrentNode())
                        .append("'");
                } else {
                    out.append(attPrefix).append(":").append(attQName.getLocalPart()).append("='").append(
                        rtx.getValueOfCurrentNode()).append("'");
                }
                break;
            default:
                throw new IllegalStateException("Node kind not known!");
            }
        } catch (final IllegalStateException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        try {
            if (out.toString().isEmpty()) {
                processStAX(out, serializer);
            }

            mTextArea.setText(out.toString());
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

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
     * Process StAX output.
     * 
     * @param paramOut
     *            {@link StringBuilder} to hold the serialized representation.
     * @param paramSerializer
     *            The {@link StAXSerializer}.
     * @return the StringBuilder instance (the serialized representation).
     * @throws XMLStreamException
     *             if any parsing exception occurs
     */
    private StringBuilder processStAX(final StringBuilder paramOut, final StAXSerializer paramSerializer)
        throws XMLStreamException {
        final StAXSerializer serializer = paramSerializer;
        final StringBuilder out = paramOut;

        final StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < GUIProp.INDENT_SPACES; i++) {
            spaces.append(" ");
        }
        final String indentSpaces = spaces.toString();
        
        final int lineHeight = mTextArea.getFontMetrics(this.getFont()).getHeight();
        final int frameHeight = mTextArea.getHeight();
        
        int level = -1;
        long height = 0;
        assert serializer != null;
        while (serializer.hasNext() && height < frameHeight) {
            final XMLEvent event = serializer.nextEvent();
            switch (event.getEventType()) {
            case XMLStreamConstants.START_DOCUMENT:
                break;
            case XMLStreamConstants.START_ELEMENT:
                final StartElement startTag = event.asStartElement();
                final String qName = qNameToString(startTag.getName());
                level++;
                indent(out, level, indentSpaces);
                out.append("<").append(qName).append(">");
                out.append(GUIProp.NEWLINE);
                height += lineHeight;
                break;
            case XMLStreamConstants.END_ELEMENT:
                final EndElement endTag = event.asEndElement();
                indent(out, level, indentSpaces);
                out.append("</").append(endTag.getName()).append(">");
                out.append(GUIProp.NEWLINE);
                level--;
                height += lineHeight;
                break;
            case XMLStreamConstants.CHARACTERS:
                level++;
                indent(out, level, indentSpaces);
                level--;
                out.append(event.asCharacters().getData());
                out.append(GUIProp.NEWLINE);
                height += lineHeight;
                break;
            default:
                // Empty.
            }
        }
        
        return out;
    }

    /**
     * Serialization compatible String representation of a {@link QName} reference.
     * 
     * @param paramQName
     *            The {@QName} reference.
     * @return the string representation
     */
    private String qNameToString(final QName paramQName) {
        String retVal;

        if (paramQName.getPrefix().isEmpty()) {
            retVal = paramQName.getLocalPart();
        } else {
            retVal = paramQName.getPrefix() + ":" + paramQName.getLocalPart();
        }

        return retVal;
    }
    
    /**
     * Indent serialized output.
     * 
     * @param paramOut
     *                  {@link StringBuilder} to hold spaces.
     * @param paramLevel
     *                  Current level in the tree.
     * @param paramIndentSpaces
     *                  Determines how many spaces to indent at every level.
     */
    private void indent(final StringBuilder paramOut, final int paramLevel, final String paramIndentSpaces) {
        for (int i = 0; i < paramLevel; i++) {
            paramOut.append(paramIndentSpaces);
        }
    }
}
