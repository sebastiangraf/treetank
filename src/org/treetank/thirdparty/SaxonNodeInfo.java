/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.thirdparty;

import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.StringTokenIterator;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.FastStringBuffer;
import net.sf.saxon.om.FingerprintedNode;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.Navigator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Value;

import org.treetank.nodelayer.INode;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.utils.UTF;
import org.treetank.xmllayer.AncestorAxisIterator;
import org.treetank.xmllayer.AttributeAxisIterator;
import org.treetank.xmllayer.ChildAxisIterator;
import org.treetank.xmllayer.DescendantAxisIterator;
import org.treetank.xmllayer.ParentAxisIterator;

/**
 * <h1>SaxonNodeInfo</h1>
 * 
 * <p>
 * Saxon adaptor for node.
 * </p>
 */
public class SaxonNodeInfo implements NodeInfo, FingerprintedNode {

  /** Map TreeTank name keys to saxon fingerprints. */
  private static final Map<Integer, Integer> FINGERPRINT_MAP =
      new HashMap<Integer, Integer>();

  /** Saxon configuration. */
  private final Configuration config;

  /** TreeTank read transaction. */
  private final IReadTransaction trx;

  /** INode node.*/
  private final INode node;

  /**
   * Constructor.
   * 
   * @param initConfig Saxon configuration.
   * @param initTrx TreeTank read transaction.
   */
  public SaxonNodeInfo(
      final Configuration initConfig,
      final IReadTransaction initTrx) {
    config = initConfig;
    trx = initTrx;
    node = trx.getNode();
  }

  /**
   * {@inheritDoc}
   */
  public final Value atomize() throws XPathException {
    return new StringValue(UTF.convert(node.getValue()));
  }

  /**
   * {@inheritDoc}
   */
  public final int compareOrder(final NodeInfo arg0) {
    if (((SaxonNodeInfo) arg0).node.getNodeKey() > node.getNodeKey()) {
      return -1;
    } else if (((SaxonNodeInfo) arg0).node.getNodeKey() == node.getNodeKey()) {
      return 0;
    } else {
      return 1;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void copy(
      final Receiver arg0,
      final int arg1,
      final boolean arg2,
      final int arg3) throws XPathException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final void generateId(final FastStringBuffer arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeValue(final int arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final String getBaseURI() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final Configuration getConfiguration() {
    return config;
  }

  /**
   * {@inheritDoc}
   */
  public final int[] getDeclaredNamespaces(final int[] arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final String getDisplayName() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final int getDocumentNumber() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final DocumentInfo getDocumentRoot() {
    try {
      trx.moveToRoot();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return new SaxonDocumentInfo(config, trx);
  }

  /**
   * {@inheritDoc}
   */
  public final int getFingerprint() {
    Integer fingerprint = FINGERPRINT_MAP.get(node.getLocalPartKey());
    if (fingerprint == null) {
      try {
        final String string = trx.nameForKey(node.getLocalPartKey());
        synchronized (config.getNamePool()) {
          fingerprint = config.getNamePool().allocateClarkName(string);
          FINGERPRINT_MAP.put(node.getLocalPartKey(), fingerprint);
        }
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    return fingerprint;
  }

  /**
   * {@inheritDoc}
   */
  public final int getLineNumber() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() {
    String result = null;
    try {
      result = trx.nameForKey(node.getLocalPartKey());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public final int getNameCode() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final NamePool getNamePool() {
    return config.getNamePool();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNodeKind() {
    return node.getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final NodeInfo getParent() {
    try {
      trx.moveTo(node.getParentKey());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return new SaxonNodeInfo(config, trx);
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() {
    String result = null;
    try {
      result = trx.nameForKey(node.getLocalPartKey());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public final NodeInfo getRoot() {
    return getDocumentRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final String getStringValue() {
    return UTF.convert(node.getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final String getSystemId() {
    return "TreeTank Document";
  }

  /**
   * {@inheritDoc}
   */
  public final int getTypeAnnotation() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() {
    String result = null;
    try {
      result = trx.nameForKey(node.getURIKey());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasChildNodes() {
    return (node.getChildCount() > 0);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isSameNodeInfo(final NodeInfo arg0) {
    return ((SaxonNodeInfo) arg0).node.getNodeKey() == node.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final AxisIterator iterateAxis(final byte axisNumber) {
    return iterateAxis(axisNumber, AnyNodeTest.getInstance());
  }

  /**
   * {@inheritDoc}
   */
  public final AxisIterator iterateAxis(
      final byte axisNumber,
      final NodeTest nodeTest) {

    try {

      trx.moveTo(node.getNodeKey());

      switch (axisNumber) {

      case Axis.CHILD:
        return new Navigator.AxisFilter(new SaxonEnumeration(
            config,
            trx,
            new ChildAxisIterator(trx)), nodeTest);

      case Axis.ATTRIBUTE:
        return new Navigator.AxisFilter(new SaxonEnumeration(
            config,
            trx,
            new AttributeAxisIterator(trx)), nodeTest);

      case Axis.ANCESTOR:
        return new Navigator.AxisFilter(new SaxonEnumeration(
            config,
            trx,
            new AncestorAxisIterator(trx)), nodeTest);

      case Axis.DESCENDANT:
        return new Navigator.AxisFilter(new SaxonEnumeration(
            config,
            trx,
            new DescendantAxisIterator(trx)), nodeTest);

      case Axis.PARENT:
        return new Navigator.AxisFilter(new SaxonEnumeration(
            config,
            trx,
            new ParentAxisIterator(trx)), nodeTest);

      case Axis.SELF:
        return Navigator.filteredSingleton(this, nodeTest);

      default:

        throw new IllegalArgumentException("Unknown axis number " + axisNumber);

      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

  }

  /**
   * {@inheritDoc}
   */
  public final void sendNamespaceDeclarations(
      final Receiver arg0,
      final boolean arg1) throws XPathException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final void setSystemId(final String arg0) {
    throw new UnsupportedOperationException();

  }

  /**
   * {@inheritDoc}
   */
  public final CharSequence getStringValueCS() {
    return UTF.convert(node.getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final SequenceIterator getTypedValue() throws XPathException {
    return new StringTokenIterator(UTF.convert(node.getValue()));
  }

}
