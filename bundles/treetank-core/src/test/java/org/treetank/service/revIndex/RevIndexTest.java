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

package org.treetank.service.revIndex;

import java.util.Stack;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.revIndex.DocumentTreeNavigator;
import org.treetank.service.revIndex.RevIndex;
import org.treetank.service.revIndex.TrieNavigator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 
 */
public class RevIndexTest {

    private RevIndex index;

    @Before
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
        index = new RevIndex(TestHelper.PATHS.PATH1.getFile(), -1);
    }

    @Test
    public void testTrie() throws AbsTTException {
        TrieNavigator.adaptTrie((IWriteTransaction)index.getTrans(), "bla");
        TrieNavigator.adaptTrie((IWriteTransaction)index.getTrans(), "blubb");
        ((IWriteTransaction)index.getTrans()).commit();
        index.close();

        // check
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();
        rtx.moveToFirstChild();
        rtx.moveToRightSibling();
        final AbsAxis desc = new DescendantAxis(rtx);

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("b"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bl"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bla"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blu"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blub"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blubb"));
        assertTrue(rtx.moveToParent());

        assertFalse(desc.hasNext());
        rtx.close();
        session.close();
        database.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDocument() throws AbsTTException {
        final Stack<String> uuids1 = new Stack<String>();
        uuids1.push("bla");
        uuids1.push("bl");
        uuids1.push("b");

        final Stack<String> uuids2 = new Stack<String>();
        uuids2.push("blubb");
        uuids2.push("blub");
        uuids2.push("blu");
        uuids2.push("bl");
        uuids2.push("b");

        DocumentTreeNavigator
            .adaptDocTree((IWriteTransaction)index.getTrans(), (Stack<String>)uuids1.clone());
        index.finishIndexInput();

        DocumentTreeNavigator
            .adaptDocTree((IWriteTransaction)index.getTrans(), (Stack<String>)uuids2.clone());
        index.close();

        // check
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToFirstChild();
        rtx.moveToRightSibling();
        rtx.moveToRightSibling();
        final AbsAxis desc = new DescendantAxis(rtx);

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("b"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bl"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bla"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blu"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blub"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getQNameOfCurrentNode().getLocalPart().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blubb"));
        assertTrue(rtx.moveToParent());

        assertFalse(desc.hasNext());

        rtx.close();
        session.close();
        database.close();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

}
