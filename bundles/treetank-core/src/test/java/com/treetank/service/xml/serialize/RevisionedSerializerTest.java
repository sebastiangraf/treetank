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
import com.treetank.service.xml.serialize.SerializerBuilder.RevisionedXMLSerializerBuilder;
import com.treetank.utils.DocumentCreater;

public class RevisionedSerializerTest {

//    @Before
//    public void setUp() throws TreetankException {
//        TestHelper.deleteEverything();
//    }
//
//    @After
//    public void tearDown() throws TreetankException {
//        TestHelper.closeEverything();
//    }
//
//    @Test
//    public void testSampleCompleteSerializer() throws Exception {
//        final IDatabase database = TestHelper
//                .getDatabase(PATHS.PATH1.getFile());
//        final ISession session = database.getSession();
//        final IWriteTransaction wtx = session.beginWriteTransaction();
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        // generate serialize all from this session
//        DocumentCreater.createVersioned(wtx);
//        wtx.commit();
//        wtx.close();
//
//        RevisionedXMLSerializer serializerall = new RevisionedXMLSerializerBuilder(
//                session, out).build();
//        serializerall.serialize();
//        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
//        out.reset();
//
//        serializerall = new RevisionedXMLSerializerBuilder(session, out, 0, 1,
//                2).build();
//        serializerall.serialize();
//        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
//        session.close();
//        database.close();
//    }
//
//    @Test
//    public void testCompleteSerializer() throws Exception {
//
//    }
}
