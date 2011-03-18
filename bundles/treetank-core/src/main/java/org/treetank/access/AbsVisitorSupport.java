/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package org.treetank.access;

import org.treetank.api.ISession;
import org.treetank.api.IVisitor;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.*;

/**
 * <h1>AbsVisitorSupport</h1>
 * 
 * <p>
 * Based on the dom4j approach <code>VisitorSupport</code> is an abstract base class which is useful for
 * implementation inheritence or when using anonymous inner classes to create simple <code>Visitor</code>
 * implementations.
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <code><pre>
 * final IVisitor visitor = new NamespaceChangeVisitor(session);
 * for (final AbsAxis axis = new DescendantAxis(rtx); axis.hasNext(); axis.next()) {
 *      rtx.getNode().acceptVisitor(visitor);
 * }
 * </pre></code>
 * 
 * <code><pre>
 * IVisitor visitor = new AbsVisitorSupport(session) {
 *      public void visit(final ElementNode paramNode) {
 *              mRtx.moveTo(paramNode.getNodeKey());
 *              System.out.println(
 *                      "Element name: " + mRtx.getCurrentQName().getLocalName()
 *              );
 *      }     
 * };
 * 
 * for (final AbsAxis axis = new DescendantAxis(rtx); axis.hasNext(); axis.next()) {
 *      rtx.getNode().acceptVisitor(visitor);
 * }
 * </pre></code>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public abstract class AbsVisitorSupport implements IVisitor {

    /** {@link IWriteTransaction} instance. */
    protected final IWriteTransaction mWtx;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession} reference
     * @throws AbsTTException
     *             if creating {@link IWriteTransaction} fails
     */
    public AbsVisitorSupport(final ISession paramSession) throws AbsTTException {
        mWtx = paramSession.beginWriteTransaction();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final ElementNode paramNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final TextNode paramNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final DocumentRootNode paramNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final AttributeNode paramNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final DummyNode paramNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final NamespaceNode paramNode) {

    }
}
