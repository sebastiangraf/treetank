package com.treetank.access;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;

public final class RevertTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void test() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IWriteTransaction wtx = session.beginWriteTransaction();
        assertEquals(0L, wtx.getRevisionNumber());
        DocumentCreater.create(wtx);
        assertEquals(0L, wtx.getRevisionNumber());
        wtx.commit();
        assertEquals(1L, wtx.getRevisionNumber());
        wtx.close();

        wtx = session.beginWriteTransaction();
        assertEquals(1L, wtx.getRevisionNumber());
        wtx.moveToFirstChild();
        wtx.insertElementAsFirstChild(new QName("bla"));
        wtx.commit();
        assertEquals(2L, wtx.getRevisionNumber());
        wtx.close();

        wtx = session.beginWriteTransaction();
        assertEquals(2L, wtx.getRevisionNumber());
        wtx.revertTo(0);
        wtx.commit();
        assertEquals(3L, wtx.getRevisionNumber());
        wtx.close();

        wtx = session.beginWriteTransaction();
        assertEquals(3L, wtx.getRevisionNumber());
        wtx.close();

        session.close();
        database.close();
    }
}
