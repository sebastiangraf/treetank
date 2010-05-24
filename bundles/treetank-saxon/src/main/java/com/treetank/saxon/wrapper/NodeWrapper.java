package com.treetank.saxon.wrapper;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.EmptyIterator;
import net.sf.saxon.om.FastStringBuffer;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceIterator;
import net.sf.saxon.om.Navigator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SiblingCountingNode;
import net.sf.saxon.om.SingletonIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.VirtualNode;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AncestorAxis;
import com.treetank.axis.AttributeAxis;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.FollowingAxis;
import com.treetank.axis.FollowingSiblingAxis;
import com.treetank.axis.ParentAxis;
import com.treetank.axis.PrecedingAxis;
import com.treetank.axis.PrecedingSiblingAxis;
import com.treetank.axis.TextFilter;
import com.treetank.exception.TreetankException;
import com.treetank.settings.ENodes;

/**
 * Wraps a Treetank node.
 * 
 * @author johannes
 *
 */
public class NodeWrapper implements NodeInfo, VirtualNode, SiblingCountingNode {

  /** Write Transaction. */
  protected transient static IReadTransaction mRTX;

  /** Kind of current node. */
  protected transient ENodes nodeKind;

  /** Document wrapper. */
  protected transient DocumentWrapper mDocWrapper;

  /** Logger. */
  protected static final Log LOGGER = LogFactory.getLog(NodeWrapper.class);

  /** Key of node. */
  protected transient final long mKey;

  /** Treetank session. */
  private transient final ISession mSession;

  /** Treetank node. */
  protected transient final IItem node;

  /** QName of current node. */
  protected transient final QName qName;

  /**
   * A node in the XML parse tree. Wrap a TreeTank node.
   * 
   * @param session
   *            TreeTank session.
   * @param nodekeyToStart
   *            NodeKey to move to.
   */
  protected NodeWrapper(final ISession session, final long nodekeyToStart) {
    try {
      if (mRTX == null) {
        mRTX = session.beginReadTransaction();
      }
      mRTX.moveTo(nodekeyToStart);
    } catch (TreetankException e) {
      LOGGER.error("TreetankException: " + e.getMessage(), e);
    }

    mSession = session;
    mRTX.moveTo(mRTX.getNode().getNodeKey());
    nodeKind = mRTX.getNode().getKind();
    mKey = mRTX.getNode().getNodeKey();
    node = mRTX.getNode();
    qName = mRTX.getQNameOfCurrentNode();
  }

  /**
   * Wrap a Treetank transaction object into a Saxon implementation of a node.
   * 
   * @param session
   *            Treetank session.
   * @param docWrapper
   *            A document wrapper.
   * @param key
   *            Node key of treetank item/node.
   * @return nodeWrapper.
   */
  protected NodeWrapper makeWrapper(
      final ISession session,
      final DocumentWrapper docWrapper,
      final long key) {

    final NodeWrapper mNodeWrapper = new NodeWrapper(session, key);
    mNodeWrapper.mDocWrapper = docWrapper;

    return mNodeWrapper;
  }

  /**
   * Get document wrapper.
   * 
   * @return document wrapper.
   */
  public DocumentWrapper getmDocWrapper() {
    return mDocWrapper;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#atomize()
   */
  public Value atomize() throws XPathException {
    StringValue value = null;

    switch (nodeKind) {
    case COMMENT_KIND:
    case PROCESSING_KIND:
      value = new StringValue(getStringValueCS());
      break;
    default:
      value = new UntypedAtomicValue(getStringValueCS());
    }

    return value;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#compareOrder(NodeInfo)
   */
  public int compareOrder(final NodeInfo node) {
    mRTX.moveTo(mKey);
    int retVal;

    // Should be in the same document.
    if (getDocumentNumber() != node.getDocumentNumber()) {
      retVal = -2;
    }

    if (((NodeWrapper) node).mKey > mKey) {
      retVal = -1;
    } else if (((NodeWrapper) node).mKey == mKey) {
      retVal = 0;
    } else {
      retVal = 1;
    }

    return retVal;
  }

  /**
   * Copy this node to a given outputter (deep copy).
   * 
   * @see net.sf.saxon.om.NodeInfo#copy(Receiver, int, boolean, int)
   */
  public void copy(
      final Receiver out,
      final int whichNamespaces,
      final boolean copyAnnotations,
      final int locationId) throws XPathException {
    Navigator.copy(
        this,
        out,
        mDocWrapper.getNamePool(),
        whichNamespaces,
        copyAnnotations,
        locationId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#generateId(FastStringBuffer)
   */
  public void generateId(final FastStringBuffer buf) {
    buf.append(String.valueOf(mKey));
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getAttributeValue(int)
   */
  public String getAttributeValue(final int fingerprint) {
    String attVal = null;

    if (nodeKind == ENodes.ELEMENT_KIND) {
      final NamePool pool = mDocWrapper.getNamePool();
      final String uri = pool.getURI(fingerprint);
      final String local = pool.getLocalName(fingerprint);

      final long atts = mRTX.getNode().getAttributeCount();

      for (int i = 0; i < atts; i++) {
        mRTX.moveToAttribute(i);

        final String localPart = qName.getLocalPart();

        if (local.equals(localPart) && uri.equals(qName.getNamespaceURI())) {
          attVal = mRTX.getValueOfCurrentNode();
          break;
        }

        mRTX.moveTo(mKey);
      }
    }

    return attVal;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getBaseURI()
   */
  public String getBaseURI() {
    mRTX.moveTo(mKey);
    String baseURI = getAttributeValue(StandardNames.XML_BASE);

    if (baseURI == null) {
      baseURI = mDocWrapper.getBaseURI();
    }

    return baseURI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getColumnNumber()
   */
  public int getColumnNumber() {
    throw new UnsupportedOperationException("Not supported by TreeTank.");
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getConfiguration()
   */
  public Configuration getConfiguration() {
    return mDocWrapper.getConfiguration();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getDeclaredNamespaces(int[])
   */
  public int[] getDeclaredNamespaces(final int[] buffer) {
    mRTX.moveTo(mKey);
    if (nodeKind == ENodes.ELEMENT_KIND) {
      final int count = mRTX.getNode().getNamespaceCount();

      if (count == 0) {
        return EMPTY_NAMESPACE_LIST;
      } else {

        int[] result =
            (buffer == null || count > buffer.length ? new int[count] : buffer);
        final NamePool pool = getNamePool();
        int n = 0;

        for (int i = 0; i < count; i++) {
          mRTX.moveTo(i);
          final String prefix = getPrefix();
          final String uri = qName.getNamespaceURI();
          mRTX.moveTo(mKey);

          result[n++] = pool.allocateNamespaceCode(prefix, uri);
        }

        /*
         * If the supplied array is larger than required, then the first
         * unused entry will be set to -1.
         */
        if (count < result.length) {
          result[count] = -1;
        }

        return result;
      }
    }

    return new int[0];
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getDisplayName()
   */
  public String getDisplayName() {
    String dName = "";

    switch (nodeKind) {
    case ELEMENT_KIND:
    case ATTRIBUTE_KIND:
      mRTX.moveTo(mKey);
      dName = getPrefix() + ":" + qName.getLocalPart();
      break;
    case NAMESPACE_KIND:
    case PROCESSING_KIND:
      dName = getLocalPart();
      break;
    default:
      // Do nothing.
    }

    return dName;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getDocumentNumber()
   */
  public long getDocumentNumber() {
    mRTX.moveTo(mKey);
    return mDocWrapper.getBaseURI().hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getDocumentRoot()
   */
  public DocumentInfo getDocumentRoot() {
    return mDocWrapper;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getFingerprint()
   */
  public int getFingerprint() {
    final int nameCount = getNameCode();

    if (nameCount == -1) {
      return -1;
    } else {
      return nameCount & 0xfffff;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getLineNumber()
   */
  public int getLineNumber() {
    throw new UnsupportedOperationException("Not supported by TreeTank.");
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getLocalPart()
   */
  public String getLocalPart() {
    String localPart = "";

    switch (nodeKind) {
    case ELEMENT_KIND:
    case ATTRIBUTE_KIND:
      localPart = qName.getLocalPart();
      break;
    default:
      // Do nothing.
    }

    return localPart;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getNameCode()
   */
  public int getNameCode() {
    int nameCode = -1;

    switch (nodeKind) {
    case ELEMENT_KIND:
    case ATTRIBUTE_KIND:
    case PROCESSING_KIND:
    case NAMESPACE_KIND:
      nameCode =
          mDocWrapper.getNamePool().allocate(
              getPrefix(),
              getURI(),
              getLocalPart());
      break;
    default:
      // Do nothing.
    }

    return nameCode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getNamePool()
   */
  public NamePool getNamePool() {
    return mDocWrapper.getNamePool();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getNodeKind()
   */
  public int getNodeKind() {
    return nodeKind.getNodeIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getParent()
   */
  public NodeInfo getParent() {
    if (mRTX.getNode().hasParent()) {
      // Parent transaction.
      final NodeInfo mParent =
          makeWrapper(mSession, mDocWrapper, mRTX.getNode().getParentKey());
      return mParent;
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getPrefix()
   */
  public String getPrefix() {
    String prefix = "";

    switch (nodeKind) {
    case ELEMENT_KIND:
    case ATTRIBUTE_KIND:
      prefix = qName.getPrefix();
      break;
    default:
      /*
       * Change nothing, return empty String in case of a node which isn't
       * an element or attribute.
       */
    }

    return prefix;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getRoot()
   */
  public NodeInfo getRoot() {
    return (NodeInfo) mDocWrapper;
  }

  /**
   * getStringValue() just calls getStringValueCS().
   * 
   * @see net.sf.saxon.om.NodeInfo#getStringValue()
   */
  public final String getStringValue() {
    return getStringValueCS().toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getStringValueCS()
   */
  public final CharSequence getStringValueCS() {
    mRTX.moveTo(mKey);
    String mValue;
    switch (nodeKind) {
    case ROOT_KIND:
    case ELEMENT_KIND:
      return expandString();
    case ATTRIBUTE_KIND:
      mValue = mRTX.getValueOfCurrentNode();
      return emptyIfNull(mValue);
    case TEXT_KIND:
      mValue = mRTX.getValueOfCurrentNode();
      return mValue;
    case COMMENT_KIND:
    case PROCESSING_KIND:
      mValue = mRTX.getValueOfCurrentNode();
      return emptyIfNull(mValue);
    default:
      return "";
    }
  }

  /**
   * Treat a node value of null as an empty string.
   * 
   * @param s
   *            The node value.
   * @return a zero-length string if s is null, otherwise s.
   */
  private static String emptyIfNull(final String s) {
    return (s == null ? "" : s);
  }

  /**
   * Filter text nodes.
   * 
   * @return concatenated String of text node values.
   */
  private String expandString() {
    final FilterAxis axis =
        new FilterAxis(new DescendantAxis(mRTX), new TextFilter(mRTX));
    final FastStringBuffer fsb = new FastStringBuffer(FastStringBuffer.SMALL);
    fsb.append("");

    for (final long key : axis) {
      if (mRTX.getNode().isText()) {
        fsb.append(mRTX.getValueOfCurrentNode());
      }
    }

    mRTX.moveTo(mKey);

    return fsb.condense().toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getSystemId()
   */
  public String getSystemId() {
    mRTX.moveTo(mKey);
    return mDocWrapper.mBaseURI;
  }

  /**
   * Get the type annotation.
   * 
   * @return UNTYPED or UNTYPED_ATOMIC.
   */
  public int getTypeAnnotation() {
    mRTX.moveTo(mKey);
    int type = 0;
    if (nodeKind == ENodes.ATTRIBUTE_KIND) {
      type = StandardNames.XS_UNTYPED_ATOMIC;
    } else {
      type = StandardNames.XS_UNTYPED;
    }
    return type;
  }

  /**
   * Get the URI part of the name of this node. This is the URI corresponding
   * to the prefix, or the URI of the default namespace if appropriate.
   * 
   * @return The URI of the namespace of this node. For an unnamed node,
   *         return null. For a node with an empty prefix, return an empty
   *         string.
   */
  public String getURI() {
    String URI = "";

    switch (nodeKind) {
    case ELEMENT_KIND:
    case ATTRIBUTE_KIND:
      URI = qName.getNamespaceURI();
      break;
    default:
      // Do nothing.
    }

    // Return URI or empty string.
    return URI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#hasChildNodes()
   */
  public boolean hasChildNodes() {
    mRTX.moveTo(mKey);
    return mRTX.getNode().getChildCount() > 0;
  }

  /**
   * Not supported.
   * 
   * @see net.sf.saxon.om.NodeInfo#isId()
   */
  public boolean isId() {
    return false;
  }

  /**
   * Not supported.
   * 
   * @see net.sf.saxon.om.NodeInfo#isIdref()
   */
  public boolean isIdref() {
    return false;
  }

  /**
   * Not supported.
   * 
   * @see net.sf.saxon.om.NodeInfo#isNilled()
   */
  public boolean isNilled() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#isSameNodeInfo(NodeInfo)
   */
  public boolean isSameNodeInfo(final NodeInfo other) {
    if (!(other instanceof NodeInfo)) {
      return false;
    }

    return ((NodeWrapper) other).mKey == mKey;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#iterateAxis(byte)
   */
  public AxisIterator iterateAxis(final byte axisNumber) {
    return iterateAxis(axisNumber, AnyNodeTest.getInstance());
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#iterateAxis(byte, NodeTest)
   */
  public AxisIterator iterateAxis(final byte axisNumber, final NodeTest nodeTest) {
    mRTX.moveTo(mKey);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("NODE TEST: " + nodeTest);
    }

    switch (axisNumber) {
    case Axis.ANCESTOR:
      if (getNodeKind() == ENodes.ROOT_KIND.getNodeIdentifier()) {
        return EmptyIterator.getInstance();
      }
      return new Navigator.AxisFilter(
          new Enumeration(new AncestorAxis(mRTX)),
          nodeTest);

    case Axis.ANCESTOR_OR_SELF:
      if (getNodeKind() == ENodes.ROOT_KIND.getNodeIdentifier()) {
        return Navigator.filteredSingleton(this, nodeTest);
      }
      return new Navigator.AxisFilter(new Enumeration(new AncestorAxis(
          mRTX,
          true)), nodeTest);

    case Axis.ATTRIBUTE:
      if (getNodeKind() != ENodes.ELEMENT_KIND.getNodeIdentifier()) {
        return EmptyIterator.getInstance();
      }
      return new Navigator.AxisFilter(
          new Enumeration(new AttributeAxis(mRTX)),
          nodeTest);

    case Axis.CHILD:
      if (hasChildNodes()) {
        return new Navigator.AxisFilter(
            new Enumeration(new ChildAxis(mRTX)),
            nodeTest);
      } else {
        return EmptyIterator.getInstance();
      }

    case Axis.DESCENDANT:
      if (hasChildNodes()) {
        return new Navigator.AxisFilter(new Enumeration(
            new DescendantAxis(mRTX)), nodeTest);
      } else {
        return EmptyIterator.getInstance();
      }

    case Axis.DESCENDANT_OR_SELF:
      return new Navigator.AxisFilter(new Enumeration(new DescendantAxis(
          mRTX,
          true)), nodeTest);

    case Axis.FOLLOWING:
      return new Navigator.AxisFilter(
          new Enumeration(new FollowingAxis(mRTX)),
          nodeTest);

    case Axis.FOLLOWING_SIBLING:
      switch (nodeKind) {
      case ROOT_KIND:
      case ATTRIBUTE_KIND:
      case NAMESPACE_KIND:
        return EmptyIterator.getInstance();
      default:
        return new Navigator.AxisFilter(new Enumeration(
            new FollowingSiblingAxis(mRTX)), nodeTest);
      }

    case Axis.NAMESPACE:
      if (getNodeKind() != ENodes.ELEMENT_KIND.getNodeIdentifier()) {
        return EmptyIterator.getInstance();
      }
      return NamespaceIterator.makeIterator(this, nodeTest);

    case Axis.PARENT:
      if (mRTX.getNode().getParentKey() == ENodes.ROOT_KIND.getNodeIdentifier()) {
        return EmptyIterator.getInstance();
      }
      return new Navigator.AxisFilter(
          new Enumeration(new ParentAxis(mRTX)),
          nodeTest);

    case Axis.PRECEDING:
      return new Navigator.AxisFilter(
          new Enumeration(new PrecedingAxis(mRTX)),
          nodeTest);

    case Axis.PRECEDING_SIBLING:
      switch (nodeKind) {
      case ROOT_KIND:
      case ATTRIBUTE_KIND:
      case NAMESPACE_KIND:
        return EmptyIterator.getInstance();
      default:
        return new Navigator.AxisFilter(new Enumeration(
            new PrecedingSiblingAxis(mRTX)), nodeTest);
      }

    case Axis.SELF:
      return Navigator.filteredSingleton(this, nodeTest);

    case Axis.PRECEDING_OR_ANCESTOR:
      return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(
          this,
          true), nodeTest);

    default:
      throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#setSystemId(String)
   */
  public void setSystemId(final String systemId) {
    mDocWrapper.setBaseURI(systemId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.NodeInfo#getTypedValue()
   */
  public SequenceIterator getTypedValue() throws XPathException {
    return SingletonIterator.makeIterator((AtomicValue) atomize());
  }

  /**
   * Just calls getUnderlying node.
   * 
   * @see net.sf.saxon.om.VirtualNode#getRealNode()
   */
  public Object getRealNode() {
    return getUnderlyingNode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.VirtualNode#getUnderlyingNode()
   */
  public Object getUnderlyingNode() {
    return node;
  }

  /**
   * {@inheritDoc}
   * 
   * @see net.sf.saxon.om.SiblingCountingNode#getSiblingPosition()
   */
  public int getSiblingPosition() {
    mRTX.moveTo(mKey);
    int index = 0;

    while (mRTX.getNode().hasLeftSibling()) {
      mRTX.moveToLeftSibling();

      index++;
    }

    mRTX.moveTo(mKey);
    return index;
  }

  /**
   * <h1>SaxonEnumeration</h1>
   * 
   * <p>
   * Saxon adaptor for axis iterations.
   * </p>
   */
  public final class Enumeration extends Navigator.BaseEnumeration {

    /** Treetank axis iterator. */
    private final IAxis mAxis;

    /**
     * Constructor.
     * 
     * @param axis
     *            TreeTank axis iterator.
     */
    public Enumeration(final IAxis axis) {
      mAxis = axis;
    }

    /**
     * {@inheritDoc}
     * 
     * @see net.sf.saxon.om.Navigator$BaseEnumeration#advance()
     */
    public void advance() {
      if (mAxis.hasNext()) {
        final long key = mAxis.getTransaction().getNode().getNodeKey();
        final long nextKey = mAxis.next();
        current = makeWrapper(mSession, mDocWrapper, nextKey);
        mAxis.getTransaction().moveTo(key);
      } else {
        current = null;
      }
    }

    /**
     * {@inheritDoc}
     * 
     * @see net.sf.saxon.om.Navigator$BaseEnumeration#getAnother()
     */
    @Override
    public SequenceIterator getAnother() {
      return new Enumeration(mAxis);
    }
  }

}
