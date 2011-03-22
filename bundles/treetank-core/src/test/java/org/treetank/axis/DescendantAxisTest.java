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

package org.treetank.axis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.settings.EFixed;
import org.treetank.utils.DocumentCreater;

public class DescendantAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testIterate() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx), new long[] {
            1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
        });

        wtx.moveTo(1L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx), new long[] {
            4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
        });

        wtx.moveTo(9L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx), new long[] {
            11L, 12L
        });

        wtx.moveTo(13L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx), new long[] {});

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testIterateIncludingSelf() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx, true), new long[] {
            (Long)EFixed.ROOT_NODE_KEY.getStandardProperty(), 1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
        });

        wtx.moveTo(1L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx, true), new long[] {
            1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
        });

        wtx.moveTo(9L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx, true), new long[] {
            9L, 11L, 12L
        });

        wtx.moveTo(13L);
        AbsAxisTest.testIAxisConventions(new DescendantAxis(wtx, true), new long[] {
            13L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }
}
