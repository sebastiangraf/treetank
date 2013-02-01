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

package org.treetank.cache;

import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.page.PageFactory;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class NodePageContainerBinding extends TupleBinding<LogContainer<IPage>> {

    private final PageFactory mFac;

    /**
     * Constructor
     * 
     * @param pNodeFac
     *            for the deserialization of nodes
     * @param pMetaFac
     *            for the deserialization of meta-entries
     */
    public NodePageContainerBinding(final INodeFactory pNodeFac, final IMetaEntryFactory pMetaFac) {
        mFac = new PageFactory(pNodeFac, pMetaFac);
    }

    @Override
    public LogContainer<IPage> entryToObject(final TupleInput arg0) {
        final ByteArrayDataInput data = ByteStreams.newDataInput(arg0.getBufferBytes());

        final int completeLength = data.readInt();
        final int modifiedLength = data.readInt();
        byte[] completeBytes = new byte[completeLength];
        byte[] modifiedBytes = new byte[modifiedLength];
        data.readFully(completeBytes);
        data.readFully(modifiedBytes);

        final IPage current = mFac.deserializePage(completeBytes);
        final IPage modified = mFac.deserializePage(modifiedBytes);
        return new LogContainer<IPage>(current, modified);
    }

    @Override
    public void objectToEntry(final LogContainer<IPage> arg0, final TupleOutput arg1) {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        final byte[] completeData = arg0.getComplete().getByteRepresentation();
        final byte[] modifiedData = arg0.getModified().getByteRepresentation();
        pOutput.writeInt(completeData.length);
        pOutput.writeInt(modifiedData.length);
        pOutput.write(arg0.getComplete().getByteRepresentation());
        pOutput.write(arg0.getModified().getByteRepresentation());
        arg1.write(pOutput.toByteArray());
    }
}
