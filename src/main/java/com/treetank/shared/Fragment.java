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
 * $Id:Fragment.java 4237 2008-07-03 12:49:26Z kramis $
 */

package com.treetank.shared;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.INode;
import com.treetank.api.IPageReference;

public final class Fragment {

  private final List<IPageReference> mPageReferenceList;

  private final List<INode> mNodeList;

  public Fragment() {
    mPageReferenceList = new ArrayList<IPageReference>();
    mNodeList = new ArrayList<INode>();
  }

  public final void addPageReference(final IPageReference pageReference) {
    mPageReferenceList.add(pageReference);
  }

  public final void addNode(final INode node) {
    mNodeList.add(node);
  }

  public final IPageReference getPageReference(final int index) {
    for (final IPageReference pageReference : mPageReferenceList) {
      if (pageReference.getIndex() == index) {
        return pageReference;
      }
    }
    return null;
  }

  public final INode getNode(final int index) {
    for (final INode node : mNodeList) {
      if (node.getIndex() == index) {
        return node;
      }
    }
    return null;
  }

  public final int getPageReferenceCount() {
    return mPageReferenceList.size();
  }

  public final int getNodeCount() {
    return mNodeList.size();
  }

  public final void serialise(final ByteArrayWriter writer) {
    writer.writeVarInt(mPageReferenceList.size());
    for (final IPageReference pageReference : mPageReferenceList) {
      pageReference.serialise(writer);
    }

    writer.writeVarInt(mNodeList.size());
    for (final INode node : mNodeList) {
      writer.writeVarInt(node.getType());
      node.serialise(writer);
    }
  }

  public final void deserialise(final ByteArrayReader reader) {
    IPageReference pageReference = null;
    for (int i = 0, l = reader.readVarInt(); i < l; i++) {
      pageReference = new PageReference();
      pageReference.deserialise(reader);
      mPageReferenceList.add(pageReference);
    }

    INode node = null;
    for (int i = 0, l = reader.readVarInt(); i < l; i++) {
      switch (reader.readVarInt()) {
      case DeletedNode.TYPE:
        node = new DeletedNode();
        break;
      case RootNode.TYPE:
        node = new RootNode();
        break;
      default:
        throw new IllegalStateException("Unknown node type encountered.");
      }
      node.deserialise(reader);
      mNodeList.add(node);
    }
  }

  public final String toString() {
    return "Fragment()";
  }

}
