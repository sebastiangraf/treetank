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

package org.treetank.api;

import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.DummyNode;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;

/**
 * Interface which must be implemented from visitors to implement functionality based on the
 * visitor pattern.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface IVisitor {
    /**
     * Do something when visiting an {@link ElementNode}.
     * 
     * @param paramNode
     *            the {@link ElementNode}
     */
    void visit(final ElementNode paramNode);

    /**
     * Do something when visiting a {@link AttributeNode}.
     * 
     * @param paramNode
     *            the {@link AttributeNode}
     */
    void visit(final AttributeNode paramNode);

    /**
     * Do something when visiting a {@link NamespaceNode}.
     * 
     * @param paramNode
     *            the {@link NamespaceNode}
     */
    void visit(final NamespaceNode paramNode);

    /**
     * Do something when visiting a {@link TextNode}.
     * 
     * @param paramNode
     *            the {@link ElementNode}
     */
    void visit(final TextNode paramNode);

    /**
     * Do something when visiting the {@link DocumentRootNode}.
     * 
     * @param paramNode
     *            the {@link DocumentRootNode}
     */
    void visit(final DocumentRootNode paramNode);

    /**
     * Do something when visiting a {@link DummyNode}.
     * 
     * @param paramNode
     *            the {@link ElementNode}
     */
    void visit(final DummyNode paramNode);
}
