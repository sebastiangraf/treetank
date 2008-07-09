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
 * $Id$
 */

package com.treetank.shared;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IPageReference;

public class PageReference implements IPageReference {

  private int mIndex;

  private long mRevision;

  private List<FragmentReference> mFragmentReferenceList;
  
  private FragmentReference mDirtyFragment;

  public PageReference() {
    this(0, 0);
  }

  public PageReference(final int index, final long revision) {
    mIndex = index;
    mRevision = revision;
    mFragmentReferenceList = new ArrayList<FragmentReference>();
    mDirtyFragment = null;
  }

  public final void setIndex(final int index) {
    mIndex = index;
  }

  public final void setRevsion(final long revision) {
    mRevision = revision;
  }

  public final int getIndex() {
    return mIndex;
  }

  public final long getRevision() {
    return mRevision;
  }

  public final int getFragmentReferenceCount() {
    return mFragmentReferenceList.size();
  }

  public final FragmentReference getFragmentReference(final int index) {
    return mFragmentReferenceList.get(index);
  }

  public void serialise(final ByteArrayWriter writer) {
    writer.writeVarInt(mIndex);
    writer.writeVarLong(mRevision);
    writer.writeVarInt(mFragmentReferenceList.size());
    for (final FragmentReference fragmentReference : mFragmentReferenceList) {
      fragmentReference.serialise(writer);
    }
  }

  public void deserialise(final ByteArrayReader reader) {
    mIndex = reader.readVarInt();
    mRevision = reader.readVarLong();
    FragmentReference fragmentReference = null;
    for (int i = 0, l = reader.readVarInt(); i < l; i++) {
      fragmentReference = new FragmentReference();
      fragmentReference.deserialise(reader);
      mFragmentReferenceList.add(fragmentReference);
    }
  }

}
