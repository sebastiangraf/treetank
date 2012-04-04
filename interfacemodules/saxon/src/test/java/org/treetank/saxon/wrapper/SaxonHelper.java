/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import org.junit.Test;
import org.treetank.TestHelper;
import org.treetank.access.Database;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.INodeWriteTransaction;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

/**
 * Helper class for saxon
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class SaxonHelper {

    /** Path to books file. */
    private static final File BOOKSXML = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).append("data").append(
        File.separator).append("my-books.xml").toString());

    public static void createBookDB() throws Exception {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();

        final DatabaseConfiguration dbConfig = new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile());
        Database.createDatabase(dbConfig);
        final IDatabase database = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
        database.createResource(new ResourceConfiguration.Builder(TestHelper.RESOURCE, dbConfig).build());
        final ISession session =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        final INodeWriteTransaction wtx = session.beginNodeWriteTransaction();
        final XMLEventReader reader = XMLShredder.createFileReader(BOOKSXML);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void fakeTest() {
    }

}
