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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.util.Configuration;
import com.treetank.util.FragmentReference;
import com.treetank.util.RevisionReference;

public class CoreTest {

  private static final String TANK = "target/CoreTest";

  @Before
  public void setUp() {
    final Core tank = new Core(TANK, 1);
    tank.erase();
  }

  @Test
  public void testCreateLoad() {
    final Core tank = new Core(TANK, 1);
    final Configuration configuration = new Configuration();
    tank.create(configuration);

    final Configuration newConfiguration = tank.load();

    Assert.assertEquals(configuration.getMaxRevision(), newConfiguration
        .getMaxRevision());
  }

  @Test
  public void testWriteRead() {
    final Core tank = new Core(TANK, 1);

    tank.create();

    final byte[] fragment = new byte[666];
    fragment[66] = 66;

    final FragmentReference fragmentReference = tank.writeFragment(fragment);
    tank.writeRevision(fragmentReference);

    final RevisionReference newRevisionReference = tank.readRevision(1, 1);
    Assert.assertEquals(fragmentReference.getOffset(), newRevisionReference
        .getOffset());
    Assert.assertEquals(fragmentReference.getLength(), newRevisionReference
        .getLength());

    final byte[] newFragment =
        tank.readFragment(1, new FragmentReference(newRevisionReference
            .getOffset(), newRevisionReference.getLength()));
    Assert.assertArrayEquals(fragment, newFragment);
  }

}
