package com.treetank.service.revIndex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.session.Session;

public class RevIndexTest {

    private RevIndex index;

    @Before
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
        index = new RevIndex(new File(ITestConstants.PATH1), -1);
    }

    @Test
    public void testTrie() throws TreetankException {
        TrieNavigator.adaptTrie((IWriteTransaction) index.getTrans(), "bla");
        TrieNavigator.adaptTrie((IWriteTransaction) index.getTrans(), "blubb");
        ((IWriteTransaction) index.getTrans()).commit();
        index.close();

        // check
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();
        rtx.moveToFirstChild();
        rtx.moveToRightSibling();
        final IAxis desc = new DescendantAxis(rtx);

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("b"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bl"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bla"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blu"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blub"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("t"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blubb"));
        assertTrue(rtx.moveToParent());

        assertFalse(desc.hasNext());
        rtx.close();
        session.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDocument() throws TreetankException {
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

        DocumentTreeNavigator.adaptDocTree(
                (IWriteTransaction) index.getTrans(), (Stack<String>) uuids1
                        .clone());
        index.finishIndexInput();

        DocumentTreeNavigator.adaptDocTree(
                (IWriteTransaction) index.getTrans(), (Stack<String>) uuids2
                        .clone());
        index.close();

        // check
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToFirstChild();
        rtx.moveToRightSibling();
        rtx.moveToRightSibling();
        final IAxis desc = new DescendantAxis(rtx);

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("b"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bl"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("bla"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blu"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blub"));
        assertTrue(rtx.moveToParent());

        assertTrue(desc.hasNext());
        assertTrue(rtx.moveTo(desc.next()));
        assertTrue(rtx.getNameOfCurrentNode().equals("d"));

        assertTrue(rtx.moveToAttribute(0));
        assertTrue(rtx.getValueOfCurrentNode().equals("blubb"));
        assertTrue(rtx.moveToParent());

        assertFalse(desc.hasNext());

        rtx.close();
        session.close();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

}
