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
 * $Id: PageReaderAndWriterTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.page;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.file.FileReader;
import com.treetank.io.file.FileWriter;
import com.treetank.node.ElementNode;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.IConstants;

public class PageReaderAndWriterTest {

	@Before
	public void setUp() {
		Session.removeSession(ITestConstants.PATH1);
		//new File(ITestConstants.PATH1).mkdirs();
	}

	@Test
	@Ignore
	public void testWriteRead() throws IOException {

		// Prepare file with version info.
		final RandomAccessFile file = new RandomAccessFile(ITestConstants.PATH1
				+ File.separator + "tt.tnk", "rw");
		file.seek(0L);
		file.writeLong(IConstants.VERSION_MAJOR);
		file.writeLong(IConstants.VERSION_MINOR);
		file.writeBoolean(false);
		file.writeBoolean(false);
		file.close();

		// Create node page with single node.
		final NodePage page1 = new NodePage(13L);
		page1.setNode(3, new ElementNode(0L, 1L, 2L, 3L, 4L, 6, 7, 0));
		final PageReference pageReference = new PageReference();

		// Serialize node page.
		final IWriter pageWriter = new FileWriter(new SessionConfiguration(
				ITestConstants.PATH1));
		pageReference.setPage(page1);
		pageWriter.write(pageReference);
		assertEquals(IConstants.BEACON_START, pageReference.getKey()
				.getIdentifier());

		// Deserialize node page.
		final IReader pageReader = new FileReader(new SessionConfiguration(
				ITestConstants.PATH1));
		final NodePage page2 = new NodePage((NodePage) pageReader
				.read(pageReference));

	}
}
