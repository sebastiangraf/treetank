/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.service.revIndex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;

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
    public void testTrie() throws TTException {
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
        final IAxis desc = new DescendantAxis(rtx);

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
    public void testDocument() throws TTException {
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
        final IAxis desc = new DescendantAxis(rtx);

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
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

}
