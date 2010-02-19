package com.treetank.access;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore
    public void test() throws TreetankException {
        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        TestCase.assertEquals(0L, wtx.getRevisionNumber());
        TestCase.assertEquals(1L, wtx.getNodeCount());

        DocumentCreater.create(wtx);
        wtx.commit();
        TestCase.assertEquals(1L, wtx.getRevisionNumber());
        TestCase.assertEquals(14L, wtx.getNodeCount());

        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        wtx.remove();
        wtx.commit();

        wtx.moveToDocumentRoot();
        TestCase.assertEquals(2L, wtx.getRevisionNumber());
        TestCase.assertEquals(1L, wtx.getNodeCount());

        wtx.revertTo(0);
        TestCase.assertEquals(1L, wtx.getRevisionNumber());
        TestCase.assertEquals(1L, wtx.getNodeCount());
        wtx.abort();
        TestCase.assertEquals(1L, wtx.getRevisionNumber());
        TestCase.assertEquals(14L, wtx.getNodeCount());

        TestCase.assertEquals(1L, wtx.getRevisionNumber());
        TestCase.assertEquals(1L, wtx.getNodeCount());
        wtx.close();

        session.close();
        database.close();
    }

}
