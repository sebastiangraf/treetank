package com.treetank.access;

import static org.junit.Assert.assertEquals;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
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
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        TestCase.assertEquals(0L, wtx.getRevisionNumber());

        DocumentCreater.create(wtx);
        wtx.commit();
        assertEquals(1L, wtx.getRevisionNumber());
        wtx.moveToDocumentRoot();
        assertEquals(1, wtx.getNode().getChildCount());

        wtx.moveToFirstChild();
        wtx.remove();
        assertEquals(0, wtx.getNode().getChildCount());
        wtx.commit();

        wtx.moveToDocumentRoot();
        TestCase.assertEquals(2L, wtx.getRevisionNumber());
        assertEquals(0, wtx.getNode().getChildCount());
        wtx.revertTo(0);
        TestCase.assertEquals(1L, wtx.getRevisionNumber());
        assertEquals(1, wtx.getNode().getChildCount());
        wtx.commit();
        TestCase.assertEquals(2L, wtx.getRevisionNumber());
        assertEquals(1, wtx.getNode().getChildCount());
        wtx.close();

        session.close();
        database.close();
    }

}
