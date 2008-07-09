/*
 * Copyright (c) 2008, Johannes Lichtenberger (HiWi), University of Konstanz
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

package org.treetank.xmllayer;

import static org.junit.Assert.fail;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NamespaceContextImplTest {
  private NamespaceContext nspContext;

  @Before
  public void setUp() {
    nspContext = new NamespaceContextImpl();
  }

  @Test
  public void getNamespaceURITest() {
    // Case 1 and 3.
    ((NamespaceContextImpl) nspContext).setNamespace(
        "testPrefix",
        "testNamespaceURI");
    Assert.assertEquals("testNamespaceURI", nspContext
        .getNamespaceURI("testPrefix"));

    // Case 2.
    ((NamespaceContextImpl) nspContext).setNamespace("", "test");
    Assert.assertEquals("test", nspContext.getNamespaceURI(""));

    // Case 4.
    Assert.assertEquals("", nspContext.getNamespaceURI("unbound"));

    // Case 5.
    Assert.assertEquals("http://www.w3.org/XML/1998/namespace", nspContext
        .getNamespaceURI("xml"));

    // Case 6.
    Assert.assertEquals("http://www.w3.org/2000/xmlns/", nspContext
        .getNamespaceURI("xmlns"));

    // Case 7.
    try {
      nspContext.getNamespaceURI(null);
      fail("Should rise an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void getPrefixTest() {
    // Case 1 and 3.
    ((NamespaceContextImpl) nspContext).setNamespace(
        "testPrefix",
        "testNamespaceURI");
    Assert.assertEquals("testPrefix", nspContext.getPrefix("testNamespaceURI"));

    // Case 2.
    ((NamespaceContextImpl) nspContext).setNamespace("", "test");
    Assert.assertEquals("", nspContext.getPrefix("test"));

    // Case 4.
    Assert.assertEquals(null, nspContext.getPrefix("unboundNamespaceURI"));

    // Case 5.
    Assert.assertEquals("xml", nspContext
        .getPrefix("http://www.w3.org/XML/1998/namespace"));

    // Case 6.
    Assert.assertEquals("xmlns", nspContext
        .getPrefix("http://www.w3.org/2000/xmlns/"));

    // Case 7.
    try {
      nspContext.getPrefix(null);
      fail("Should rise an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void getPrefixes() {
    // Case 1, 2 and 3.
    ((NamespaceContextImpl) nspContext).setNamespace(
        "testNamespace1",
        "testNamespaceURI");
    ((NamespaceContextImpl) nspContext).setNamespace(
        "testNamespace2",
        "testNamespaceURI");
    ((NamespaceContextImpl) nspContext).setNamespace(
        "testNamespace3",
        "testNamespaceURI");

    Iterator<String> iter = nspContext.getPrefixes("testNamespaceURI");

    Assert.assertEquals("testNamespace2", iter.next());
    Assert.assertEquals("testNamespace1", iter.next());
    Assert.assertEquals("testNamespace3", iter.next());

    // Case 4.
    Assert.assertEquals("xml", nspContext.getPrefixes(
        "http://www.w3.org/XML/1998/namespace").next());

    // Case 5.
    Assert.assertEquals("xmlns", nspContext.getPrefixes(
        "http://www.w3.org/2000/xmlns/").next());

    // Case 6.
    try {
      nspContext.getPrefixes(null);
      fail("Should rise an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }
}
