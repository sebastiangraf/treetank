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

import static com.treetank.gui.GUIConstants.ATTRIBUTE_COLOR;
import static com.treetank.gui.GUIConstants.ELEMENT_COLOR;
import static com.treetank.gui.GUIConstants.NAMESPACE_COLOR;
import static com.treetank.gui.GUIConstants.NEWLINE;
import static com.treetank.gui.GUIConstants.TEXT_COLOR;

import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Iterator;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.LoggerFactory;

import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.gui.GUI;
import com.treetank.gui.GUIProp;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.IView;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.serialize.StAXSerializer;
import com.treetank.utils.LogWrapper;

/**
 * <h1>TextView</h1>
 * 
 * <p>
 * Basic text view.
 * </p>
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

    /** Scrollpane width. */
    private static final int PANE_WIDTH = 400;

    /** Scrollpane height. */
    private static final int PANE_HEIGHT = 600;

    // ======= Global member variables =====
    
    /** {@link TextView} instance. */
    private static TextView mView;

    /** {@link JTextPane}, which displays XML data. */
    private final JTextPane mText = new JTextPane();

    /** {@link ViewNotifier} which notifies views of changes. */
    private final ViewNotifier mNotifier;

    /** Main {@link GUI} window. */
    private final GUI mGUI;

    /** {@link StAXSerializer}. */
    private transient StAXSerializer mSerializer;

    /** Temporal adjustment value. */
    private transient int mTempValue;

    /** Lines changed during adjustment. */
    private transient int mLineChanges;

    /** State to serialize data. */
    private enum State {
        /** Initial state. */
        INITIAL,

        /** Update state. */
        UPDATE,
    };

    /** Determines if a node has children or not. */
    private enum Child {
        /** Node has no children. */
        NOCHILD,

        /** Node has children. */
        CHILD,
    }

    /** Temporary level after initial filling of the text area. */
    private transient int mTempLevel;

    /**
     * Private constructor, called from singleton factory method.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     */
    private TextView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;
        mNotifier.add(this);
        mGUI = paramNotifier.getGUI();

        // Setup text field.
        mText.setEditable(false);
        mText.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        // mText.setColumns(COLUMNS);
        // mText.setLineWrap(true);
        mText.setCaretPosition(0);

        // Create a scroll pane and add the XML text area to it.
        setViewportView(mText);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setMinimumSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
    }
    
    /**
     * Singleton factory method.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     * @return {@link TextView} instance.
     */
    public static TextView createInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new TextView(paramNotifier);
        }
        
        return mView;
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
    public void refreshInit() {
        final JScrollBar vertScrollBar = getVerticalScrollBar();
        vertScrollBar.setValue(vertScrollBar.getMinimum());

        if (vertScrollBar.getAdjustmentListeners().length == 0) {
            vertScrollBar.addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(final AdjustmentEvent paramEvt) {
                    /*
                     * getValueIsAdjusting() returns true if the user is currently dragging
                     * the scrollbar's knob and has not picked a final value.
                     */
                    if (paramEvt.getValueIsAdjusting()) {
                        // The user is dragging the knob.
                        return;
                    }

                    final int lineHeight = mText.getFontMetrics(mText.getFont()).getHeight();
                    final int value = paramEvt.getValue();
                    final int result = value - mTempValue;
                    mLineChanges = result / lineHeight;

                    if (mLineChanges > 0) {
                        try {
                            processStAX(State.UPDATE);
                            mText.setCaretPosition(0);
                        } catch (final XMLStreamException e) {
                            LOGWRAPPER.error(e.getMessage(), e);
                        } catch (final BadLocationException e) {
                            LOGWRAPPER.error(e.getMessage(), e);
                        }
                    }

                    mTempValue = value;
                }
            });
        }
    }

    @Override
    public void refreshUpdate() {
        // Get references.
        final ReadDB db = mGUI.getReadDB();
        final IReadTransaction rtx = db.getRtx();
        final IItem node = rtx.getNode();

        serialize(rtx, node);
        mText.setCaretPosition(0);

        repaint();
    }

    /**
     * Serialize a tree.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}.
     * @param paramNode
     *            Treetank {@link IItem}.
     */
    private void serialize(final IReadTransaction paramRtx, final IItem paramNode) {
        assert paramRtx != null && paramNode != null;
        final IReadTransaction rtx = paramRtx;
        final IItem node = paramNode;

        // Style document.
        final StyledDocument doc = (StyledDocument)mText.getDocument();
        final Style styleElements = doc.addStyle("elements", null);
        StyleConstants.setForeground(styleElements, ELEMENT_COLOR);
        final Style styleNamespaces = doc.addStyle("attributes", null);
        StyleConstants.setForeground(styleNamespaces, NAMESPACE_COLOR);
        final Style styleAttributes = doc.addStyle("attributes", null);
        StyleConstants.setForeground(styleAttributes, ATTRIBUTE_COLOR);
        final Style styleText = doc.addStyle("text", null);
        StyleConstants.setForeground(styleText, TEXT_COLOR);

        boolean insert = false;
        final long nodeKey = node.getNodeKey();

        try {
            switch (node.getKind()) {
            case ROOT_KIND:
            case ELEMENT_KIND:
                mText.setText("");
                mSerializer = new StAXSerializer(new DescendantAxis(rtx, true), false);
                insert = true;
                break;
            case TEXT_KIND:
                rtx.moveTo(nodeKey);
                mText.setText("");
                doc.insertString(doc.getLength(), new String(rtx.getNode().getRawValue()), styleText);
                break;
            case NAMESPACE_KIND:
                // Move transaction to parent of given namespace node.
                rtx.moveTo(node.getParentKey());
                mText.setText("");

                final long nNodeKey = node.getNodeKey();
                for (int i = 0, namespCount = ((ElementNode)rtx.getNode()).getNamespaceCount(); i < namespCount; i++) {
                    rtx.moveToNamespace(i);
                    if (rtx.getNode().equals(node)) {
                        break;
                    }
                    rtx.moveTo(nNodeKey);
                }

                if (rtx.nameForKey(rtx.getNode().getNameKey()).length() == 0) {
                    doc.insertString(doc.getLength(), new StringBuilder().append("xmlns='").append(
                        rtx.nameForKey(rtx.getNode().getURIKey())).append("'").toString(), styleNamespaces);
                } else {
                    doc.insertString(doc.getLength(), new StringBuilder().append("xmlns:").append(
                        rtx.nameForKey(rtx.getNode().getNameKey())).append("='").append(
                        rtx.nameForKey(rtx.getNode().getURIKey())).append("'").toString(), styleNamespaces);
                }
                break;
            case ATTRIBUTE_KIND:
                // Move transaction to parent of given attribute node.
                rtx.moveTo(node.getParentKey());
                mText.setText("");

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
                    doc.insertString(doc.getLength(), new StringBuilder().append(attQName.getLocalPart())
                        .append("='").append(rtx.getValueOfCurrentNode()).append("'").toString(),
                        styleAttributes);
                } else {
                    doc.insertString(doc.getLength(), new StringBuilder().append(attPrefix).append(":")
                        .append(attQName.getLocalPart()).append("='").append(rtx.getValueOfCurrentNode())
                        .append("'").toString(), styleAttributes);
                }
                break;
            default:
                throw new IllegalStateException("Node kind not known!");
            }

            if (insert) {
                processStAX(State.INITIAL);
            }
        } catch (final BadLocationException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /**
     * Process StAX output.
     * 
     * @param paramState
     *            {@link State} enum, which determines if an initial or update of the view occurs.
     * @throws XMLStreamException
     *             if any parsing exception occurs
     * @throws BadLocationException
     *             if inserting strings into the {@link JTextPane} failes
     */
    private void processStAX(final State paramState) throws XMLStreamException, BadLocationException {
        assert paramState != null;

        final GUIProp prop = new GUIProp();
        final int indent = prop.getIndentSpaces();
        final StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            spaces.append(" ");
        }
        final String indentSpaces = spaces.toString();

        // Style document.
        final StyledDocument doc = (StyledDocument)mText.getDocument();
        final Style styleElements = doc.addStyle("elements", null);
        StyleConstants.setForeground(styleElements, ELEMENT_COLOR);
        final Style styleText = doc.addStyle("text", null);
        StyleConstants.setForeground(styleText, TEXT_COLOR);

        boolean emptyElement = false;
        assert mSerializer != null;
        switch (paramState) {
        case INITIAL:
            // Initialize variables.
            final int lineHeight = mText.getFontMetrics(this.getFont()).getHeight();
            final int frameHeight = mText.getHeight();
            int level = -1;
            long height = 0;
            while (mSerializer.hasNext() && height < frameHeight) {
                final XMLEvent event = mSerializer.nextEvent();
                switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    final StartElement startTag = event.asStartElement();
                    level++;
                    indent(doc, level, indentSpaces);
                    if (mSerializer.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
                        processStartTag(startTag, doc, Child.NOCHILD);
                        emptyElement = true;
                    } else {
                        processStartTag(startTag, doc, Child.CHILD);
                    }
                    height += lineHeight;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (emptyElement) {
                        emptyElement = false;
                    } else {
                        final EndElement endTag = event.asEndElement();
                        indent(doc, level, indentSpaces);
                        doc.insertString(doc.getLength(), new StringBuilder().append("</").append(
                            qNameToString(endTag.getName())).append(">").append(NEWLINE).toString(),
                            styleElements);
                    }
                    level--;
                    break;
                case XMLStreamConstants.CHARACTERS:
                    level++;
                    indent(doc, level, indentSpaces);
                    level--;
                    doc.insertString(doc.getLength(), event.asCharacters().getData() + NEWLINE, styleText);
                    height += lineHeight;
                    break;
                default:
                    // Empty.
                }
            }
            mTempLevel = level;
            break;
        case UPDATE:
            for (int i = 0; mSerializer.hasNext() && i <= mLineChanges; i++) {
                final XMLEvent event = mSerializer.nextEvent();
                switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    final StartElement startTag = event.asStartElement();
                    mTempLevel++;
                    indent(doc, mTempLevel, indentSpaces);
                    if (mSerializer.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
                        processStartTag(startTag, doc, Child.NOCHILD);
                        emptyElement = true;
                    } else {
                        processStartTag(startTag, doc, Child.CHILD);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (emptyElement) {
                        emptyElement = false;
                    } else {
                        final EndElement endTag = event.asEndElement();
                        indent(doc, mTempLevel, indentSpaces);
                        doc.insertString(doc.getLength(), new StringBuilder().append("</").append(
                            qNameToString(endTag.getName())).append(">").append(NEWLINE).toString(),
                            styleElements);
                    }
                    mTempLevel--;
                    break;
                case XMLStreamConstants.CHARACTERS:
                    mTempLevel++;
                    indent(doc, mTempLevel, indentSpaces);
                    mTempLevel--;
                    doc.insertString(doc.getLength(), event.asCharacters().getData() + NEWLINE, styleText);
                    break;
                default:
                    // Empty.
                }
            }
            break;
        default:
            // Do nothing.
        }
    }

    /**
     * Serialization compatible String representation of a {@link QName} reference.
     * 
     * @param paramQName
     *            The {@QName} reference.
     * @return the string representation
     */
    private String qNameToString(final QName paramQName) {
        assert paramQName != null;
        String retVal;

        if (paramQName.getPrefix().isEmpty()) {
            retVal = paramQName.getLocalPart();
        } else {
            retVal = paramQName.getPrefix() + ":" + paramQName.getLocalPart();
        }

        return retVal;
    }

    /**
     * Generate a String representation from a {@link StartElement}.
     * 
     * @param paramStartTag
     *            The {@link StartElement} to serialize.
     * @param paramDoc
     *            The {@link StyledDocument} from the {@link JTextPane} instance.
     * @param paramHasChild
     *            {@link Child}.
     */
    private void processStartTag(final StartElement paramStartTag, final StyledDocument paramDoc,
        final Child paramHasChild) {
        assert paramStartTag != null;
        assert paramDoc != null;

        final Style styleElements = paramDoc.addStyle("elements", null);
        StyleConstants.setForeground(styleElements, ELEMENT_COLOR);
        final Style styleNamespaces = paramDoc.addStyle("attributes", null);
        StyleConstants.setForeground(styleNamespaces, NAMESPACE_COLOR);
        final Style styleAttributes = paramDoc.addStyle("attributes", null);
        StyleConstants.setForeground(styleAttributes, ATTRIBUTE_COLOR);

        try {
            final String qName = qNameToString(paramStartTag.getName());
            paramDoc.insertString(paramDoc.getLength(), "<" + qName, styleElements);

            // Insert a space if namespaces or attributes follow.
            if (paramStartTag.getAttributes().hasNext() || paramStartTag.getNamespaces().hasNext()) {
                paramDoc.insertString(paramDoc.getLength(), " ", styleElements);
            }

            // Process namespaces.
            for (final Iterator<?> namespaces = paramStartTag.getNamespaces(); namespaces.hasNext();) {
                final Namespace ns = (Namespace)namespaces.next();
                if (ns.getPrefix().isEmpty()) {
                    paramDoc.insertString(paramDoc.getLength(), " xmlns=" + ns.getNamespaceURI(),
                        styleNamespaces);
                } else {
                    paramDoc.insertString(paramDoc.getLength(), " xmlns:" + ns.getPrefix() + "="
                        + ns.getNamespaceURI(), styleNamespaces);
                }

                if (paramStartTag.getAttributes().hasNext()) {
                    paramDoc.insertString(paramDoc.getLength(), " ", styleElements);
                }
            }

            // Process attributes.
            for (final Iterator<?> attributes = paramStartTag.getAttributes(); attributes.hasNext();) {
                final Attribute att = (Attribute)attributes.next();

                paramDoc.insertString(paramDoc.getLength(), new StringBuilder().append(
                    qNameToString(att.getName())).append("=\"").append(att.getValue()).append("\"")
                    .toString(), styleAttributes);

                if (attributes.hasNext()) {
                    paramDoc.insertString(paramDoc.getLength(), " ", styleElements);
                }
            }

            switch (paramHasChild) {
            case CHILD:
                paramDoc.insertString(paramDoc.getLength(), ">" + NEWLINE, styleElements);
                break;
            case NOCHILD:
                paramDoc.insertString(paramDoc.getLength(), "/>" + NEWLINE, styleElements);
                break;
            default:
                break;
            }
        } catch (final BadLocationException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /**
     * Indent serialized output.
     * 
     * @param paramDocument
     *            {@link StyledDocument}.
     * @param paramLevel
     *            Current level in the tree.
     * @param paramIndentSpaces
     *            Determines how many spaces to indent at every level.
     */
    private void indent(final StyledDocument paramDocument, final int paramLevel,
        final String paramIndentSpaces) {
        try {
            for (int i = 0; i < paramLevel; i++) {
                paramDocument.insertString(paramDocument.getLength(), paramIndentSpaces, paramDocument
                    .addStyle(null, null));
            }
        } catch (final BadLocationException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }
}
