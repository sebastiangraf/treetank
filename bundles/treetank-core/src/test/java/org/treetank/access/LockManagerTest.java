package org.treetank.access;

import javax.xml.namespace.QName;

import junit.framework.TestCase;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.LockManager;
import org.treetank.access.SynchWriteTransaction;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class LockManagerTest {

    private static Long[] nodes;

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    public static void setUp() throws AbsTTException {
        TestHelper.deleteEverything();

        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        nodes = new Long[13];
        nodes[0] = Long.valueOf("0");

        wtx.insertElementAsFirstChild(new QName("1"));
        nodes[1] = wtx.getNode().getNodeKey();

        wtx.insertElementAsRightSibling(new QName("2"));
        nodes[2] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("3"));
        nodes[3] = wtx.getNode().getNodeKey();
        wtx.moveToLeftSibling();
        wtx.moveToLeftSibling();

        wtx.insertElementAsFirstChild(new QName("4"));
        nodes[4] = wtx.getNode().getNodeKey();

        wtx.insertElementAsRightSibling(new QName("5"));
        nodes[5] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("6"));
        nodes[6] = wtx.getNode().getNodeKey();
        wtx.moveToParent();
        wtx.moveToRightSibling();

        wtx.insertElementAsFirstChild(new QName("7"));
        nodes[7] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("8"));
        nodes[8] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("9"));
        nodes[9] = wtx.getNode().getNodeKey();
        wtx.moveToParent();
        wtx.moveToRightSibling();

        wtx.insertElementAsFirstChild(new QName("10"));
        nodes[10] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("11"));
        nodes[11] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("12"));
        nodes[12] = wtx.getNode().getNodeKey();
        wtx.commit();
        wtx.close();
    }

    @Ignore
    @Test
    /**
     * Simply locking an available subtree without interference
     */
    public void basicLockingTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        LockManager lock = LockManager.getLockManager();
        try {
            lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction)wtx);
        } catch (Exception e) {
            TestCase.fail();
        }
        wtx.close();
    }

    @Ignore
    @Test
    /**
     * Locking an available subtree with other wtx holding locks on different subtrees
     */
    public void permitLockingInFreeSubtreeTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();

        LockManager lock = LockManager.getLockManager();
        try {
            lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
            lock.getWritePermission(nodes[7], (SynchWriteTransaction)wtx2);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree blocked by a foreign transaction root node has to fail
     */
    public void denyLockingOnForeignTrnTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();

        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(wtx2.getNode().getNodeKey(), (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree which is part of a blocked subtree (has parent which is trn 
     * of a foreign transaction) has to fail
     */
    public void denyLockingUnderForeignTrnTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree which would contain a blocked subtree (has ancestor which is trn
     * of a foreign transaction) has to fail
     */
    public void denyLockingAboveForeignTrnTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }


    @Ignore
    @Test
    /**
     * Locking a subtree which would contain one or more subtrees previously locked by the same
     * transaction is permitted
     */
    public void permitLockingAboveMultipleOwnTrnTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
        lock.getWritePermission(nodes[7], (SynchWriteTransaction)wtx);
        lock.getWritePermission(nodes[10], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[0], (SynchWriteTransaction)wtx);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

    @Ignore
    @Test
    /**
     * Locking a subtree which has been blocked and afterwards released by a foreign
     * transaction is possible
     */
    public void conquerReleasedSubtreeTest() throws AbsTTException {
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx);
        lock.releaseWritePermission((SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx2);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

}
