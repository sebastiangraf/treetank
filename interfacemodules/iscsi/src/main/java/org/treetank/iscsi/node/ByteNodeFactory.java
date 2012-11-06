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
package org.treetank.iscsi.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;

/**
 * This factory is used to deserialize {@link ByteNode}
 * @author Andreas Rain
 *
 */
public class ByteNodeFactory implements INodeFactory {

	@Override
	public INode deserializeNode(byte[] pData) {
	  ByteArrayInputStream pInputStream = new ByteArrayInputStream(pData);
	  
	  byte[] sizeAsByte = new byte[4];
	  pInputStream.read(sizeAsByte, 0, 4);
	  
	  ByteArrayInputStream sizeInputStream = new ByteArrayInputStream(sizeAsByte);
	  
	  int size = sizeInputStream.read();
	  
	  if((pData.length-4) % size == 0){
	    byte[] nodeContent = new byte[size];
	    pInputStream.read(nodeContent, 4, 4+size);
	    
	    ByteNode root = new ByteNode(nodeContent);
	    
	    for(int i = 4+size; i <= (pData.length - 4 - size); i = i + size){
	      pInputStream.read(nodeContent, i, i+size);
	      root.insertAfter(new ByteNode(nodeContent));
	    }
	    
	    return root;
	  }
	  
		return null;
	}

}
