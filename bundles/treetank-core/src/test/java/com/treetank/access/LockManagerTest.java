package com.treetank.access;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;


public class LockManagerTest {

	private static Long[] nodes;



            @After
            public void tearDown() throws TreetankException {
                TestHelper.closeEverything();
            }
	
            public static void setUp() throws TreetankException {
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

	
	
            @Ignore @Test
	public void basicLockingTest() throws TreetankException{
	    IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            
		LockManager lock = LockManager.getLockManager();
		try {
			lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction) wtx);
		} catch (Exception e) {
			TestCase.fail();
		}
		wtx.close();
	}
	
            @Ignore @Test
	public void permitLockingInFreeSubtreeTest() throws TreetankException{
	    IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final IWriteTransaction wtx2 = session.beginWriteTransaction();
            
		LockManager lock = LockManager.getLockManager();
		try {
			lock.getWritePermission(nodes[4], (SynchWriteTransaction) wtx);
			lock.getWritePermission(nodes[7], (SynchWriteTransaction) wtx2);
		} catch (Exception e) {
			TestCase.fail();
		}
	}
	
            @Ignore @Test
	public void denyLockingOnForeignTrnTest() throws TreetankException{
	    IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final IWriteTransaction wtx2 = session.beginWriteTransaction();
            
		LockManager lock = LockManager.getLockManager();
		lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(wtx2.getNode().getNodeKey(), (SynchWriteTransaction) wtx2);
			TestCase.fail();
		} catch (Exception e) {
			TestCase.assertTrue(true);	// has to fail
		}
	}
	
            @Ignore @Test
	public void denyLockingUnderForeignTrnTest() throws TreetankException{
	    IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final IWriteTransaction wtx2 = session.beginWriteTransaction();
            LockManager lock = LockManager.getLockManager();
		lock.getWritePermission(nodes[1], (SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(nodes[4], (SynchWriteTransaction) wtx2);
			TestCase.fail();
		} catch (Exception e) {
		    TestCase.assertTrue(true);        //has to fail
		}
	}
	
            @Ignore @Test
	public void denyLockingAboveForeignTrnTest() throws TreetankException{
	       IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
	            final ISession session = database.getSession();
	            final IWriteTransaction wtx = session.beginWriteTransaction();
	            final IWriteTransaction wtx2 = session.beginWriteTransaction();
	            LockManager lock = LockManager.getLockManager();
		lock.getWritePermission(nodes[4], (SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(nodes[1], (SynchWriteTransaction) wtx2);
			TestCase.fail();
		} catch (Exception e) {
		    TestCase.assertTrue(true);        //has to fail
		}
	}
	

            @Ignore @Test
	public void permitLockingAboveOwnTrnTest() throws TreetankException{
	       IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
	            final ISession session = database.getSession();
	            final IWriteTransaction wtx = session.beginWriteTransaction();
	            LockManager lock = LockManager.getLockManager();	            
		lock.getWritePermission(nodes[7], (SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(nodes[4], (SynchWriteTransaction) wtx);
		} catch (Exception e) {
			TestCase.fail();
		}
		
	}
	
            @Ignore @Test
	public void permitLockingAboveMultipleOwnTrnTest() throws TreetankException{
	       IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
	            final ISession session = database.getSession();
	            final IWriteTransaction wtx = session.beginWriteTransaction();
	            LockManager lock = LockManager.getLockManager();
		lock.getWritePermission(nodes[4], (SynchWriteTransaction) wtx);
		lock.getWritePermission(nodes[7], (SynchWriteTransaction) wtx);
		lock.getWritePermission(nodes[10], (SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(nodes[0], (SynchWriteTransaction) wtx);
		} catch (Exception e) {
			TestCase.fail();
		}
	}
	
            @Ignore @Test
	public void conquerReleasedSubtreeTest() throws TreetankException{
	       IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
	            final ISession session = database.getSession();
	            final IWriteTransaction wtx = session.beginWriteTransaction();
	            final IWriteTransaction wtx2 = session.beginWriteTransaction();
	            LockManager lock = LockManager.getLockManager();
		lock.getWritePermission(nodes[1], (SynchWriteTransaction) wtx);
		lock.releaseWritePermission((SynchWriteTransaction) wtx);
		try {
			lock.getWritePermission(nodes[1], (SynchWriteTransaction) wtx2);
		} catch (Exception e) {
			TestCase.fail();
		}
	}
	
}
