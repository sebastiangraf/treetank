package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.UTF;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SubtreeSAXGenerator extends SAXGenerator {

  private boolean firstElement = true;

  private boolean lastElement = true;

  private final FastLongStack subtreeKeyStack = new FastLongStack();

  public SubtreeSAXGenerator(
      final IReadTransaction initTrx,
      final ContentHandler contentHandler) throws Exception {
    super(initTrx, contentHandler);
  }

  @Override
  public final void run() {
    try {

      while (fireNextEvent()) {
        Thread.yield();
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public final boolean fireNextEvent() throws Exception {
    // Iterate over all descendants.
    if (firstElement) {
      mHandler.startDocument();
      firstElement = false;
      return true;
    }
    while (mRTX.moveTo(mNextKey)) {

      // debug();

      // --- Clean up all pending closing tags.
      // --------------------------------
      while (mRightSiblingKeyStack.size() > 0
          && mRTX.getNodeKey() == mRightSiblingKeyStack.peek()) {
        mRightSiblingKeyStack.pop();
        final INode node = (INode) mNodeStack.pop();
        final String localPart = mRTX.nameForKey(node.getLocalPartKey());
        final String prefix = mRTX.nameForKey(node.getPrefixKey());
        final String uri = mRTX.nameForKey(node.getURIKey());
        if (localPart.length() > 0) {
          mHandler.endElement(uri, localPart, qName(prefix, localPart));
          return true;
        }
      }

      setNextKey();

      // --- Emit events based on current node.
      // --------------------------------
      switch (mRTX.getKind()) {
      case IConstants.ELEMENT:
        final INode node = (INode) mNodeStack.peek();
        final String localPart = mRTX.nameForKey(node.getLocalPartKey());
        final String prefix = mRTX.nameForKey(node.getPrefixKey());
        final String uri = mRTX.nameForKey(node.getURIKey());
        mHandler.startElement(
            uri,
            localPart,
            qName(prefix, localPart),
            visitAttributes());
        return true;
      case IConstants.TEXT:
        final char[] text = UTF.convert(mRTX.getValue()).toCharArray();
        mHandler.characters(text, 0, text.length);
        return true;
      case IConstants.PROCESSING_INSTRUCTION:
        mHandler.processingInstruction(mRTX.getLocalPart(), UTF.convert(mRTX
            .getValue()));
        return true;
      default:
        throw new IllegalStateException("Unknown kind: " + mRTX.getKind());

      }

    }

    // Clean up all pending closing tags.
    while (mNodeStack.size() > 0) {
      mRightSiblingKeyStack.pop();
      final INode node = (INode) mNodeStack.pop();
      final String localPart = mRTX.nameForKey(node.getLocalPartKey());
      final String prefix = mRTX.nameForKey(node.getPrefixKey());
      final String uri = mRTX.nameForKey(node.getURIKey());
      if (localPart.length() > 0) {
        mHandler.endElement(uri, localPart, qName(prefix, localPart));
        return true;
      }
    }
    if (lastElement) {
      mHandler.endDocument();
      lastElement = false;
      return true;
    }

    return false;
  }

  public void subtreeStarting(final long subtreeID) throws SAXException {
    try {
      subtreeKeyStack.push(this.mNextKey);
      mRTX.moveToRoot();
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

      } while (mRTX.moveToRightSibling());
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

}
