/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.nodelayer.ElementNode;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastByteArrayReader;

public class PageWriterTest {

  private final static String PATH =
      "generated" + File.separator + "PageWriterTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(PATH).delete();
  }

  @Test
  public void testWriteRead() throws IOException {

    // Prepare file with version info.
    final RandomAccessFile file = new RandomAccessFile(PATH, "rw");
    file.seek(0L);
    file.writeInt(IConstants.VERSION_MAJOR);
    file.writeInt(IConstants.VERSION_MINOR);
    file.writeBoolean(false);
    file.writeBoolean(false);
    file.close();

    // Create node page with single node.
    final NodePage page1 = new NodePage(13L);
    page1.setNode(3, new ElementNode(0L, 1L, 2L, 3L, 4L, 6, 7, 8));
    final PageReference pageReference = new PageReference();

    // Serialize node page.
    final PageWriter pageWriter =
        new PageWriter(new SessionConfiguration(PATH));
    pageReference.setPage(page1);
    pageWriter.write(pageReference);
    assertEquals(IConstants.BEACON_START, pageReference.getStart());

    // Deserialize node page.
    final PageReader pageReader =
        new PageReader(new SessionConfiguration(PATH));
    final FastByteArrayReader in = pageReader.read(pageReference);
    final NodePage page2 = new NodePage(in, 0L);

  }
}
