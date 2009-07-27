package com.treetank.page;

import java.util.Map;

import com.treetank.node.AbstractNode;
import com.treetank.utils.FastWeakHashMap;
import com.treetank.utils.IByteBuffer;
import com.treetank.utils.IConstants;

public class CachedNodePage extends NodePage {

	private final PageReader mReader;

	private final PageReference<NodePage> mReference;

	private final Map<Integer, AbstractNode> refs;

	public CachedNodePage(final PageReference<NodePage> page,
			final long nodePageKey, final PageReader reader) {
		super(nodePageKey);
		mReference = page;
		page.setPage(null);
		mReader = reader;
		refs = new FastWeakHashMap<Integer, AbstractNode>();
	}

	/**
	 * Get node at a given offset.
	 * 
	 * @param offset
	 *            Offset of node within local node page.
	 * @return Node at given offset.
	 */
	public final AbstractNode getNode(final int offset) {
		final AbstractNode node = getWeakNode(offset);
		super.setNode(offset, node);
		refs.remove(offset);
		return node;
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
		super.setNode(offset, node);
		refs.put(offset, node);
	}

	/**
	 * Get node at a given offset but do not cache it
	 * 
	 * @param offset
	 *            Offset of node within local node page.
	 * @return Node at given offset.
	 */
	public final AbstractNode getWeakNode(final int offset) {
		if (super.getNode(offset) != null) {
			return super.getNode(offset);
		} else {
			AbstractNode node = refs.get(offset);
			if (node != null) {
				return node;
			} else {
				final NodePage page = dereferenceWeakRef();
				node = page.getNode(offset);
				if (node == null) {
					throw new IllegalStateException("should never happen!");
				}
				refs.put(offset, node);
				return node;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IByteBuffer out) {

		long[] values1 = new long[getReferences().length];
		for (int i = 0; i < getReferences().length; i++) {
			if (getReferences()[i] != null) {
				values1[i] = (byte) 1;
			} else {
				values1[i] = (byte) 0;
			}
		}
		out.putAll(values1);
		for (final PageReference<? extends AbstractPage> reference : getReferences()) {
			if (reference != null) {
				reference.serialize(out);
			}
		}

		final NodePage page = dereferenceWeakRef();
		final AbstractNode[] tempNodes = new AbstractNode[getNodes().length];
		for (int i = 0; i < getNodes().length; i++) {
			if (getNodes()[i] != null) {
				tempNodes[i] = getNodes()[i];
			} else {
				tempNodes[i] = page.getNode(i);
			}
		}

		long[] values2 = new long[tempNodes.length];
		for (int i = 0; i < tempNodes.length; i++) {
			if (tempNodes[i] != null) {
				values2[i] = tempNodes[i].getKind();
			} else {
				values2[i] = IConstants.UNKNOWN;
			}
		}
		out.putAll(values2);

		for (final AbstractNode node : tempNodes) {
			if (node != null) {
				node.serialize(out);
			}
		}
	}

	private final NodePage dereferenceWeakRef() {
		final IByteBuffer in = mReader.read(mReference);
		return new NodePage(in, getNodePageKey());
	}

}
