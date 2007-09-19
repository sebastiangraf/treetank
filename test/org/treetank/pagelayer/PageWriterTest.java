/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;


public class PageWriterTest {

  private final static String PATH = "generated/PageWriterTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testWriteRead() throws Exception {

    // Create node page with single node.
    final NodePage page1 = NodePage.create(13L);
    page1.setNode(3, new Node(0L, 1L, 2L, 3L, 4L, 5, 6, 7, 8, UTF
        .convert("foo")));
    final PageReference pageReference = new PageReference();

    // Serialize node page.
    final PageWriter pageWriter = new PageWriter(PATH);
    pageReference.setPage(page1);
    pageWriter.write(pageReference);
    assertEquals(0L, pageReference.getStart());

    // Deserialize node page.
    final PageReader pageReader = new PageReader(PATH);
    final FastByteArrayReader in = pageReader.read(pageReference);
    final NodePage page2 = NodePage.read(in);

    assertEquals("foo", new String(
        page2.getNode(3).getValue(),
        IConstants.ENCODING));
  }

}
