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

public final class Fragment {

  private final List<INode> mNodeList;

  public Fragment() {
    mNodeList = new ArrayList<INode>();
  }

  public final void addNode(final INode node) {
    mNodeList.add(node);
  }

  public final INode getNode(final int index) {
    return mNodeList.get(index);
  }

  public final int getNodeCount() {
    return mNodeList.size();
  }

  public final void serialise(final ByteArrayWriter writer) {
    writer.writeVarInt(mNodeList.size());
    for (final INode node : mNodeList) {
      node.serialise(writer);
    }
  }

  public final void deserialise(final ByteArrayReader reader) {
    INode node = null;
    for (int i = 0, l = reader.readVarInt(); i < l; i++) {
      node = new Node();
      node.deserialise(reader);
      mNodeList.add(node);
    }
  }

  public final String toString() {
    return "Fragment()";
  }

}
