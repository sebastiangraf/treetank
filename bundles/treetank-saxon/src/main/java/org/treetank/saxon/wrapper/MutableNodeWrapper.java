/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.ChildAxis;
import com.treetank.exception.AbsTTException;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

import net.sf.saxon.event.Builder;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;

/**
 * <h1>MutableNodeWrapper</h1>
 * 
 * <p>
 * Implements all methods which are needed to create a modifiable Saxon internal node. Therefore it wraps
 * Treetank's nodes into the appropriate format.
 * </p>
 * 
 * <p>
 * <strong>Currently not used.</strong> For use with XQuery Update and requires a "commercial" Saxon license.
 * Furthermore as of now not stable and doesn't support third party applications. Needs to be fully
 * implemented and tested.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class MutableNodeWrapper extends NodeWrapper implements MutableNodeInfo {

    /** Treetank write transaction. */
    private final IWriteTransaction mWTX;

    /**
     * Constructor.
     * 
     * @param database
     *            Treetank database.
     * @param wtx
     *            Treetank write transaction.
     * @throws TreetankException
     *             in case of something went wrong.
     */
    protected MutableNodeWrapper(final IDatabase database, final IWriteTransaction wtx) throws AbsTTException {
        super(database, 0);
        mWTX = database.getSession().beginWriteTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttribute(final int nameCode, final int typeCode, final CharSequence value,
        final int properties) {
        if (mWTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
            String uri = "";
            String local = "";

            for (int i = 0; i < ((ElementNode)mWTX.getNode()).getAttributeCount(); i++) {
                mWTX.moveToAttribute(i);

                NamePool pool = mDocWrapper.getNamePool();
                uri = pool.getURI(nameCode);
                local = pool.getLocalName(nameCode);

                if (uri.equals(mWTX.getQNameOfCurrentNode().getNamespaceURI())
                    && local.equals(getLocalPart())) {
                    throw new IllegalStateException("Attribute with the given name already exists!");
                }

                mWTX.moveTo(mKey);
            }

            try {
                mWTX.insertAttribute(mWTX.getQNameOfCurrentNode(), (String)value);
            } catch (AbsTTException e) {
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
            throw new IllegalArgumentException("Namespace code is not present in the name pool!");
        }

        // Insert Namespace.
        if (mWTX.getQNameOfCurrentNode().getNamespaceURI() != uri && getPrefix() != prefix) {
            try {
                mWTX.insertNamespace(uri, prefix);

                // Add namespace to child nodes if prefix
                if (inherit) {
                    final AbsAxis axis = new ChildAxis(mWTX);
                    while (axis.hasNext()) {
                        if (getPrefix() != prefix) {
                            mWTX.insertNamespace(uri, prefix);
                        }
                        axis.next();
                    }
                }
            } catch (AbsTTException e) {
                LOGGER.error("Insert Namespace failed: " + e.getMessage(), e);
            }
            // Already bound.
        } else if (mWTX.getQNameOfCurrentNode().getNamespaceURI() != uri && getPrefix() == prefix) {
            throw new IllegalArgumentException("An URI is already bound to this prefix!");
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
        } catch (AbsTTException e) {
            LOGGER.error("Removing current node failed: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertChildren(NodeInfo[] source, boolean atStart, boolean inherit) {
        switch (mWTX.getNode().getKind()) {
        case ROOT_KIND:
        case ELEMENT_KIND:
            boolean first = true;
            for (final NodeInfo node : source) {
                try {
                    if (first) {
                        mWTX.insertElementAsFirstChild(new QName(node.getURI(), node.getLocalPart(), node
                            .getPrefix()));
                        first = false;
                    } else {
                        mWTX.insertElementAsRightSibling(new QName(node.getURI(), node.getLocalPart(), node
                            .getPrefix()));
                    }
                } catch (AbsTTException e) {
                    LOGGER.error("Insertion of element failed: " + e.getMessage(), e);
                }
            }

            mWTX.moveTo(mKey);
            break;
        default:
            throw new IllegalStateException("");
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
                    mWTX.insertElementAsFirstChild(new QName(node.getURI(), node.getLocalPart(), node
                        .getPrefix()));

                    if (inherit) {
                        mWTX.insertNamespace(uri, prefix);
                    }

                    mWTX.moveToParent();
                } catch (AbsTTException e) {
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
                    mWTX.insertElementAsRightSibling(new QName(node.getDisplayName(), node.getURI()));

                    if (inherit) {
                        mWTX.insertNamespace(uri, prefix);
                    }
                } catch (AbsTTException e) {
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
        if (mWTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
            for (int i = 0, attCount = ((ElementNode)mWTX.getNode()).getAttributeCount(); i < attCount; i++) {
                mWTX.moveToAttribute(i);
                try {
                    if (mWTX.getQNameOfCurrentNode().equals(attribute.getDisplayName())) {
                        mWTX.remove();
                    }
                } catch (AbsTTException e) {
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
