package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.utils.FastStack;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SubtreeSAXGenerator extends SAXGenerator {

  private final FastStack<INode> subtreeKeyStack;

  private INode lastNode;

  private INode currentNode;

  public SubtreeSAXGenerator(
      final IReadTransaction input,
      final ContentHandler contentHandler,
      final boolean prettyPrint) throws Exception {
    super(new DescendantAxis(input), contentHandler, prettyPrint);
    subtreeKeyStack = new FastStack<INode>();
    currentNode = super.mAxis.next();
  }

  private boolean oneStepFurther() {
    if (super.mAxis.hasNext()) {
      lastNode = currentNode;
      currentNode = super.mAxis.next();
      return true;
    } else {
      return false;
    }
  }

  public final boolean nextEventAvaliable() throws Exception {

    final IReadTransaction trx = super.mAxis.getTransaction();
    if (trx.hasFirstChild()) {
      emitNode(currentNode, trx);
      if (currentNode.getKind() == IConstants.ELEMENT) {
        super.stack.push(currentNode);
      }
    } else {
      //evaluating endElement
      if (currentNode.getKind() == IConstants.ELEMENT) {
        if (super.stack.peek().getNodeKey() == currentNode.getParentKey()) {
          emitEndElement(currentNode, trx);
        }
      }

    }
    return oneStepFurther();
  }

  public void subtreeStarting(final long subtreeID) throws SAXException {
    //    try {
    //      subtreeKeyStack.push(this.mNextKey);
    //      mRTX.moveToDocument();
    //      mRTX.moveToFirstChild();
    //      do {
    //        if (mRTX.getLocalPart().matches("((\\D)+)(.{1})((\\d)+){1}")) {
    //          final long currentSubtreeID =
    //              Long.parseLong(mRTX.getLocalPart().replaceAll("((\\D)+)", ""));
    //          if (currentSubtreeID == subtreeID) {
    //            mNextKey = mRTX.getNodeKey();
    //          }
    //        } else {
    //          throw new SAXException(
    //              "Invalid XML Layout, just splitelements should occur on the first level!");
    //        }
    //
    //      } while (mRTX.moveToRightSibling() != null);
    //    } catch (Exception e) {
    //      throw new SAXException(e);
    //    }
  }

  public void subtreeEnding(final long subtreeID) throws SAXException {
    //    try {
    //      mNextKey = subtreeKeyStack.pop();
    //    } catch (Exception e) {
    //      throw new SAXException(e);
    //    }
  }

  public ContentHandler getHandler() {
    return mHandler;
  }

  public void closeTransaction() {
    //    this.mRTX.close();
  }

}
