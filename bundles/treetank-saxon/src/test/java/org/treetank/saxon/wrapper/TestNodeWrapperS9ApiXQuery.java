/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.saxon.wrapper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.treetank.saxon.evaluator.XQueryEvaluator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test XQuery S9Api.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TestNodeWrapperS9ApiXQuery {

    /** Treetank database on books document. */
    private static transient IDatabase databaseBooks;

    /** Path to books file. */
    private static final File BOOKSXML = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).append("data").append(
        File.separator).append("my-books.xml").toString());

    @Before
    public void setUp() throws Exception {
        Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
        Database.createDatabase(new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile()));

        databaseBooks = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction mWTX = databaseBooks.getSession().beginWriteTransaction();
        final XMLEventReader reader = XMLShredder.createReader(BOOKSXML);
        final XMLShredder shredder = new XMLShredder(mWTX, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        mWTX.close();
    }

    @AfterClass
    public static void tearDown() throws AbsTTException {
        databaseBooks.close();
        Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
    }

    @Test
    public void testWhereBooks() throws Exception {
        final XdmValue value =
            new XQueryEvaluator("for $x in /bookstore/book where $x/price>30 return $x/title", databaseBooks)
                .call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
            strBuilder.toString());
    }

    @Test
    public void testOrderByBooks() throws Exception {
        final XdmValue value =
            new XQueryEvaluator(
                "for $x in /bookstore/book where $x/price>30 order by $x/title return $x/title",
                databaseBooks).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            strBuilder.toString());
    }

    @Test
    public void testFLOWR() throws Exception {
        final XdmValue value =
            new XQueryEvaluator(
                "for $x in /bookstore/book let $y := $x/price where $y>30 order by $x/title return $x/title",
                databaseBooks).call();
        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            strBuilder.toString());
    }

}
