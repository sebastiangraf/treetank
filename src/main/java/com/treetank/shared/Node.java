/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: Node.java 4252 2008-07-09 08:33:19Z kramis $
 */

package com.treetank.shared;

import com.treetank.api.INode;

public abstract class Node implements INode {

	private int mIndex;

	private long mRevision;

	public Node() {
		this(0, 0);
	}

	public Node(final int index, final long revision) {
		mIndex = index;
		mRevision = revision;
	}

	public final void setIndex(final int index) {
		mIndex = index;
	}

	public final void setRevsion(final long revision) {
		mRevision = revision;
	}

	public final int getIndex() {
		return mIndex;
	}

	public final long getRevision() {
		return mRevision;
	}

	public void serialise(final ByteArrayWriter writer) {
		writer.writeVarInt(mIndex);
		writer.writeVarLong(mRevision);
	}

	public void deserialise(final ByteArrayReader reader) {
		mIndex = reader.readVarInt();
		mRevision = reader.readVarLong();
	}

	public abstract int getType();

}
