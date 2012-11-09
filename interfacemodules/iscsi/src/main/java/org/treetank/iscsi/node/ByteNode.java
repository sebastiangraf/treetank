/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.iscsi.node;

import org.treetank.api.INode;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * This implementation of {@link INode} is used to store byte arrays in nodes.
 * 
 * @author Andreas Rain
 */
public class ByteNode implements INode {

  /**
   * The nodes key value, which is equal with it's position in the list.
   */
  long nodeKey = 0;

  /**
   * The following nodes key
   */
  long nextNodeKey = 0;

  /**
   * The previous nodes key
   */
  long previousNodeKey = -1;
  
  /**
   * The real index of this byte.
   */
  int index = 0;

  /**
   * The size of the byte array in the node. The maximum size of a byte array in
   * a {@link ByteNode} is 2^32 - 1. This is because in the deserialization the
   * first 4 bytes determine the size of each node.
   */
  private int size = 0;

  /**
   * The content of this node in form of a byte array.
   */
  private byte[] val;

  /**
   * Standard constructor with a size of 512 bytes for each node.
   */
  public ByteNode(long nodeKey) {
    this.nodeKey = nodeKey;
    
    size = 512;
    val = new byte[512];
  }

  /**
   * Creates a ByteNode with given bytes
   * 
   * @param content
   *          , as byte array
   */
  public ByteNode(long nodeKey, byte[] content) {
    this.nodeKey = nodeKey;
    
    size = content.length;
    val = content;
  }

  @Override
  public byte[] getByteRepresentation() {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeInt(size);
    output.writeLong(nodeKey);
    output.writeLong(previousNodeKey);
    output.writeLong(nextNodeKey);
    output.write(val);

    return output.toByteArray();
  }

  @Override
  public long getNodeKey() {

    return this.nodeKey;
  }

  @Override
  public void setHash(long pHash) {

  }

  @Override
  public long getHash() {

    return this.nodeKey*previousNodeKey*nextNodeKey*31;
  }

  public byte[] getVal() {

    return val;
  }

  public void setVal(byte[] val) {

    this.val = val;
  }

  public long getNextNodeKey() {

    return nextNodeKey;
  }

  public boolean hasNext(){
    return (this.nextNodeKey != 0);
  }
  
  public void setNextNodeKey(long nextNodeKey) {

    this.nextNodeKey = nextNodeKey;
  }

  public long getPreviousNodeKey() {

    return previousNodeKey;
  }

  public void setPreviousNodeKey(long previousNodeKey) {

    this.previousNodeKey = previousNodeKey;
  }

  public boolean hasPrevious(){
    return (this.previousNodeKey != -1);
  }
  
  public int getIndex() {
  
    return index;
  }
  
  public void setIndex(int index) {
  
    this.index = index;
  }
  
  public int incIndex(){
    this.index++;
    return this.index;
  }
  
  public int decIndex(){
    this.index--;
    return this.index;
  }

}
