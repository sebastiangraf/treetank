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
 * $Id: AttributeAndNamespaceTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package org.treetank.access;

import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ElementNode;
import org.treetank.utils.DocumentCreater;

public class AttributeAndNamespaceTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testAttribute() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveTo(1L);
        assertEquals(1, ((ElementNode)wtx.getNode()).getAttributeCount());
        wtx.moveToAttribute(0);
        assertEquals("i", wtx.nameForKey(wtx.getNode().getNameKey()));

        wtx.moveTo(9L);
        assertEquals(1, ((ElementNode)wtx.getNode()).getAttributeCount());
        wtx.moveToAttribute(0);
        assertEquals("p:x", wtx.nameForKey(wtx.getNode().getNameKey()));
        assertEquals("ns", wtx.nameForKey(wtx.getNode().getURIKey()));

        wtx.abort();
        wtx.close();
        session.close();
        database.close();

    }

    @Test
    public void testNamespace() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveTo(1L);
        assertEquals(1, ((ElementNode)wtx.getNode()).getNamespaceCount());
        wtx.moveToNamespace(0);
        assertEquals("p", wtx.nameForKey(wtx.getNode().getNameKey()));
        assertEquals("ns", wtx.nameForKey(wtx.getNode().getURIKey()));

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}
