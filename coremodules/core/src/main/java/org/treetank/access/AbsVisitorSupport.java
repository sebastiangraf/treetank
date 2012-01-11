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

package org.treetank.access;

import org.treetank.api.IVisitor;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;

/**
 * <h1>AbsVisitorSupport</h1>
 * 
 * <p>
 * Inspired by the dom4j approach <code>VisitorSupport</code> is an abstract base class which is useful for
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
 * IVisitor visitor = new AbsVisitorSupport(rtx) {
 *      public void visit(final ElementNode pNode) {
 *              mRtx.moveTo(pNode.getNodeKey());
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

    /** {@inheritDoc} */
    @Override
    public void visit(final ElementNode pNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final TextNode pNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final DocumentRootNode pNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final AttributeNode pNode) {

    }

    /** {@inheritDoc} */
    @Override
    public void visit(final NamespaceNode pNode) {

    }
}
