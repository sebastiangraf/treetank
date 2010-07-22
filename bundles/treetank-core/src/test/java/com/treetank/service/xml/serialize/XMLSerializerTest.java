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

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
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
        final IReadTransaction rtx = session.beginReadTransaction();
        final XMLSerializer serializer = new XMLSerializerBuilder(rtx, out)
                .build();
        serializer.call();
        TestCase.assertEquals(DocumentCreater.XML, out.toString());
        rtx.close();
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
        final IReadTransaction rtx = session.beginReadTransaction();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(rtx, out);
        builder.setIntermediateSerializeRest(true);
        builder.setIntermediateId(true);
        builder.setIntermediateDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        TestCase.assertEquals(DocumentCreater.REST, out.toString());

        rtx.close();
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
        final IReadTransaction rtx = session.beginReadTransaction();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(rtx, out);
        builder.setIntermediateId(true);
        builder.setIntermediateDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        TestCase.assertEquals(DocumentCreater.ID, out.toString());
        rtx.close();
        session.close();
        database.close();
    }
}
