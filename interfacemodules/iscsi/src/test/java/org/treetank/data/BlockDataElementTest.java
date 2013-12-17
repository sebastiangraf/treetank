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
package org.treetank.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.DataInput;
import java.io.IOException;

import org.testng.annotations.Test;
import org.treetank.api.IData;
import org.treetank.exception.TTIOException;
import org.treetank.iscsi.data.BlockDataElement;
import org.treetank.iscsi.data.BlockDataElementFactory;
import org.treetank.iscsi.jscsi.TreetankStorageModule;
import org.treetank.testutil.CoreTestHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Test case for Byte Nodes
 * 
 * @author Andreas Rain, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
public class BlockDataElementTest {

    /**
     * Test method for {@link org.treetank.data.BlockDataElement#serialize(java.io.DataOutput)}.
     * 
     * @throws IOException
     * @throws TTIOException
     */
    @Test
    public void testSerializeAndDeserialize() throws IOException, TTIOException {

        // testing full writes
        byte[] bytes = new byte[TreetankStorageModule.BYTES_IN_DATA];
        CoreTestHelper.random.nextBytes(bytes);

        final BlockDataElement blockDataElement =
            new BlockDataElement(CoreTestHelper.random.nextLong(), bytes);

        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        blockDataElement.serialize(out);

        final BlockDataElementFactory factory = new BlockDataElementFactory();
        final ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        final IData data = factory.deserializeData((DataInput)in);
        assertTrue(data instanceof BlockDataElement);

        final BlockDataElement newNode = (BlockDataElement)data;

        assertEquals(newNode.getDataKey(), blockDataElement.getDataKey());
        assertEquals(newNode.getVal(), blockDataElement.getVal());

    }

}
