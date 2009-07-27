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
 * $Id:Configuration.java 4237 2008-07-03 12:49:26Z kramis $
 */

package com.treetank.shared;

public final class Configuration {

	private long mMaxRevision;

	public Configuration() {
		this(0);
	}

	public Configuration(final long maxRevision) {
		mMaxRevision = maxRevision;
	}

	public final void incrementMaxRevision() {
		mMaxRevision += 1;
	}

	public final long getMaxRevision() {
		return mMaxRevision;
	}

	public final void serialise(final ByteArrayWriter writer) {
		writer.writeByteArray(new byte[448]);
	}

	public final void deserialise(final ByteArrayReader reader) {
	}

	public final String toString() {
		return "Configuration(" + mMaxRevision + ")";
	}

}
