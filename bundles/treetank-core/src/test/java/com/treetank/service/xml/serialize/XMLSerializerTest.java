/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: XMLSerializerTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

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
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.utils.DocumentCreater;

public class XMLSerializerTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testXMLSerializer() throws Exception {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializer serializer = new XMLSerializerBuilder(session, out)
                .build();
        serializer.call();
        assertEquals(DocumentCreater.XML, out.toString());
        session.close();
        database.close();
    }

    @Test
    public void testRestSerializer() throws Exception {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(session,
                out);
        builder.setREST(true);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.REST, out.toString());

        session.close();
        database.close();
    }

    @Test
    public void testIDSerializer() throws Exception {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(session,
                out);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.ID, out.toString());
        session.close();
        database.close();
    }

    @Test
    public void testSampleCompleteSerializer() throws Exception {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // generate serialize all from this session
        DocumentCreater.createVersioned(wtx);
        wtx.commit();
        wtx.close();

        XMLSerializer serializerall = new XMLSerializerBuilder(session, out, -1)
                .build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        out.reset();

        serializerall = new XMLSerializerBuilder(session, out, 0, 1, 2).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        session.close();
        database.close();
    }
}
