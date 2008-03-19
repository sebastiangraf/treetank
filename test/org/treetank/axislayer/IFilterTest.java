/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.axislayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;
import org.treetank.sessionlayer.Session;

public class IFilterTest {

  public static final String PATH =
      "generated" + File.separator + "IFilterTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  public static void testIFilterConventions(
      final IFilter filter,
      final boolean expected) {

    final IReadTransaction rtx = filter.getTransaction();

    // IFilter Convention 1.
    final long startKey = rtx.getNodeKey();

    assertEquals(expected, filter.filter());

    // IAxis Convention 2.
    assertEquals(startKey, rtx.getNodeKey());

  }

  @Test
  public void testIFilterExample() {
    // Do nothing. This class is only used with other test cases.
  }

}
