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

import static org.testng.Assert.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

import org.jscsi.target.storage.IStorageModule;
import org.testng.annotations.Test;
import org.treetank.api.INode;
import org.treetank.exception.TTIOException;
import org.treetank.jscsi.TreetankStorageModule;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Andreas Rain
 *
 */
public class ByteNodeTest {

    /**
     * Test method for {@link org.treetank.node.ByteNode#serialize(java.io.DataOutput)}.
     * @throws IOException 
     * @throws TTIOException 
     */
    @Test(enabled=false)
    public void testSerializeAndDeserialize() throws IOException, TTIOException {
        Random rand = new Random(42);
        
        // testing full writes
        byte[] bytes = new byte[TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE];
        rand.nextBytes(bytes);
        
        ByteNode byteNode = new ByteNode(0, bytes);
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        byteNode.setIndex(5);
        byteNode.setNextNodeKey(1);
        byteNode.setPreviousNodeKey(-1);
        
        byteNode.serialize((DataOutput) out);
        
        ByteNodeFactory factory = new ByteNodeFactory();
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        INode node = factory.deserializeNode((DataInput) in);
        
        assertTrue(node instanceof ByteNode);
        assertEquals(node.getNodeKey(), byteNode.getNodeKey());
        assertEquals(((ByteNode) node).getPreviousNodeKey(), byteNode.getPreviousNodeKey());
        assertEquals(((ByteNode) node).getNextNodeKey(), byteNode.getNextNodeKey());
        assertEquals(((ByteNode) node).getIndex(), byteNode.getIndex());
        assertEquals(((ByteNode) node).getVal(), byteNode.getVal());
        
    }

}
