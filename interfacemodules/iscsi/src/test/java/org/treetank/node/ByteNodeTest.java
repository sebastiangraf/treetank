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
package org.treetank.node;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.DataInput;
import java.io.IOException;

import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.api.INode;
import org.treetank.exception.TTIOException;
import org.treetank.jscsi.TreetankStorageModule;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Test case for Byte Nodes
 * 
 * @author Andreas Rain, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
public class ByteNodeTest {

    /**
     * Test method for {@link org.treetank.node.ByteNode#serialize(java.io.DataOutput)}.
     * 
     * @throws IOException
     * @throws TTIOException
     */
    @Test
    public void testSerializeAndDeserialize() throws IOException, TTIOException {

        // testing full writes
        byte[] bytes = new byte[TreetankStorageModule.BYTES_IN_NODE];
        CoreTestHelper.random.nextBytes(bytes);

        final ByteNode byteNode = new ByteNode(CoreTestHelper.random.nextLong(), bytes);
        byteNode.setIndex(CoreTestHelper.random.nextLong());
        byteNode.setNextNodeKey(CoreTestHelper.random.nextLong());
        byteNode.setPreviousNodeKey(CoreTestHelper.random.nextLong());

        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        byteNode.serialize(out);

        final ByteNodeFactory factory = new ByteNodeFactory();
        final ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        final INode node = factory.deserializeNode((DataInput)in);
        assertTrue(node instanceof ByteNode);

        final ByteNode newNode = (ByteNode)node;

        assertEquals(newNode.getNodeKey(), byteNode.getNodeKey());
        assertEquals(newNode.getPreviousNodeKey(), byteNode.getPreviousNodeKey());
        assertEquals(newNode.getNextNodeKey(), byteNode.getNextNodeKey());
        assertEquals(newNode.getIndex(), byteNode.getIndex());
        assertEquals(newNode.getVal(), byteNode.getVal());

    }

}
