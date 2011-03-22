/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import javax.xml.namespace.QName;

import junit.framework.Assert;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.PostOrderAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleCommitTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void test() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        Assert.assertEquals(0L, wtx.getRevisionNumber());

        wtx.commit();

        wtx.insertElementAsFirstChild(new QName("foo"));
        assertEquals(1L, wtx.getRevisionNumber());
        wtx.moveTo(1);
        assertEquals(new QName("foo"), wtx.getQNameOfCurrentNode());
        wtx.abort();

        assertEquals(1L, wtx.getRevisionNumber());

        wtx.close();

        session.close();
        database.close();
    }

    @Test
    public void testAutoCommit() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction(100, 1);
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testRemove() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        assertEquals(1L, wtx.getRevisionNumber());

        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        wtx.remove();
        wtx.commit();
        assertEquals(2L, wtx.getRevisionNumber());

        wtx.close();
        session.close();
        database.close();

    }

    @Test
    public void testAttributeRemove() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.moveToDocumentRoot();

        final AbsAxis postorderAxis = new PostOrderAxis(wtx);
        while (postorderAxis.hasNext()) {
            postorderAxis.next();
            if (wtx.getNode().getKind() == ENodes.ELEMENT_KIND
                && ((ElementNode)wtx.getNode()).getAttributeCount() > 0) {
                for (int i = 0, attrCount = ((ElementNode)wtx.getNode()).getAttributeCount(); i < attrCount; i++) {
                    wtx.moveToAttribute(i);
                    wtx.remove();
                }
            }
        }
        wtx.commit();
        wtx.moveToDocumentRoot();

        int attrTouch = 0;
        final AbsAxis descAxis = new DescendantAxis(wtx);
        while (descAxis.hasNext()) {
            descAxis.next();
            if (wtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                for (int i = 0, attrCount = ((ElementNode)wtx.getNode()).getAttributeCount(); i < attrCount; i++) {
                    if (wtx.moveToAttribute(i)) {
                        attrTouch++;
                    } else {
                        throw new IllegalStateException("Should never occur!");
                    }
                }
            }
        }
        wtx.close();
        session.close();
        database.close();
        assertEquals(0, attrTouch);

    }

}
