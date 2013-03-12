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
package org.treetank.jscsi.buffering;

import java.util.Random;

import static org.mockito.Mockito.*;

import org.jscsi.target.storage.IStorageModule;
import org.testng.annotations.*;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.jscsi.TreetankStorageModule;
import org.treetank.node.ByteNode;

/**
 * @author Andreas Rain
 *
 */
public class BufferedWriteTaskTest {
    
    /**
     * Mocked transaction class.
     */
    private final static IIscsiWriteTrx mRtx = mock(IIscsiWriteTrx.class);

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUp() throws Exception {
        
        for(int i = 0; i < 128; i++){
            when(mRtx.moveTo(i)).thenReturn(true);
        }

        when(mRtx.getCurrentNode()).thenReturn(new ByteNode(0, new byte[TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE]));
    }

    /**
     * Testing a call on a buffered write task.
     * @throws Exception 
     */
    @Test
    public void testCall() throws Exception {
        Random rand = new Random(42);
        
        // testing full writes
        byte[] bytes = new byte[127 * TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE];
        
        rand.nextBytes(bytes);
        
        BufferedWriteTask task = new BufferedWriteTask(bytes, 0, mRtx);
        task.call();
        
        // Testing an overlap with offsets
        bytes = new byte[500000];
        rand.nextBytes(bytes);
        
        task = new BufferedWriteTask(bytes, 2, mRtx);
        task.call();
        
        // Testing a during relation
        bytes = new byte[20000];
        rand.nextBytes(bytes);
        
        task = new BufferedWriteTask(bytes, 20000, mRtx);
        task.call();
    }

}
