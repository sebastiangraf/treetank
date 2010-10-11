package com.treetank.access;

import javax.xml.namespace.QName;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HashTest {

    private final static String NAME1 = "a";
    private final static String NAME2 = "b";

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    public void adaptHashWithInsertAndRemove() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        // inserting a element as root
        wtx.insertElementAsFirstChild(new QName(NAME1));
        final long rootKey = wtx.getNode().getNodeKey();
        final long firstRootHash = wtx.getNode().getHash();

        // inserting a text as second child of root
        wtx.moveTo(rootKey);
        wtx.insertTextAsFirstChild(NAME1);
        wtx.moveToParent();
        final long secondRootHash = wtx.getNode().getHash();

        // inserting a second element on level 2 under the only element
        wtx.moveToFirstChild();
        wtx.insertElementAsRightSibling(new QName(NAME2));
        // wtx.insertAttribute(new QName(NAME), NAME);
        wtx.moveTo(rootKey);
        final long thirdRootHash = wtx.getNode().getHash();

        // Checking that all hashes are different
        assertFalse(firstRootHash == secondRootHash);
        assertFalse(firstRootHash == thirdRootHash);
        assertFalse(secondRootHash == thirdRootHash);

        // removing the second element
        wtx.moveToFirstChild();
        wtx.moveToRightSibling();
        wtx.remove();
        wtx.moveTo(rootKey);
        assertEquals(secondRootHash, wtx.getNode().getHash());

        // adding additional element for showing that hashes are computed incrementilly
        wtx.insertTextAsFirstChild(NAME1);
        wtx.insertElementAsRightSibling(new QName(NAME1));
        wtx.insertAttribute(new QName(NAME1), NAME2);
        wtx.moveToParent();
        wtx.insertElementAsFirstChild(new QName(NAME1));
        wtx.insertAttribute(new QName(NAME2), NAME1);

        wtx.moveTo(rootKey);
        wtx.moveToFirstChild();
        wtx.remove();
        wtx.remove();
        wtx.remove();

        wtx.moveTo(rootKey);
        assertEquals(firstRootHash, wtx.getNode().getHash());

    }

    @Test
    public void testDeepTree() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        wtx.insertElementAsFirstChild(new QName(NAME1));
        final long oldHash = wtx.getNode().getHash();

        wtx.insertElementAsFirstChild(new QName(NAME1));
        wtx.insertElementAsFirstChild(new QName(NAME2));
        wtx.insertElementAsFirstChild(new QName(NAME1));
        wtx.insertElementAsFirstChild(new QName(NAME2));
        wtx.insertElementAsFirstChild(new QName(NAME1));
        wtx.remove();
        wtx.insertElementAsFirstChild(new QName(NAME2));
        wtx.insertElementAsFirstChild(new QName(NAME2));
        wtx.insertElementAsFirstChild(new QName(NAME1));

        wtx.moveTo(1);
        wtx.moveToFirstChild();
        wtx.remove();
        assertEquals(oldHash, wtx.getNode().getHash());

    }

    @Test
    public void testSetter() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        wtx.insertElementAsFirstChild(new QName(NAME1));
        final long hashRoot1 = wtx.getNode().getHash();
        wtx.insertElementAsFirstChild(new QName(NAME1));
        final long leafKey = wtx.insertElementAsFirstChild(new QName(NAME1));
        final long hashLeaf1 = wtx.getNode().getHash();
        wtx.setName(NAME2);
        final long hashLeaf2 = wtx.getNode().getHash();
        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        final long hashRoot2 = wtx.getNode().getHash();
        assertFalse(hashRoot1 == hashRoot2);
        assertFalse(hashLeaf1 == hashLeaf2);
        wtx.moveTo(leafKey);
        wtx.setName(NAME1);
        final long hashLeaf3 = wtx.getNode().getHash();
        assertEquals(hashLeaf1, hashLeaf3);
        wtx.moveToDocumentRoot();
        wtx.moveToFirstChild();
        final long hashRoot3 = wtx.getNode().getHash();
        assertEquals(hashRoot1, hashRoot3);
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

}
