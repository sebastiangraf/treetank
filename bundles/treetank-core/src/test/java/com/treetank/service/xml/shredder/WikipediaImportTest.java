/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.service.xml.shredder;

import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.exception.TTException;
import com.treetank.service.xml.serialize.XMLSerializer;

/**
 * Test WikipediaImport.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class WikipediaImportTest extends TestCase {

    public static final String WIKIPEDIA = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testWikipedia.xml";

    public static final String EXPECTED = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testWikipediaExpected.xml";

    @Override
    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @Override
    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

    public void testWikipediaImport() throws Exception {
        WikipediaImport.main(WIKIPEDIA, PATHS.PATH2.getFile().getAbsolutePath());
        XMLSerializer.main(PATHS.PATH2.getFile().getAbsolutePath(), PATHS.PATH1.getFile().getAbsolutePath());

        final StringBuilder actual = TestHelper.readFile(PATHS.PATH1.getFile().getAbsoluteFile(), false);
        final StringBuilder expected = TestHelper.readFile(new File(EXPECTED), false);
        assertEquals("XML files match", expected.toString(), actual.toString());
    }
}
