package com.treetank.service.xml.serialize;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

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

public class TreetankCompleteSerializerTest {

	@Before
	public void setUp() throws TreetankException {
		TestHelper.deleteEverything();
	}

	@After
	public void tearDown() throws TreetankException {
		TestHelper.closeEverything();
	}

	@Test
	public void testSampleCompleteSerializer() throws Exception {
        // final IDatabase database = TestHelper
        // .getDatabase(PATHS.PATH1.getFile());
        // final ISession session = database.getSession();
        // final IWriteTransaction wtx = session.beginWriteTransaction();
        // final ByteArrayOutputStream out = new ByteArrayOutputStream();
        //
        // // generate serialize all from this session
        // DocumentCreater.createVersioned(wtx);
        //
        // RevsionedXMLSerializer serializerall = new RevsionedXMLSerializer(
        // out, session, false);
        // serializerall.serialize();
        // assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        // out.reset();
        //
        // serializerall = new RevsionedXMLSerializer(out, session,false, 0, 1,
        // 2);
        // serializerall.serialize();
        // assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        // session.close();
        // database.close();
	}

	@Test
	public void testCompleteSerializer() throws Exception {

	}
}
