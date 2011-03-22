/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.wikipedia.hadoop;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.io.ArrayWritable;

/**
 * <h1>ListWritable</h1>
 * 
 * <p>
 * Wrapper to wrap a List of {@link XMLEventWritable}s in an {@link ArrayWritable}.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ListWritable extends ArrayWritable {

    /** {@link List} of {@link XMLEventWritable}s. */
    private List<XMLEventWritable> mList;

    /**
     * Constructor.
     * 
     * @param paramList
     *            List of {@link XMLEvent}s.
     */
    public ListWritable(final List<XMLEventWritable> paramList) {
        super(XMLEventWritable.class, paramList.toArray(new XMLEventWritable[paramList.size()]));
        mList = paramList;
    }

    /**
     * Get the underlying list of {@link XMLEvent}s.
     * 
     * @return the List.
     */
    public List<XMLEventWritable> getList() {
        return mList;
    }

    /**
     * Set the underlying list of {@link XMLEvent}s.
     * 
     * @param paramList
     *            The List of {@link XMLEventWritable}s to set.
     */
    public void setmList(final List<XMLEventWritable> paramList) {
        mList = paramList;
    }

    @Override
    public void readFields(final DataInput paramIn) throws IOException {
        readFields(paramIn);
        mList = Arrays.asList((XMLEventWritable[])get());
    }

    /**
     * Read from {@link DataInput}.
     * 
     * @param paramList
     *                  The underlying {@link List}.
     * @param paramIn
     *                  The {@link DataInput}.
     * @return a new writable list.
     * @throws IOException
     *                  In case of any I/O failure.
     */
    public static ListWritable read(final List<XMLEventWritable> paramList, final DataInput paramIn)
        throws IOException {
        final ListWritable list = new ListWritable(paramList);
        list.readFields(paramIn);
        return list;
    }
}
