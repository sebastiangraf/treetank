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
 * $Id: Device.java 4241 2008-07-03 14:43:08Z kramis $
 */

package com.treetank.device;

import java.io.RandomAccessFile;

import com.treetank.api.IDevice;
import com.treetank.shared.ByteArrayWriter;

public final class Device implements IDevice {

	private final RandomAccessFile mFile;

	public Device(final String device, final String mode) {
		try {
			mFile = new RandomAccessFile(device, mode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final byte[] read(final long offset, final int length) {
		try {
			final byte[] buffer = new byte[length];
			mFile.seek(offset);
			mFile.readFully(buffer);
			return buffer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final void write(final long offset, final byte[] buffer) {
		try {
			mFile.seek(offset);
			mFile.write(buffer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final void write(final long offset, final ByteArrayWriter writer) {
		try {
			mFile.seek(offset);
			mFile.write(writer.getBytes(), 0, writer.size());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final long size() {
		long size = 0L;
		try {
			size = mFile.length();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return size;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			mFile.close();
		} finally {
			super.finalize();
		}
	}

}
