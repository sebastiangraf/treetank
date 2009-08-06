
package com.treetank.session;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;

public class SynchWriteTest {

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "SynchWriteTest.tnk";

	@Before
	public void setUp() {
		Session.removeSession(PATH);
		createEnvironment();
	}

	@Test
	public void ConcurrentWritetransactionsTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		IWriteTransaction wtx = session.beginWriteTransaction(rtx.getNodeKey());
		rtx.moveToRightSibling();
		try {
			IWriteTransaction wtx2 = session.beginWriteTransaction(rtx.getNodeKey());
			wtx2.close();
		} catch (Exception e) {
			TestCase.fail("Unable to open concurrent write transaction.");
			session.close();
		}
				
		wtx.close();
		rtx.close();
		session.close();
		TestCase.assertTrue(true);
		
	}

	@Test
	public void concurrentWritingTest() throws IOException{
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		IWriteTransaction wtx1 = session.beginWriteTransaction(rtx.getNodeKey());
		rtx.moveToRightSibling();
		IWriteTransaction wtx2 = session.beginWriteTransaction(rtx.getNodeKey());
		
		wtx1.insertElementAsFirstChild("test", null);
		wtx2.insertElementAsFirstChild("juhu", null);
		
		
		wtx1.commit();
		wtx2.commit();
		
		wtx1.close();
		wtx2.close();
		rtx.close();
		
		IReadTransaction rtx2 = session.beginReadTransaction();
		/**
		 * This test case will work as soon as caching prevents "lost updates"
		 * 
		rtx2.moveToDocumentRoot();
		rtx2.moveToFirstChild();
		TestCase.assertTrue(rtx.moveToFirstChild());
		 * 
		 */
		rtx2.moveToDocumentRoot();
		rtx2.moveToFirstChild();
		rtx2.moveToRightSibling();
		TestCase.assertTrue(rtx2.moveToFirstChild());
		rtx2.close();
		session.close();
		
	}
	
	@Test 
	public void denyCreateRightSiblingOfTransactionRootNodeTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		rtx.moveToRightSibling();
		long hasNoRightSibling = rtx.getNodeKey();
		IWriteTransaction wtx = session.beginWriteTransaction(hasNoRightSibling);
		try {
			wtx.moveTo(hasNoRightSibling);
			wtx.insertElementAsRightSibling("test", null);
			TestCase.fail("Right sibling in transaction root node created");
			
		} catch (Exception expected) {}
		wtx.close();
		rtx.close();
		session.close();
		
	}
	
	@Test
	public void denyRemoveOfTransactionRootNodeTest() throws IOException{
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		
		IWriteTransaction wtx = session.beginWriteTransaction(rtx.getNodeKey());
		try {
			wtx.remove();
			TestCase.fail("Removal of Transaction root node prohibited.");
		} catch (Exception expecteds) {
		}
		
		wtx.close();
		rtx.close();
		session.close();
	}
	
	
	@Test
	public void denyWtxInSubtreeTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		IWriteTransaction wtx1 = session.beginWriteTransaction(rtx.DOCUMENT_ROOT_KEY);
		
		rtx.moveToFirstChild();
		try {
			IWriteTransaction wtx2 = session.beginWriteTransaction(rtx.getNodeKey());
			TestCase.fail("Write transaction in blocked subtree should not be permitted to start.");
			wtx2.close();
		} catch (Exception expected) {
			session.close();
		}
		
		rtx.close();
		wtx1.close();
		session.close();
	}
	
	@Test
	public void denyWtxInLockedPartentNodesTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		IWriteTransaction wtx1 = session.beginWriteTransaction(rtx.getNodeKey());
		try {
			IWriteTransaction wtx2 = session.beginWriteTransaction(rtx.DOCUMENT_ROOT_KEY);
			TestCase.fail("Write transaction in blocked parent nodes should not be permitted to start.");
		} catch (Exception expected) {
			session.close();
		}
		
		rtx.close();
		wtx1.close();
		session.close();
	}
	
	@Test
	public void denyRightSiblingInTrnTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		IWriteTransaction wtx = session.beginWriteTransaction(rtx.getNodeKey());
		TestCase.assertTrue(!wtx.moveToRightSibling());
		rtx.close();
		wtx.close();
		session.close();
	}
	
	@Test
	public void denyLeftSiblingInTrnTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		rtx.moveToRightSibling();
		IWriteTransaction wtx = session.beginWriteTransaction(rtx.getNodeKey());
		TestCase.assertTrue(!wtx.moveToLeftSibling());
		rtx.close();
		wtx.close();
		session.close();
	}
	
	@Test
	public void denyParentInTrnTest() throws IOException {
		ISession session = Session.beginSession(PATH);
		IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToFirstChild();
		IWriteTransaction wtx = session.beginWriteTransaction(rtx.getNodeKey());
		TestCase.assertTrue(!wtx.moveToParent());
		rtx.close();
		wtx.close();
		session.close();
	}
	
	private void createEnvironment(){
		ISession session = Session.beginSession(PATH);
		IWriteTransaction wtx = session.beginWriteTransaction();
		wtx.insertElementAsFirstChild("2", null);
		wtx.insertElementAsRightSibling("3", null);
		wtx.commit();
		wtx.close();
		session.close();
	}
}
