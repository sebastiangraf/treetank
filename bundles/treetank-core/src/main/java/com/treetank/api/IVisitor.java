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
package com.treetank.api;

import com.treetank.node.*;

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
