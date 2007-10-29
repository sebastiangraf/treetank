package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AttributeAxis;
import org.treetank.utils.FastStack;
import org.treetank.utils.UTF;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class SubtreeSAXGenerator extends Thread {

  /** Stack for remembering next nodeKey in document order. */
  private final FastStack<Long> mRightSiblingKeyStack;

  private final FastStack<INode> stack;

  private final FastStack<Long> subtreeKeyStack;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  private INode mCurrentNode;

  private IReadTransaction mRTX;

  private boolean noMoreElements;

  private final ContentHandler mHandler;

  public SubtreeSAXGenerator(
      final IReadTransaction input,
      final ContentHandler paramHandler) throws Exception {
    mRTX = input;
    mRTX.moveTo(IConstants.DOCUMENT_KEY);
    mRightSiblingKeyStack = new FastStack<Long>();
    stack = new FastStack<INode>();
    subtreeKeyStack = new FastStack<Long>();
    mNextKey = input.getFirstChildKey();

    mHandler = paramHandler;
    noMoreElements = false;
  }

  public final void run() {
    try {
      mHandler.startDocument();
      while (nextEventAvaliable()) {
        Thread.yield();
      }
      mHandler.endDocument();

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

  }

  public final boolean nextEventAvaliable() throws Exception {
    if (noMoreElements) {
      if (this.stack.size() > 0) {
        emitEndElement(stack.pop(), mRTX);
        return true;
      } else {
        return false;
      }
    } else {
      mCurrentNode = mRTX.moveTo(mNextKey);

      // Fail if there is no node anymore.
      if (mCurrentNode == null) {
        this.noMoreElements = true;
        if (this.stack.size() > 0) {
          return true;
        } else {
          return false;
        }
      }

      // Always follow first child if there is one.
      if (mCurrentNode.hasFirstChild()) {
        mNextKey = mCurrentNode.getFirstChildKey();
        if (mCurrentNode.hasRightSibling()) {
          mRightSiblingKeyStack.push(mCurrentNode.getRightSiblingKey());
        }
        emitNode(mCurrentNode, mRTX);
        return true;
      }

      // Then follow right sibling if there is one.
      if (mCurrentNode.hasRightSibling()) {
        mNextKey = mCurrentNode.getRightSiblingKey();
        emitNode(mCurrentNode, mRTX);
        return true;
      } else if (stack.size() > 0 && stack.peek().getNodeKey() != mCurrentNode.getLeftSiblingKey()) {
        emitEndElement(stack.pop(), mRTX);
        return true;
      }

      // Then follow right sibling on stack.
      if (mRightSiblingKeyStack.size() > 0) {
        mNextKey = mRightSiblingKeyStack.pop();
        emitNode(mCurrentNode, mRTX);
        return true;
      }

      // Then end.
      mNextKey = IConstants.NULL_KEY;
      return true;
    }
  }

  public void subtreeStarting(final long subtreeID) throws SAXException {
    try {
      subtreeKeyStack.push(this.mNextKey);
      mRTX.moveToDocument();
      mRTX.moveToFirstChild();
      do {
        if (mRTX.getLocalPart().matches("((\\D)+)(.{1})((\\d)+){1}")) {
          final long currentSubtreeID =
              Long.parseLong(mRTX.getLocalPart().replaceAll("((\\D)+)", ""));
          if (currentSubtreeID == subtreeID) {
            mNextKey = mRTX.getNodeKey();
          }
        } else {
          throw new SAXException(
              "Invalid XML Layout, just splitelements should occur on the first level!");
        }

      } while (mRTX.moveToRightSibling() != null);
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  public void subtreeEnding(final long subtreeID) throws SAXException {
    try {
      mNextKey = subtreeKeyStack.pop();
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  public ContentHandler getHandler() {
    return mHandler;
  }

  public void closeTransaction() {
    //    this.mRTX.close();
  }

  private final void emitNode(final INode node, final IReadTransaction rtx)
      throws Exception {
    // Emit events of current node.
    switch (node.getKind()) {
    case IConstants.ELEMENT:
      // Emit start element.
      mHandler.startElement(node.getURI(rtx), node.getLocalPart(rtx), qName(
          node.getPrefix(rtx),
          node.getLocalPart(rtx)), visitAttributes(rtx));
      break;
    case IConstants.TEXT:
      final char[] text = UTF.convert(node.getValue()).toCharArray();
      mHandler.characters(text, 0, text.length);
      break;
    default:
      throw new IllegalStateException("Unknown kind: " + node.getKind());
    }
  }

  private final String qName(final String prefix, final String localPart) {
    return (prefix.length() > 0 ? prefix + ":" + localPart : localPart);
  }

  private final AttributesImpl visitAttributes(final IReadTransaction rtx)
      throws Exception {

    final AttributesImpl attributes = new AttributesImpl();

    for (final INode attribute : new AttributeAxis(rtx)) {
      attributes.addAttribute(attribute.getURI(rtx), attribute
          .getLocalPart(rtx), qName(attribute.getPrefix(rtx), attribute
          .getLocalPart(rtx)), "", UTF.convert(attribute.getValue()));
    }

    return attributes;
  }

  private final void emitEndElement(final INode node, final IReadTransaction rtx)
      throws Exception {
    mHandler.endElement(node.getURI(rtx), node.getLocalPart(rtx), qName(node
        .getPrefix(rtx), node.getLocalPart(rtx)));
  }

}
