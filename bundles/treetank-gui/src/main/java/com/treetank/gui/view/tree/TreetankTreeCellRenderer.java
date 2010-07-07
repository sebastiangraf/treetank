package com.treetank.gui.view.tree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.ElementNode;

/**
 * <h>TreetankTreeCellRenderer</h1>
 * 
 * <p>Customized tree cell renderer to render nodes nicely.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class TreetankTreeCellRenderer extends DefaultTreeCellRenderer {

  /**
   * Generated UID.
   */
  private static final long serialVersionUID = -6242168246410260644L;
  
  /** Logger. */
  private static final Log LOGGER =
      LogFactory.getLog(TreetankTreeCellRenderer.class);

  /** Element color. */
  private final Color elementColor = new Color(0, 0, 128);

  /** Attribute color. */
  private final Color attributeColor = new Color(0, 128, 0);

  /** Treetant reading transaction. */
  private transient static IReadTransaction mRTX;
  
  /** Treetank databse. */
  protected transient static IDatabase mDatabase;

  /** Path to file. */
  private static String PATH;

  /**
   * Constructor.
   * 
   * @param database
   *                Treetank database.
   *                
   */
  public TreetankTreeCellRenderer(
      final IDatabase database) {
    this(database, 0);
  }
  
  /**
   * Constructor.
   * 
   * @param database
   *                Treetank database.
   * @param nodekeyToStart
   *                Starting point of transaction.
   *                
   */
  public TreetankTreeCellRenderer(
      final IDatabase database,
      final long nodekeyToStart) {
    setOpenIcon(null);
    setClosedIcon(null);
    setLeafIcon(null);
    setBackgroundNonSelectionColor(null);
    setTextSelectionColor(Color.red);

    try {
      if (mDatabase == null || mDatabase.getFile() == null
          || !(mDatabase.getFile().equals(database.getFile()))) {
        mDatabase = database;

        if (mRTX != null && !mRTX.isClosed()) {
          mRTX.close();
        }
      }

      if (mRTX == null || mRTX.isClosed()) {
        mRTX = mDatabase.getSession().beginReadTransaction();
      }
      mRTX.moveTo(nodekeyToStart);
    } catch (final TreetankException e) {
      LOGGER.error("TreetankException: " + e.getMessage(), e);
    }

    PATH = database.getFile().getAbsolutePath();
  }

  /**
   * {@inheritDoc}
   */
  public Component getTreeCellRendererComponent(
      final JTree tree,
      Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    final IItem node = (IItem) value;

    final long key = node.getNodeKey();
    
    switch (node.getKind()) {
    case ELEMENT_KIND:
      mRTX.moveTo(node.getNodeKey());
      final String prefix = mRTX.getQNameOfCurrentNode().getPrefix();
      final QName qName = mRTX.getQNameOfCurrentNode();

      if (prefix == null || prefix == "") {
        final String localPart = qName.getLocalPart();
        
        if (((ElementNode) mRTX.getNode()).hasFirstChild()) {
          value = '<' + localPart + '>';
        } else {
          value = '<' + localPart + "/>";
        }
      } else {
        value = '<' + prefix + ":" + qName.getLocalPart() + '>';
      }

      break;
    case ATTRIBUTE_KIND:
      // Move transaction to given attribute node.
      mRTX.moveTo(node.getParentKey());
      final long aNodeKey = node.getNodeKey();
      for (int i = 0, attsCount =
          ((ElementNode) mRTX.getNode()).getAttributeCount(); i < attsCount; i++) {
        mRTX.moveToAttribute(i);
        if (mRTX.getNode().equals(node)) {
          break;
        }
        mRTX.moveTo(aNodeKey);
      }

      // Display value.
      final String attPrefix = mRTX.getQNameOfCurrentNode().getPrefix();
      final QName attQName = mRTX.getQNameOfCurrentNode();

      if (attPrefix == null || attPrefix == "") {
        value =
            '@'
                + attQName.getLocalPart()
                + "='"
                + mRTX.getValueOfCurrentNode()
                + "'";
      } else {
        value =
            '@'
                + attPrefix
                + ":"
                + attQName.getLocalPart()
                + "='"
                + mRTX.getValueOfCurrentNode()
                + "'";
      }

      break;
    case NAMESPACE_KIND:
      // Move transaction to given namespace node.
      mRTX.moveTo(node.getParentKey());
      final long nNodeKey = node.getNodeKey();
      for (int i = 0, namespCount =
          ((ElementNode) mRTX.getNode()).getNamespaceCount(); i < namespCount; i++) {
        mRTX.moveToNamespace(i);
        if (mRTX.getNode().equals(node)) {
          break;
        }
        mRTX.moveTo(nNodeKey);
      }

      if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
        value = "xmlns='" + mRTX.nameForKey(mRTX.getNode().getURIKey()) + "'";
      } else {
        value =
            "xmlns:"
                + mRTX.nameForKey(mRTX.getNode().getNameKey())
                + "='"
                + mRTX.nameForKey(mRTX.getNode().getURIKey())
                + "'";
      }
      break;
    case TEXT_KIND:
      mRTX.moveTo(node.getNodeKey());
      value = mRTX.getValueOfCurrentNode();
      break;
    case COMMENT_KIND:
      mRTX.moveTo(node.getNodeKey());
      value = "<!-- " + mRTX.getValueOfCurrentNode() + " -->";
      break;
    case PROCESSING_KIND:
      mRTX.moveTo(node.getNodeKey());
      value = "<? " + mRTX.getValueOfCurrentNode() + " ?>";
      break;
    case ROOT_KIND:
      value = PATH;
      break;
    case WHITESPACE_KIND:
      break;
    default:
      new IllegalStateException("Node kind not known!");
    }

    value = value + " [" + key + "]";
    
    super.getTreeCellRendererComponent(
        tree,
        value,
        sel,
        expanded,
        leaf,
        row,
        hasFocus);
    if (!selected) {
      switch (node.getKind()) {
      case ELEMENT_KIND:
        setForeground(elementColor);
        break;
      case ATTRIBUTE_KIND:
        setForeground(attributeColor);
        break;
      }
    }

    return this;
  }

  public Color getBackground() {
    return null;
  }
}
