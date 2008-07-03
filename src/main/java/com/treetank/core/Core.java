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

package com.treetank.core;

import java.io.File;

import com.treetank.api.Configuration;
import com.treetank.api.FragmentReference;
import com.treetank.api.ICore;
import com.treetank.api.IFragmentReadCore;
import com.treetank.api.IFragmentWriteCore;
import com.treetank.api.IHeaderCreateCore;
import com.treetank.api.IHeaderLoadCore;
import com.treetank.api.IRevisionReadCore;
import com.treetank.api.IRevisionWriteCore;
import com.treetank.api.RevisionReference;

public final class Core implements ICore {

  private final String mDevice;

  private final int mReadCoreCount;

  private final IRevisionWriteCore mRevisionWriteCore;

  private final IFragmentWriteCore mFragmentWriteCore;

  private final IRevisionReadCore[] mRevisionReadCore;

  private final IFragmentReadCore[] mFragmentReadCore;

  private Configuration mConfiguration;

  public Core(final String device, final int readCoreCount) {
    mDevice = device;
    mReadCoreCount = readCoreCount;

    mRevisionWriteCore = new RevisionWriteCore(device);
    mFragmentWriteCore = new FragmentWriteCore(device);

    mRevisionReadCore = new IRevisionReadCore[readCoreCount];
    mFragmentReadCore = new IFragmentReadCore[readCoreCount];
    for (int i = 0; i < readCoreCount; i++) {
      mRevisionReadCore[i] = new RevisionReadCore(device);
      mFragmentReadCore[i] = new FragmentReadCore(device);
    }
    mConfiguration = null;
  }

  public final void create() {
    mConfiguration = new Configuration();
    final IHeaderCreateCore headerCreateCore = new HeaderCreateCore(mDevice);
    headerCreateCore.create(mConfiguration);
  }

  public final void create(final Configuration configuration) {
    mConfiguration = configuration;
    final IHeaderCreateCore headerCreateCore = new HeaderCreateCore(mDevice);
    headerCreateCore.create(configuration);
  }

  public final Configuration load() {
    final IHeaderLoadCore headerLoadCore = new HeaderLoadCore(mDevice);
    mConfiguration = headerLoadCore.load();
    return mConfiguration;
  }

  public final void erase() {
    mConfiguration = null;
    new File(mDevice + ".tt1").delete();
    new File(mDevice + ".tt2").delete();
  }

  public final FragmentReference writeFragment(final byte[] fragment) {
    return mFragmentWriteCore.writeFragment(fragment);
  }

  public final RevisionReference writeRevision(
      final FragmentReference fragmentReference) {
    try {
      final RevisionReference revisionReference =
          mRevisionWriteCore.writeRevision(
              mConfiguration.getMaxRevision() + 1,
              fragmentReference);
      mConfiguration.incrementMaxRevision();
      return revisionReference;
    } catch (Exception e) {
      throw new RuntimeException("Could not write revision due to: "
          + e.toString());
    }
  }

  public final RevisionReference readRevision(
      final int core,
      final long revision) {
    if ((core < 1) || (core > mReadCoreCount)) {
      throw new IllegalArgumentException("Argument 'core="
          + core
          + "' must be in [1,...,"
          + (mReadCoreCount)
          + "].");
    }
    return mRevisionReadCore[core - 1].readRevision(revision);
  }

  public final byte[] readFragment(
      final int core,
      final FragmentReference fragmentReference) {
    if ((core < 1) || (core > mReadCoreCount)) {
      throw new IllegalArgumentException("Argument 'core="
          + core
          + "' must be in [1,...,"
          + (mReadCoreCount)
          + "].");
    }
    return mFragmentReadCore[core - 1].readFragment(fragmentReference);
  }

}
