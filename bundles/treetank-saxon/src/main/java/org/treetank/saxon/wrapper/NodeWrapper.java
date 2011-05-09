/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.saxon.wrapper;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.NamespaceIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.wrapper.SiblingCountingNode;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AncestorAxis;
import org.treetank.axis.AttributeAxis;
import org.treetank.axis.ChildAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.FollowingAxis;
import org.treetank.axis.FollowingSiblingAxis;
import org.treetank.axis.ParentAxis;
import org.treetank.axis.PrecedingAxis;
import org.treetank.axis.PrecedingSiblingAxis;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;

/**
 * <h1>NodeWrapper</h1>
 * 
 * <p>
 * Wraps a Treetank node into Saxon's internal representation of a node. It therefore implements Saxon's core
 * interface NodeInfo as well as two others:
 * </p>
 * 
 * <dl>
 * <dt>NodeInfo</dt>
 * <dd>The NodeInfo interface represents a node in Saxon's implementation of the XPath 2.0 data model.</dd>
 * <dt>VirtualNode</dt>
 * <dd>This interface is implemented by NodeInfo implementations that act as wrappers on some underlying tree.
 * It provides a method to access the real node underlying the virtual node, for use by applications that need
 * to drill down to the underlying data.</dd>
 * <dt>SiblingCountingNode</dt>
 * <dd>Interface that extends NodeInfo by providing a method to get the position of a node relative to its
 * siblings.</dd>
 * </dl>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class NodeWrapper implements NodeInfo, VirtualNode, SiblingCountingNode {

    /** Treetank reading transaction. */
    protected final IReadTransaction mRTX;

    /** Kind of current node. */
    protected transient ENodes nodeKind;

    /** Document wrapper. */
    protected transient DocumentWrapper mDocWrapper;

    /**
     * Log wrapper for better output.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(NodeWrapper.class);

    /** Key of node. */
    protected transient final long mKey;

    /** Treetank node. */
    protected transient final IItem node;

    /** QName of current node. */
    protected transient QName qName;

    /**
     * A node in the XML parse tree. Wrap a Treetank node.
     * 
     * @param database
     *            Treetank database.
     * @param nodekeyToStart
     *            NodeKey to move to.
     */
    protected NodeWrapper(final IDatabase database, final long nodekeyToStart) {
        try {

            mRTX = database.getSession(new SessionConfiguration()).beginReadTransaction();
            mRTX.moveTo(nodekeyToStart);

            mRTX.moveTo(mRTX.getNode().getNodeKey());
            nodeKind = mRTX.getNode().getKind();
            mKey = mRTX.getNode().getNodeKey();
            node = mRTX.getNode();

            if (nodeKind == ENodes.ELEMENT_KIND || nodeKind == ENodes.ATTRIBUTE_KIND) {
                qName = mRTX.getQNameOfCurrentNode();
            }
        } catch (final AbsTTException e) {
            LOGGER.error("TreetankException: " + e.getMessage(), e);
        }
    }

    public NodeWrapper wrapNode(final long key) {
        mRTX.moveTo(key);
        return this;
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
     */
    @Override
    public Value atomize() throws XPathException {
        Value value = null;

        switch (nodeKind) {
        case COMMENT_KIND:
        case PROCESSING_KIND:
            // The content as an instance of the xs:string data type.
            value = new StringValue(getStringValueCS());
            break;
        default:
            // The content as an instance of the xdt:untypedAtomic data type.
            value = new UntypedAtomicValue(getStringValueCS());
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareOrder(final NodeInfo node) {
        int retVal;

        // Should be in the same document.
        if (getDocumentNumber() != node.getDocumentNumber()) {
            retVal = -2;
        }

        // FIXME fix the key order, this can result in errors related to
        // different version of a file.
        else if (((NodeWrapper)node).mKey > mKey) {
            retVal = -1;
        } else if (((NodeWrapper)node).mKey == mKey) {
            retVal = 0;
        } else {
            retVal = 1;
        }

        return retVal;
    }

    /**
     * Copy this node to a given outputter (deep copy).
     * 
     * @see net.sf.saxon.om.NodeInfo#copy(Receiver, int, int)
     */
    public void copy(final Receiver out, final int copyOption, final int locationId) throws XPathException {
        Navigator.copy(this, out, mDocWrapper.getNamePool(), copyOption, locationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateId(final FastStringBuffer buf) {
        buf.append(String.valueOf(mKey));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttributeValue(final int fingerprint) {
        String attVal = null;

        final NameTest test = new NameTest(Type.ATTRIBUTE, fingerprint, getNamePool());
        final AxisIterator iterator = iterateAxis(Axis.ATTRIBUTE, test);
        final NodeInfo attribute = (NodeInfo)iterator.next();

        if (attribute != null) {
            attVal = attribute.getStringValue();
        }

        mRTX.moveTo(mKey);

        return attVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseURI() {
        mRTX.moveTo(mKey);
        String baseURI = null;

        NodeInfo node = this;

        while (node != null) {
            baseURI = node.getAttributeValue(StandardNames.XML_BASE);

            if (baseURI == null) {
                // Search for baseURI in parent node (xml:base="").
                node = node.getParent();
            } else {
                break;
            }
        }

        if (baseURI == null) {
            baseURI = mDocWrapper.getBaseURI();
        }

        return baseURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber() {
        throw new UnsupportedOperationException("Not supported by TreeTank.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return mDocWrapper.getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getDeclaredNamespaces(final int[] buffer) {
        int[] retVal = null;
        if (nodeKind == ENodes.ELEMENT_KIND) {
            final int count = ((ElementNode)node).getNamespaceCount();

            if (count == 0) {
                retVal = EMPTY_NAMESPACE_LIST;
            } else {
                retVal = (buffer == null || count > buffer.length ? new int[count] : buffer);
                final NamePool pool = getNamePool();
                int n = 0;

                for (int i = 0; i < count; i++) {
                    mRTX.moveTo(i);
                    final String prefix = getPrefix();
                    final String uri = getURI();
                    mRTX.moveTo(mKey);

                    retVal[n++] = pool.allocateNamespaceCode(prefix, uri);
                }

                /*
                 * If the supplied array is larger than required, then the first
                 * unused entry will be set to -1.
                 */
                if (count < retVal.length) {
                    retVal[count] = -1;
                }
            }
        }

        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        String dName = "";

        switch (nodeKind) {
        case ELEMENT_KIND:
        case ATTRIBUTE_KIND:
            dName = getPrefix() + ":" + getLocalPart();
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
     */
    @Override
    public long getDocumentNumber() {
        return mDocWrapper.getBaseURI().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentInfo getDocumentRoot() {
        return mDocWrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFingerprint() {
        int retVal;

        final int nameCount = getNameCode();
        if (nameCount == -1) {
            retVal = -1;
        } else {
            retVal = nameCount & 0xfffff;
        }

        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        throw new UnsupportedOperationException("Not supported by TreeTank.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public int getNameCode() {
        int nameCode = -1;

        switch (nodeKind) {
        case ELEMENT_KIND:
        case ATTRIBUTE_KIND:
        case PROCESSING_KIND:
            // case NAMESPACE_KIND:
            nameCode = mDocWrapper.getNamePool().allocate(getPrefix(), getURI(), getLocalPart());
            break;
        default:
            // text, comment, document and namespace nodes.
        }

        return nameCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamePool getNamePool() {
        return mDocWrapper.getNamePool();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNodeKind() {
        return nodeKind.getNodeIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeInfo getParent() {
        mRTX.moveTo(mKey);
        if (mRTX.getNode().hasParent()) {
            // Parent transaction.
            final NodeInfo mParent = wrapNode(mRTX.getNode().getParentKey());
            return mParent;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public NodeInfo getRoot() {
        return (NodeInfo)mDocWrapper;
    }

    /**
     * getStringValue() just calls getStringValueCS().
     * 
     */
    @Override
    public final String getStringValue() {
        return getStringValueCS().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final CharSequence getStringValueCS() {
        mRTX.moveTo(mKey);
        String mValue;
        switch (nodeKind) {
        case ROOT_KIND:
        case ELEMENT_KIND:
            mValue = expandString();
            break;
        case ATTRIBUTE_KIND:
            mValue = emptyIfNull(mRTX.getValueOfCurrentNode());
            break;
        case TEXT_KIND:
            mValue = mRTX.getValueOfCurrentNode();
            break;
        case COMMENT_KIND:
        case PROCESSING_KIND:
            mValue = emptyIfNull(mRTX.getValueOfCurrentNode());
            break;
        default:
            mValue = "";
        }

        return mValue;
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
        final FilterAxis axis = new FilterAxis(new DescendantAxis(mRTX), new TextFilter(mRTX));
        final FastStringBuffer fsb = new FastStringBuffer(FastStringBuffer.SMALL);

        while (axis.hasNext()) {
            if (mRTX.getNode().getKind() == ENodes.TEXT_KIND) {
                fsb.append(mRTX.getValueOfCurrentNode());
            }
            axis.next();
        }
        mRTX.moveTo(mKey);

        return fsb.condense().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemId() {
        return mDocWrapper.getBaseURI();
    }

    /**
     * Get the type annotation.
     * 
     * @return UNTYPED or UNTYPED_ATOMIC.
     */
    public int getTypeAnnotation() {
        int type = 0;
        if (nodeKind == ENodes.ATTRIBUTE_KIND) {
            type = StandardNames.XS_UNTYPED_ATOMIC;
        } else {
            type = StandardNames.XS_UNTYPED;
        }
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getURI() {
        String URI = "";

        switch (nodeKind) {
        case ELEMENT_KIND:
        case ATTRIBUTE_KIND:
        case NAMESPACE_KIND:
            if (!"".equals(qName.getPrefix())) {
                URI = qName.getNamespaceURI();
            }
            break;
        default:
            // Do nothing.
        }

        // Return URI or empty string.
        return URI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildNodes() {
        boolean hasChildNodes = false;
        if (mRTX.getStructuralNode().getChildCount() > 0) {
            hasChildNodes = true;
        }
        return hasChildNodes;
    }

    /**
     * Not supported.
     */
    @Override
    public boolean isId() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdref() {
        throw new UnsupportedOperationException("Currently not supported by Treetank!");
        // return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNilled() {
        throw new UnsupportedOperationException("Currently not supported by Treetank!");
        // return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSameNodeInfo(final NodeInfo other) {
        boolean retVal;

        if (!(other instanceof NodeInfo)) {
            retVal = false;
        } else {
            retVal = ((NodeWrapper)other).mKey == mKey;
        }

        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AxisIterator iterateAxis(final byte axisNumber) {
        return iterateAxis(axisNumber, AnyNodeTest.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            return new Navigator.AxisFilter(new SaxonEnumeration(new AncestorAxis(mRTX)), nodeTest);

        case Axis.ANCESTOR_OR_SELF:
            if (getNodeKind() == ENodes.ROOT_KIND.getNodeIdentifier()) {
                return Navigator.filteredSingleton(this, nodeTest);
            }
            return new Navigator.AxisFilter(new SaxonEnumeration(new AncestorAxis(mRTX, true)), nodeTest);

        case Axis.ATTRIBUTE:
            if (getNodeKind() != ENodes.ELEMENT_KIND.getNodeIdentifier()) {
                return EmptyIterator.getInstance();
            }
            return new Navigator.AxisFilter(new SaxonEnumeration(new AttributeAxis(mRTX)), nodeTest);

        case Axis.CHILD:
            if (hasChildNodes()) {
                return new Navigator.AxisFilter(new SaxonEnumeration(new ChildAxis(mRTX)), nodeTest);
            } else {
                return EmptyIterator.getInstance();
            }

        case Axis.DESCENDANT:
            if (hasChildNodes()) {
                return new Navigator.AxisFilter(new SaxonEnumeration(new DescendantAxis(mRTX)), nodeTest);
            } else {
                return EmptyIterator.getInstance();
            }

        case Axis.DESCENDANT_OR_SELF:
            return new Navigator.AxisFilter(new SaxonEnumeration(new DescendantAxis(mRTX, true)), nodeTest);

        case Axis.FOLLOWING:
            return new Navigator.AxisFilter(new SaxonEnumeration(new FollowingAxis(mRTX)), nodeTest);

        case Axis.FOLLOWING_SIBLING:
            switch (nodeKind) {
            case ROOT_KIND:
            case ATTRIBUTE_KIND:
            case NAMESPACE_KIND:
                return EmptyIterator.getInstance();
            default:
                return new Navigator.AxisFilter(new SaxonEnumeration(new FollowingSiblingAxis(mRTX)),
                    nodeTest);
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
            return new Navigator.AxisFilter(new SaxonEnumeration(new ParentAxis(mRTX)), nodeTest);

        case Axis.PRECEDING:
            return new Navigator.AxisFilter(new SaxonEnumeration(new PrecedingAxis(mRTX)), nodeTest);

        case Axis.PRECEDING_SIBLING:
            switch (nodeKind) {
            case ROOT_KIND:
            case ATTRIBUTE_KIND:
            case NAMESPACE_KIND:
                return EmptyIterator.getInstance();
            default:
                return new Navigator.AxisFilter(new SaxonEnumeration(new PrecedingSiblingAxis(mRTX)),
                    nodeTest);
            }

        case Axis.SELF:
            return Navigator.filteredSingleton(this, nodeTest);

        case Axis.PRECEDING_OR_ANCESTOR:
            return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, true), nodeTest);

        default:
            throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemId(final String systemId) {
        mDocWrapper.setBaseURI(systemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SequenceIterator getTypedValue() throws XPathException {
        return SingletonIterator.makeIterator((AtomicValue)atomize());
    }

    /**
     * {@inheritDoc}
     */
    public Object getRealNode() {
        return getUnderlyingNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getUnderlyingNode() {
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSiblingPosition() {
        mRTX.moveTo(mKey);
        int index = 0;

        while (mRTX.getStructuralNode().hasLeftSibling()) {
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
    public final class SaxonEnumeration extends Navigator.BaseEnumeration {

        /** Treetank axis iterator. */
        private final AbsAxis mAxis;

        /**
         * Constructor.
         * 
         * @param paramAxis
         *            TreeTank axis iterator.
         */
        public SaxonEnumeration(final AbsAxis paramAxis) {
            mAxis = paramAxis;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void advance() {
            if (mAxis.hasNext()) {
                final long nextKey = mAxis.next();
                current = wrapNode(nextKey);
            } else {
                current = null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequenceIterator getAnother() {
            return new SaxonEnumeration(mAxis);
        }
    }

}
