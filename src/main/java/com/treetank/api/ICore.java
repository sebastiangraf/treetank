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

package com.treetank.api;

import com.treetank.util.Configuration;
import com.treetank.util.FragmentReference;
import com.treetank.util.RevisionReference;

public interface ICore {

  public void create();

  public void create(final Configuration configuration);

  public Configuration load();

  public void erase();

  public FragmentReference writeFragment(final byte[] fragment);

  public RevisionReference writeRevision(
      final FragmentReference fragmentReference);

  public RevisionReference readRevision(final int core, final long revision);

  public byte[] readFragment(
      final int core,
      final FragmentReference fragmentReference);

}
