package org.treetank.saxon.wrapper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import org.treetank.TestHelper;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.FileDatabase;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

import org.junit.Test;

public final class BookShredding {

    public BookShredding() {
    }

    /** Path to books file. */
    private static final File BOOKSXML = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).append("data").append(
        File.separator).append("my-books.xml").toString());

    public static void createBookDB() throws Exception {
        FileDatabase.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
        FileDatabase.createDatabase(TestHelper.PATHS.PATH1.getFile(), new DatabaseConfiguration.Builder()
            .build());
        final IDatabase database = FileDatabase.openDatabase(TestHelper.PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLEventReader reader = XMLShredder.createReader(BOOKSXML);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();
        session.close();
    }

    @Test
    public void fakeTest() {
    }

}
