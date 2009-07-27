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
 * $Id: DeletedNode.java 4252 2008-07-09 08:33:19Z kramis $
 */

package com.treetank.shared;

import com.treetank.api.INode;

public final class DeletedNode extends Node implements INode {

	public static final int TYPE = 0;

	public DeletedNode() {
		this(0, 0);
	}

	public DeletedNode(final int index, final long revision) {
		super(index, revision);
	}

	public final void serialise(final ByteArrayWriter writer) {
		super.serialise(writer);
	}

	public final void deserialise(final ByteArrayReader reader) {
		super.deserialise(reader);
	}

	public final int getType() {
		return TYPE;
	}

	public final String toString() {
		return "DeletedNode(" + getIndex() + ", " + getRevision() + ")";
	}

}
