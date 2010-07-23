package com.treetank;

import java.io.File;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;

/**
 * 
 * Test Class for TreeTank API
 * 
 * @author Bhummiwat Manphadung, KMUTNB
 * @param <ENodes>
 * 
 */

public final class TestAll {

	private IDatabase database;
	private ISession session;
	private IReadTransaction rtx;
	private IWriteTransaction wtx;
	private String value;
	private long AMOUNT = 100;

	@Before
	public void setUp() throws Exception {
		TestHelper.deleteEverything();

		/* MUST BE Shredder XML to *.tnk file before */
		String filename = "/mnt/data/home/pop221771/Desktop/book.tnk";

		database = Database.openDatabase(new File(filename));
		session = database.getSession();

	}

	// @Ignore
	@Test
	public final void testReadAPI() throws Exception {
		rtx = session.beginReadTransaction(0);
		// Show Database Info
		testUtil();

		/* Overriding amount for length of node */
		// AMOUNT = rtx.getMaxNodeKey();

		for (int i = 0; i <= AMOUNT; i++) {
			rtx.moveTo(i);
			printf("Node " + i + " : Type = " + rtx.getNode().getKind());

			if (rtx.getQNameOfCurrentNode() != null)
				printf("<" + rtx.getQNameOfCurrentNode() + ">");

			if (rtx.getNode().getKind().getNodeIdentifier() == 3) {
				long parentKey = rtx.getNode().getParentKey();
				rtx.moveTo(parentKey);
				printf("Parent Node : " + parentKey + " <"
						+ rtx.getQNameOfCurrentNode() + ">");
				rtx.moveTo(i);
			}

			if (rtx.getNode().getRawValue() != null) {
				value = new String(rtx.getNode().getRawValue());
				printf("Value = " + value);
			}

		}
		rtx.close();
	}

	@Ignore
	@Test
	public final void testWriteAPI() throws Exception {
		wtx = session.beginWriteTransaction();
		// printf("test");

		long i = 1;

		wtx.moveTo(i);
		// wtx.insertTextAsFirstChild(Integer.toString(a));
		wtx.insertElementAsRightSibling(QName.valueOf("book"));
		wtx.insertElementAsFirstChild(QName.valueOf("name"));
		wtx.insertTextAsFirstChild("Versakey");
		wtx.moveToParent();
		wtx.insertElementAsRightSibling(QName.valueOf("author"));
		wtx.insertTextAsFirstChild("pz");
		wtx.moveToParent();
		wtx.insertElementAsRightSibling(QName.valueOf("isbn"));
		wtx.insertTextAsFirstChild("10320");
		wtx.moveToParent();
		wtx.insertElementAsRightSibling(QName.valueOf("publisher"));
		wtx.insertTextAsFirstChild("Jamsai");

		wtx.commit();
		wtx.close();
		printf("Write Complete...");

	}

	// @Ignore
	@Test
	public final void testUtil() throws Exception {
		rtx = session.beginReadTransaction();
		printf("\n********** Number of All Nodes : " + rtx.getMaxNodeKey());
		printf("********** Current Revision : " + rtx.getRevisionNumber());
		printf("********** Current Revision Time : "
				+ new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
						.format(rtx.getRevisionTimestamp()));
		printf("----------------");
		Logger logger = LoggerFactory.getLogger(TestAll.class);
		logger.info("Number of All Nodes : {}",rtx.getMaxNodeKey());
	}

	@Ignore
	@Test
	public final void testRevertRev() throws Exception {
		long rev = 0;
		wtx = session.beginWriteTransaction();
		wtx.revertTo(rev);
		wtx.commit();
		printf("Revert to revision(" + rev + ") Complete !!!");
		wtx.close();
	}

	public final void printf(Object data) {
		System.out.println(data);
	}

	@After
	public void tearDown() {
		TestHelper.closeEverything();
	}

}