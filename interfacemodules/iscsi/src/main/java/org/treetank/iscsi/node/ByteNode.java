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

import java.io.ByteArrayOutputStream;

import org.treetank.api.INode;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * This implementation of {@link INode} is used to abstract
 * a list of byte array segments.
 * @author Andreas Rain
 *
 */
public class ByteNode implements INode {

  /**
   * The nodes key value, which is equal with it's position in the list.
   */
  long nodeKey = 0;
  
  /**
   * The nodes hash value.
   */
  private long hash = 0;
  
  /**
   * The size of the byte array in the node.
   * The maximum size of a byte array in a {@link ByteNode} is
   * 2^32 - 1. This is because in the deserialization the first 
   * 4 bytes determine the size of each node.
   */
  private int size = 0;

  /**
   * The content of this node in form of a byte array.
   */
  private byte[] val;

  /**
   * A link to the next node in the list.
   */
  private ByteNode next = null;

  /**
   * A link to the previous node in the list.
   */
  private ByteNode previous = null;

  /**
   * Standard constructor with a size of 512 bytes for each node.
   */
  public ByteNode() {

    int size = 512;
    val = new byte[512];
    
    this.setHash(0);
  }

  /**
   * Creates a ByteNode with given bytes
   * 
   * @param content
   *          , as byte array
   */
  public ByteNode(byte[] content) {
    size = content.length;
    val = content;
  }

  /**
   * Inserts a node before this node.
   * 
   * @param node
   *          the node to be inserted before this node
   */
  public void insertBefore(ByteNode node) {
    
    node.setPrevious(this.previous);
    node.setNext(this);
    node.setNodeKey(this.nodeKey+1);
    this.previous.setNext(node);
    this.setPrevious(node);
    
    this.nodeKey++;
  }

  /**
   * Inserts a node after this node.
   * 
   * @param node
   *          the node to be inserted after this node
   */
  public void insertAfter(ByteNode node) {

    node.setPrevious(this);
    node.setNext(this.next);
    node.setNodeKey(this.nodeKey+2);
    this.next.setNodeKey(this.nodeKey+2);
    this.next.setPrevious(node);
    this.setNext(node);
  }

  /**
   * Removes this ByteNode from the list
   * @return returns this ByteNode instance
   */
  public ByteNode remove() {

    if (previous != null)
      previous.setNext(next);

    if (next != null){
      next.setPrevious(previous);
      next.setNodeKey(this.nodeKey);
    }
    
    this.previous = null;
    this.next = null;
    this.nodeKey = 0;

    return this;
  }

  @Override
  public byte[] getByteRepresentation() {
    if(previous == null){
      ByteArrayOutputStream output = new ByteArrayOutputStream(this.size+4);
      ByteArrayOutputStream sizeByte = new ByteArrayOutputStream(4);
      sizeByte.write(this.size);
      
      output.write(sizeByte.toByteArray(), 0, 4);
      output.write(this.val, 4, (this.size+4));
      
      //TODO Append the other nodes!. ~ara
    }
    
    return val;
  }

  @Override
  public void setNodeKey(long pNodeKey) {

    this.nodeKey = pNodeKey;
    this.hash = pNodeKey;
  }

  @Override
  public long getNodeKey() {

    return this.nodeKey;
  }

  @Override
  public void setHash(long pHash) {
    
    this.hash = pHash;
  }

  @Override
  public long getHash() {

    return this.hash;
  }

  public byte[] getVal() {

    return val;
  }

  public void setVal(byte[] val) {

    this.val = val;
  }

  public INode getNext() {

    return next;
  }

  public void setNext(ByteNode next) {

    this.next = next;
  }

  public INode getPrevious() {

    return previous;
  }

  public void setPrevious(ByteNode previous) {

    this.previous = previous;
  }

}
