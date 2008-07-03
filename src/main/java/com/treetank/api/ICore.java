/*
 * Copyright (c) 2008, Marc Kramis, University of Konstanz
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
 * $Id:license.txt 4014 2008-03-26 16:36:35Z kramis $
 */

package com.treetank.api;

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
