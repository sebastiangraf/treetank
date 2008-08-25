/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: AncestorAxisTest.java 3507 2007-11-15 08:47:27Z kramis $
 */

package org.treetank.axislayer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;

public class IAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "IAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  public static void testIAxisConventions(
      final IAxis axis,
      final long[] expectedKeys) {

    final IReadTransaction rtx = axis.getTransaction();

    // IAxis Convention 1.
    final long startKey = rtx.getNodeKey();

    final long[] keys = new long[expectedKeys.length];
    int offset = 0;
    for (final long nodeKey : axis) {

      // IAxis results.
      if (offset >= expectedKeys.length) {
        fail("More nodes found than expected.");
      }
      keys[offset++] = rtx.getNodeKey();

      // IAxis Convention 2.
      try {
        axis.next();
        fail("Should only allow to call next() once.");
      } catch (Exception e) {
        // Must throw exception.
      }

      // IAxis Convention 3.
      rtx.moveToDocumentRoot();

    }

    // IAxis Convention 5.
    assertEquals(startKey, rtx.getNodeKey());

    // IAxis results.
    assertArrayEquals(expectedKeys, keys);

  }

  @Test
  public void testIAxisUserExample() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveToDocumentRoot();
    final IAxis axis = new DescendantAxis(wtx);
    long count = 0L;
    while (axis.hasNext()) {
      count += 1;
    }
    Assert.assertEquals(10L, count);

    wtx.abort();
    wtx.close();
    session.close();
  }

}
