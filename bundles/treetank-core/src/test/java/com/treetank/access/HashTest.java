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

    private final static String NAME = "a";

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    @Ignore
    public void adaptHashWithInsertAndRemove() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        // inserting a element as root
        wtx.insertElementAsFirstChild(new QName(NAME));
        final long rootKey = wtx.getNode().getNodeKey();
        final long firstRootHash = wtx.getNode().getHash();

        // inserting a text as second child of root
        wtx.moveTo(rootKey);
        wtx.insertTextAsFirstChild(NAME);
        wtx.moveToParent();
        final long secondRootHash = wtx.getNode().getHash();

        // inserting a second element on level 2 under the only element
        wtx.moveToFirstChild();
        wtx.insertElementAsRightSibling(new QName(NAME));
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
        wtx.insertTextAsFirstChild(NAME);
        wtx.insertElementAsRightSibling(new QName(NAME));
        wtx.insertElementAsFirstChild(new QName(NAME));

        wtx.moveTo(rootKey);
        wtx.moveToFirstChild();
        wtx.moveToRightSibling();
        wtx.remove();
        wtx.moveToLeftSibling();
        wtx.remove();
        wtx.remove();

        wtx.moveTo(rootKey);
        assertEquals(firstRootHash, wtx.getNode().getHash());

    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

}
