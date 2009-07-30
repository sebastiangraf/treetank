/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: NodePage.java 4443 2008-08-30 16:28:14Z kramis $
 */

package com.treetank.page;

import java.nio.ByteBuffer;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AbstractNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
import com.treetank.utils.IConstants;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
@Entity
public class NodePage extends AbstractPage {

	/** Key of node page. This is the base key of all contained nodes. */
	@PrimaryKey
	private final long mNodePageKey;

	/** Array of nodes. This can have null nodes that were removed. */
	private final AbstractNode[] mNodes;

	/**
	 * Create node page.
	 * 
	 * @param nodePageKey
	 *            Base key assigned to this node page.
	 */
	public NodePage(final long nodePageKey) {
		super(0);
		mNodePageKey = nodePageKey;
		mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];
	}

	/**
	 * Read node page.
	 * 
	 * @param in
	 *            Input bytes to read page from.
	 * @param nodePageKey
	 *            Base key assigned to this node page.
	 */
	public NodePage(final ByteBuffer in, final long nodePageKey) {
		super(0, in);
		mNodePageKey = nodePageKey;
		mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];

		final long[] values = new long[IConstants.NDP_NODE_COUNT];
		for (int i = 0; i < values.length; i++) {
			values[i] = in.getLong();
		}

		final long keyBase = mNodePageKey << IConstants.NDP_NODE_COUNT_EXPONENT;
		for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
			final int kind = (int) values[offset];
			switch (kind) {
			case IConstants.UNKNOWN:
				// Was null node, do nothing here.
				break;
			case IReadTransaction.DOCUMENT_ROOT_KIND:
				getNodes()[offset] = new DocumentRootNode(in);
				break;
			case IReadTransaction.ELEMENT_KIND:
				getNodes()[offset] = new ElementNode(keyBase + offset, in);
				break;
			case IReadTransaction.ATTRIBUTE_KIND:
				getNodes()[offset] = new AttributeNode(keyBase + offset, in);
				break;
			case IReadTransaction.NAMESPACE_KIND:
				getNodes()[offset] = new NamespaceNode(keyBase + offset, in);
				break;
			case IReadTransaction.TEXT_KIND:
				getNodes()[offset] = new TextNode(keyBase + offset, in);
				break;
			default:
				throw new IllegalStateException(
						"Unsupported node kind encountered during read: "
								+ kind);
			}
		}
	}

	/**
	 * Clone node page.
	 * 
	 * @param committedNodePage
	 *            Node page to clone.
	 */
	public NodePage(final NodePage committedNodePage) {
		super(0, committedNodePage);
		mNodePageKey = committedNodePage.mNodePageKey;
		mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];

		// Deep-copy all nodes.
		for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
			if (committedNodePage.getNodes()[offset] != null) {
				final int kind = committedNodePage.getNodes()[offset].getKind();
				switch (kind) {
				case IConstants.UNKNOWN:
					// Was null node, do nothing here.
					break;
				case IReadTransaction.DOCUMENT_ROOT_KIND:
					getNodes()[offset] = new DocumentRootNode(committedNodePage
							.getNodes()[offset]);
					break;
				case IReadTransaction.ELEMENT_KIND:
					getNodes()[offset] = new ElementNode(committedNodePage
							.getNodes()[offset]);
					break;
				case IReadTransaction.ATTRIBUTE_KIND:
					getNodes()[offset] = new AttributeNode(committedNodePage
							.getNodes()[offset]);
					break;
				case IReadTransaction.NAMESPACE_KIND:
					getNodes()[offset] = new NamespaceNode(committedNodePage
							.getNodes()[offset]);
					break;
				case IReadTransaction.TEXT_KIND:
					getNodes()[offset] = new TextNode(committedNodePage
							.getNodes()[offset]);
					break;
				default:
					throw new IllegalStateException(
							"Unsupported node kind encountered during clone: "
									+ kind);
				}
			}
		}
	}

	public NodePage(final PageReference<NodePage> realPageReference,
			final long nodePageKey) {
		super(0, realPageReference.getPage());
		mNodePageKey = nodePageKey;
		mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];
	}

	/**
	 * Get key of node page.
	 * 
	 * @return Node page key.
	 */
	public final long getNodePageKey() {
		return mNodePageKey;
	}

	/**
	 * Get node at a given offset.
	 * 
	 * @param offset
	 *            Offset of node within local node page.
	 * @return Node at given offset.
	 */
	public AbstractNode getNode(final int offset) {
		return getNodes()[offset];
	}

	/**
	 * Overwrite a single node at a given offset.
	 * 
	 * @param offset
	 *            Offset of node to overwrite in this node page.
	 * @param node
	 *            Node to store at given nodeOffset.
	 */
	public void setNode(final int offset, final AbstractNode node) {
		getNodes()[offset] = node;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final ByteBuffer out) {
		super.serialize(out);

		for (int i = 0; i < getNodes().length; i++) {
			if (getNodes()[i] != null) {
				out.putLong(getNodes()[i].getKind());
			} else {
				out.putLong(IConstants.UNKNOWN);
			}
		}

		for (final AbstractNode node : getNodes()) {
			if (node != null) {
				node.serialize(out);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		String returnString = super.toString() + ": nodePageKey="
				+ mNodePageKey + ", isDirty=" + isDirty() + " nodes: \n";
		for (final AbstractNode node : getNodes()) {
			returnString = returnString + node.getNodeKey() + ",";
		}

		return returnString;
	}

	/**
	 * @return the mNodes
	 */
	protected AbstractNode[] getNodes() {
		return mNodes;
	}
}
