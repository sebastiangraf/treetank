package com.treetank.saxon.wrapper;

import javax.xml.namespace.QName;

import net.sf.saxon.event.Builder;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.ChildAxis;
import com.treetank.exception.TreetankException;
import com.treetank.settings.ENodes;

/**
 * Currently not used. For use with XQuery Update. Requires a "commercial" Saxon 
 * license.
 * 
 * Implements all methods which modify the (existing) tree.
 * 
 * @author johannes
 *
 */
public class MutableNodeWrapper extends NodeWrapper implements MutableNodeInfo {

  /** Treetank write transaction. */
  private final IWriteTransaction mWTX;

  /**
   * Constructor.
   * 
   * @param session Treetank session.
   * @param wtx Treetank write transaction.
   * @throws TreetankException in case of something went wrong.
   */
  protected MutableNodeWrapper(
      final ISession session,
      final IWriteTransaction wtx) throws TreetankException {
    super(session, 0);
    mWTX = session.beginWriteTransaction();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAttribute(
      final int nameCode,
      final int typeCode,
      final CharSequence value,
      final int properties) {
    if (mWTX.getNode().isElement()) {
      String uri = "";
      String local = "";

      for (int i = 0; i < mWTX.getNode().getAttributeCount(); i++) {
        mWTX.moveToAttribute(i);

        NamePool pool = mDocWrapper.getNamePool();
        uri = pool.getURI(nameCode);
        local = pool.getLocalName(nameCode);

        if (uri.equals(mWTX.getQNameOfCurrentNode().getNamespaceURI())
            && local.equals(getLocalPart())) {
          throw new IllegalStateException(
              "Attribute with the given name already exists!");
        }

        mWTX.moveTo(mKey);
      }

      try {
        mWTX.insertAttribute(mWTX.getQNameOfCurrentNode(), (String) value);
      } catch (TreetankException e) {
        LOGGER.error("Couldn't insert Attribute: " + e.getMessage(), e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNamespace(int nscode, boolean inherit) {
    final NamePool pool = mDocWrapper.getNamePool();
    final String uri = pool.getURI(nscode);
    final String prefix = pool.getPrefix(nscode);

    // Not present in name pool.
    if (uri == null || prefix == null) {
      throw new IllegalArgumentException(
          "Namespace code is not present in the name pool!");
    }

    // Insert Namespace.
    if (mWTX.getQNameOfCurrentNode().getNamespaceURI() != uri
        && getPrefix() != prefix) {
      try {
        mWTX.insertNamespace(uri, prefix);

        // Add namespace to child nodes if prefix
        if (inherit) {
          for (final long key : new ChildAxis(mWTX)) {
            if (getPrefix() != prefix) {
              mWTX.insertNamespace(uri, prefix);
            }
          }
        }
      } catch (TreetankException e) {
        LOGGER.error("Insert Namespace failed: " + e.getMessage(), e);
      }
      // Already bound.
    } else if (mWTX.getQNameOfCurrentNode().getNamespaceURI() != uri
        && getPrefix() == prefix) {
      throw new IllegalArgumentException(
          "An URI is already bound to this prefix!");
    }

    // Do nothing is uri and prefix already are bound.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete() {
    try {
      mWTX.remove();
    } catch (TreetankException e) {
      LOGGER.error("Removing current node failed: " + e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insertChildren(NodeInfo[] source, boolean atStart, boolean inherit) {
    if (mWTX.getNode().isDocumentRoot() || mWTX.getNode().isElement()) {
      boolean first = true;
      for (final NodeInfo node : source) {
        try {
          if (first) {
            mWTX.insertElementAsFirstChild(new QName(node.getURI(), node
                .getLocalPart(), node.getPrefix()));
            first = false;
          } else {
            mWTX.insertElementAsRightSibling(new QName(node.getURI(), node
                .getLocalPart(), node.getPrefix()));
          }
        } catch (TreetankException e) {
          LOGGER.error("Insertion of element failed: " + e.getMessage(), e);
        }
      }

      mWTX.moveTo(mKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insertSiblings(NodeInfo[] source, boolean before, boolean inherit) {
    if (before) {
      mWTX.moveToParent();

      final String uri = mWTX.getQNameOfCurrentNode().getNamespaceURI();
      final String prefix = getPrefix();

      for (final NodeInfo node : source) {
        try {
          mWTX.insertElementAsFirstChild(new QName(node.getURI(), node
              .getLocalPart(), node.getPrefix()));

          if (inherit) {
            mWTX.insertNamespace(uri, prefix);
          }

          mWTX.moveToParent();
        } catch (TreetankException e) {
          LOGGER.error("Inserting element failed: " + e.getMessage(), e);
        }
      }

      mWTX.moveTo(mKey);
    } else {
      // Get URI and prefix of parent node.
      final long key = mWTX.getNode().getNodeKey();
      mWTX.moveToParent();
      final String uri = mWTX.getQNameOfCurrentNode().getNamespaceURI();
      final String prefix = getPrefix();
      mWTX.moveTo(key);

      for (final NodeInfo node : source) {
        try {
          mWTX.insertElementAsRightSibling(new QName(
              node.getDisplayName(),
              node.getURI()));

          if (inherit) {
            mWTX.insertNamespace(uri, prefix);
          }
        } catch (TreetankException e) {
          LOGGER.error("Inserting element failed: " + e.getMessage(), e);
        }
      }
    }

    mWTX.moveTo(mKey);
  }

  @Override
  public boolean isDeleted() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Builder newBuilder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeAttribute(final NodeInfo attribute) {
    if (mWTX.getNode().isAttribute()
        && ENodes.ATTRIBUTE_KIND.getNodeIdentifier() == attribute.getNodeKind()) {

      for (int i = 0, attCount = mWTX.getNode().getAttributeCount(); i < attCount; i++) {
        mWTX.moveToAttribute(i);
        try {
          if (mWTX.getQNameOfCurrentNode().equals(attribute.getDisplayName())) {
            mWTX.remove();
          }
        } catch (TreetankException e) {
          LOGGER.error("Removing attribute failed: " + e.getMessage(), e);
        }
        mWTX.moveTo(mKey);
      }
    }
  }

  @Override
  public void removeTypeAnnotation() {
    // TODO Auto-generated method stub

  }

  @Override
  public void rename(final int newNameCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void replace(final NodeInfo[] replacement, final boolean inherit) {
    // TODO Auto-generated method stub

  }

  @Override
  public void replaceStringValue(final CharSequence stringValue) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setTypeAnnotation(final int typeCode) {
    // TODO Auto-generated method stub

  }
}
