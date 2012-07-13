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

package org.treetank.io.berkeley;

import org.treetank.exception.TTByteHandleException;
import org.treetank.io.decorators.ByteRepresentation;
import org.treetank.io.decorators.IByteRepresentation;
import org.treetank.io.decorators.ZipperDecorator;
import org.treetank.page.IPage;
import org.treetank.page.PageFactory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Binding for storing {@link IPage} objects within the Berkeley DB.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class PageBinding extends TupleBinding<IPage> {

	/** Factory for Pages. */
	private final PageFactory mFac = PageFactory.getInstance();

	final IByteRepresentation mByteHandler = new ZipperDecorator(
			new ByteRepresentation());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage entryToObject(final TupleInput arg0) {
		final ByteArrayDataOutput data = ByteStreams.newDataOutput();
		int result = arg0.read();
		while (result != -1) {
			byte b = (byte) result;
			data.write(b);
			result = arg0.read();
		}
		byte[] resultBytes;
		try {
			resultBytes = mByteHandler.deserialize(data.toByteArray());
			return mFac.deserializePage(resultBytes);
		} catch (TTByteHandleException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectToEntry(final IPage arg0, final TupleOutput arg1) {
		final byte[] pagebytes = arg0.getByteRepresentation();
		arg1.write(mByteHandler.serialize(pagebytes));
	}

}
