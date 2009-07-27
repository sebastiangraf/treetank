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
 * $Id:RevisionReference.java 4237 2008-07-03 12:49:26Z kramis $
 */

package com.treetank.shared;

public final class RevisionReference {

	private long mOffset;

	private int mLength;

	private long mRevision;

	public RevisionReference() {
		this(0, 0, 0);
	}

	public RevisionReference(final long offset, final int length,
			final long revision) {
		mOffset = offset;
		mLength = length;
		mRevision = revision;
	}

	public final void setOffset(final long offset) {
		mOffset = offset;
	}

	public final void setLength(final int length) {
		mLength = length;
	}

	public final void setRevision(final long revision) {
		mRevision = revision;
	}

	public final long getOffset() {
		return mOffset;
	}

	public final int getLength() {
		return mLength;
	}

	public final long getRevision() {
		return mRevision;
	}

	public final void serialise(final ByteArrayWriter writer) {
		writer.writeLong(mOffset);
		writer.writeInt(mLength);
		writer.writeLong(mRevision);
		writer.writeByteArray(new byte[44]);
	}

	public final void deserialise(final ByteArrayReader reader) {
		mOffset = reader.readLong();
		mLength = reader.readInt();
		mRevision = reader.readLong();
	}

	public final String toString() {
		return "RevisionReference(" + mOffset + ", " + mLength + ", "
				+ mRevision + ")";
	}

}
