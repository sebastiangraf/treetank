/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NestedAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
    }

    @Test
    public void testNestedAxisTest() throws AbsTTException {
        final AbsAxisTest.Holder holder = AbsAxisTest.generateHolder();
        final IReadTransaction wtx = holder.rtx;

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /p:a/b/text()
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /text()
        final AbsAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter(wtx));
        // Part: /p:a/b/text()
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            6L, 12L
        });

        wtx.close();
        holder.session.close();
    }

    @Test
    public void testNestedAxisTest2() throws AbsTTException {
        final AbsAxisTest.Holder holder = AbsAxisTest.generateHolder();
        final IReadTransaction wtx = holder.rtx;

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /[:a/b/@p:x]
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /@x
        final AbsAxis attributeX = new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "p:x"));
        // Part: /p:a/b/@p:x
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB), attributeX);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            10L
        });

        wtx.close();
        holder.session.close();
    }

    @Test
    public void testNestedAxisTest3() throws AbsTTException {
        final AbsAxisTest.Holder holder = AbsAxisTest.generateHolder();
        final IReadTransaction wtx = holder.rtx;

        // Find desceFndants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression p:a/node():
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));

        // Part: /node()
        final AbsAxis childNode = new FilterAxis(new ChildAxis(wtx), new NodeFilter(wtx));

        // Part: /p:a/node():
        final AbsAxis axis = new NestedAxis(childA, childNode);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            4L, 5L, 8L, 9L, 13L
        });

        wtx.close();
        holder.session.close();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }
}
