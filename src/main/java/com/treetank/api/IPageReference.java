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
 * $Id: IPageReference.java 4252 2008-07-09 08:33:19Z kramis $
 */

package com.treetank.api;

import com.treetank.shared.ByteArrayReader;
import com.treetank.shared.ByteArrayWriter;
import com.treetank.shared.FragmentReference;

public interface IPageReference {

	public int getIndex();

	public long getRevision();

	public int getFragmentReferenceCount();

	public FragmentReference getFragmentReference(int index);

	public void serialise(final ByteArrayWriter writer);

	public void deserialise(final ByteArrayReader reader);

}
