package org.treetank.xmllayer;

import java.util.Hashtable;

import org.treetank.api.ISession;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SubtreeSAXHandler extends SAXHandler {

  private static final int COMMITTHRESHOLD = 10000;

  /** Idefix write transaction. */
  private final ISession session;

  /**
   * Counter for commiting after the insertation of a fixed number of
   * nodes
   */
  private int nodeCounter = 0;

  /** Mapping for nodekeys for each subtree */
  private final Hashtable<Long, Long> subtreeKeyNodeMapping;

  /**
   * Constructor.
   * 
   * @param initDocument
   *            Name of document.
   * @param initSession
   *            Writing transaction to write to.
   */
  public SubtreeSAXHandler(final ISession initSession) throws Exception {
    super(initSession);
    session = initSession;
    subtreeKeyNodeMapping = new Hashtable<Long, Long>(0);
  }

  @Override
  public void endDocument() throws SAXException {
    try {
      if (!this.subtreeKeyNodeMapping.isEmpty()) {
        throw new IllegalStateException(
            "Mapping not empty. There are subtrees left to be closed.");
      }
      super.endDocument();
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(
      final String uri,
      final String localName,
      final String qName,
      final Attributes attr) throws SAXException {

    try {

      if (nodeCounter > COMMITTHRESHOLD) {
        final long tempkey = mWTX.getNodeKey();
        session.commit();
        mWTX = session.beginWriteTransaction();
        System.gc();
        mWTX.moveTo(tempkey);
        nodeCounter = 0;
      }
      nodeCounter++;

      super.startElement(uri, localName, qName, attr);

    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  /**
   * Starts a new subtree with the given subtreeID. The current node is
   * stored in the mapping and the writetransaction is moved to the first
   * level in order to insert a new subtree.
   * 
   * @param subtreeID
   *            to start
   * @throws SAXException
   */
  public void subtreeStarting(final long subtreeID) throws SAXException {
    try {

      this.subtreeKeyNodeMapping.put(subtreeID, mWTX.getNodeKey());

      // while (mLeftSiblingKeyStack.size() > 0) {
      // if (mLeftSiblingKeyStack.peek() != IConstants.NULL_KEY) {
      // mWTX.moveTo(mLeftSiblingKeyStack.peek());
      //
      // if (this.subtreeKeyNodeMapping.containsValue(mWTX
      // .getParentKey())) {
      // break;
      // } else {
      // mLeftSiblingKeyStack.pop();
      // }
      // } else {
      // break;
      // }
      // }
      mWTX.moveToRoot();
      // mLeftSiblingKeyStack.push(IConstants.NULL_KEY);

      // while (mWTX.getRightSiblingKey() != IConstants.NULL_KEY)
      // {
      // mWTX.moveToRightSibling();
      // }

    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  /**
   * Ends the current subtree. The pointer is moved to the last position
   * where the new subtree event happens
   * 
   * @param subtreeID
   *            where the new subtree starts
   * @throws SAXException
   */
  public void subtreeEnding(final long subtreeID) throws SAXException {
    try {
      session.commit();
      mWTX = session.beginWriteTransaction();
      System.gc();
      nodeCounter = 0;
      mWTX.moveTo(this.subtreeKeyNodeMapping.remove(subtreeID));
    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

}
