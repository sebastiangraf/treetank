package com.treetank.access;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HashTest {

    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";
    private static final String XML = RESOURCES + File.separator + "revXMLsSame" + File.separator + "1.xml";

    private final static String NAME1 = "a";
    private final static String NAME2 = "b";

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    @Ignore
    public void testPostorderNamespace() throws Exception {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        testNamespace(session);

    }

    @Test
    public void testPostorderInsertRemove() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testHashTreeWithInsertAndRemove(wtx);
    }

    @Test
    public void testPostorderDeep() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testDeepTree(wtx);
    }

    @Test
    @Ignore
    public void testPostorderSetter() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testSetter(wtx);
    }

    @Test
    public void testRollingNamespace() throws Exception {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        testNamespace(session);

    }

    @Test
    public void testRollingInsertRemove() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testHashTreeWithInsertAndRemove(wtx);
    }

    @Test
    public void testRollingDeep() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testDeepTree(wtx);
    }

    @Test
    public void testRollingSetter() throws TreetankException {
        TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        testSetter(wtx);
    }

    /**
     * Inserting nodes and removing them.
     * 
     * <pre>
     * -a (1)
     *  '-test (5)
     *  '-a (6)
     *    '-attr(7)
     *    '-a (8)
     *      '-attr (9)
     *  '-text (2)
     *  '-a (3(x))
     *    '-attr(4(x))
     * </pre>
     * 
     * @param wtx
     * @throws TreetankException
     */
    private void testHashTreeWithInsertAndRemove(final IWriteTransaction wtx) throws TreetankException {

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
        wtx.insertAttribute(new QName(NAME2), NAME1);
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

    private void testNamespace(final ISession session) throws TreetankException, IOException,
        XMLStreamException {
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLShredder shredder = new XMLShredder(wtx, XMLShredder.createReader(new File(XML)), true);
        shredder.call();
        wtx.close();
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToFirstChild();
        final long nodeKey = rtx.getNode().getNodeKey();
        rtx.moveToNamespace(0);
        final long firstHash = rtx.getNode().getHash();
        rtx.moveTo(nodeKey);
        rtx.moveToNamespace(1);
        final long secondHash = rtx.getNode().getHash();
        assertFalse(firstHash == secondHash);
    }

    private void testDeepTree(final IWriteTransaction wtx) throws TreetankException {

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

    private void testSetter(final IWriteTransaction wtx) throws TreetankException {
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
