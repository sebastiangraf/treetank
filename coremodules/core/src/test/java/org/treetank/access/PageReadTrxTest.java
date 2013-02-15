/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import static org.treetank.CoreTestHelper.getFakedStructure;

import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.CoreTestHelper.Holder;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.DumbMetaEntryFactory.DumbKey;
import org.treetank.page.DumbMetaEntryFactory.DumbValue;
import org.treetank.page.DumbNodeFactory.DumbNode;

import com.google.inject.Inject;

/**
 * 
 * Test-case for PageReadTrx.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class PageReadTrxTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private Holder mHolder;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        mHolder = CoreTestHelper.Holder.generateStorage();
        final ResourceConfiguration config =
            mResourceConfig.create(StandardSettings.getPropsAndCreateStructure(CoreTestHelper.PATHS.PATH1
                .getFile().getAbsolutePath(), CoreTestHelper.RESOURCENAME));
        CoreTestHelper.Holder.generateSession(mHolder, config);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getNode(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetNode() throws TTException {
        DumbNode[][] nodes = CoreTestHelper.createTestData(mHolder);
        testGet(mHolder.getSession(), nodes);
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#close()} and
     * {@link org.treetank.access.PageReadTrx#isClosed()}.
     * 
     * @throws TTException
     */
    @Test
    public void testCloseAndIsClosed() throws TTException {
        IPageReadTrx rtx =
            mHolder.getSession().beginPageReadTransaction(mHolder.getSession().getMostRecentVersion());
        testClose(mHolder.getStorage(), mHolder.getSession(), rtx);
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getRevision()}.
     * 
     * @throws TTException
     */
    @Test
    public void testRevision() throws TTException {
        CoreTestHelper.createTestData(mHolder);
        testRevision(mHolder.getSession());
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getMetaPage()}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetMetaPage() throws TTException {
        List<List<Map.Entry<DumbKey, DumbValue>>> data = CoreTestHelper.createTestMeta(mHolder);
        testMeta(mHolder.getSession(), data);
    }

    /**
     * Test method for
     * {@link org.treetank.access.PageReadTrx#dereferenceLeafOfTree(org.treetank.io.IBackendReader, long, long)}
     * .
     */
    @Test
    public void testDereferenceLeafOfTree() throws TTIOException {
        int[] offsets = new int[5];

        IBackendReader reader = getFakedStructure(offsets);
        long key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 0);
        // 6 is base key because of 5 layers plus the 1 as start key
        assertEquals(6, key);

        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 127);
        // 6 as base plus 127 as offset on last page
        assertEquals(133, key);

        offsets[3] = 1;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 128);
        // 6 as base plus one additional offset on one level above
        assertEquals(7, key);

        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16383);
        // 6 as base plus two times 127 as offsets on level above
        assertEquals(260, key);

        offsets[2] = 1;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16384);
        // 6 as base plus one additional offset on two levels above
        assertEquals(7, key);

        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097151);
        // 6 as base plus three times 127 as offsets on levels above
        assertEquals(387, key);

        offsets[1] = 1;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097152);
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435455);
        // 6 as base plus four times 127 as offsets on levels above
        assertEquals(514, key);

        offsets[0] = 1;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435456);
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[0] = 127;
        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l);
        // 6 as base plus five times 127 as offsets on levels above
        assertEquals(641, key);

        // false offset, not existing
        offsets[0] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l);
        assertEquals(-1, key);
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#nodePageOffset(long)}.
     */
    @Test
    public void testNodePageOffset() {
        assertEquals(0, PageReadTrx.nodePageOffset(0));
        assertEquals(127, PageReadTrx.nodePageOffset(127));
        assertEquals(0, PageReadTrx.nodePageOffset(128));
        assertEquals(127, PageReadTrx.nodePageOffset(16383));
        assertEquals(0, PageReadTrx.nodePageOffset(16384));
        assertEquals(127, PageReadTrx.nodePageOffset(2097151));
        assertEquals(0, PageReadTrx.nodePageOffset(2097152));
    }

    protected static void testMeta(final ISession pSession,
        final List<List<Map.Entry<DumbKey, DumbValue>>> pEntries) throws TTException {
        long i = 0;
        for (List<Map.Entry<DumbKey, DumbValue>> entriesPerRev : pEntries) {
            final IPageReadTrx rtx = pSession.beginPageReadTransaction(i);
            CoreTestHelper.checkStructure(entriesPerRev, rtx, true);
            i++;
        }
    }

    protected static void testGet(final ISession pSession, final DumbNode[][] pNodes) throws TTException {
        // check against invalid nodekey
        IPageReadTrx rtx = pSession.beginPageReadTransaction(0);
        try {
            rtx.getNode(-1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }
        rtx.close();
        // check against stored structure
        long nodeKey = 0;
        for (int i = 0; i < pNodes.length; i++) {
            rtx = pSession.beginPageReadTransaction(i + 1);
            for (DumbNode node : pNodes[i]) {
                assertEquals(node, rtx.getNode(nodeKey));
                nodeKey++;
            }
            rtx.close();
        }
    }

    protected static void testRevision(final ISession pSession) throws TTException {
        for (long i = 0; i <= pSession.getMostRecentVersion(); i++) {
            final IPageReadTrx rtx = pSession.beginPageReadTransaction(i);
            assertEquals(i, rtx.getRevision());
            rtx.close();
        }
    }

    protected static void
        testClose(final IStorage pStorage, final ISession pSession, final IPageReadTrx pRtx)
            throws TTException {

        IPageReadTrx rtx = pRtx;

        // explicit closing of one transaction
        rtx.getMetaPage();
        assertFalse(rtx.isClosed());
        assertTrue(rtx.close());
        try {
            rtx.getMetaPage();
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
        try {
            rtx.getRevision();
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
        try {
            rtx.getNode(0);
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }

        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());

        // implicit closing over session
        rtx = pSession.beginPageReadTransaction(pSession.getMostRecentVersion());
        assertFalse(rtx.isClosed());
        assertTrue(pSession.close());
        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());

        // implicit closing over storage
        ISession session = pStorage.getSession(new SessionConfiguration(CoreTestHelper.RESOURCENAME, null));
        rtx = session.beginPageReadTransaction(session.getMostRecentVersion());
        assertFalse(rtx.isClosed());
        assertTrue(pStorage.close());
        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());
        assertFalse(session.close());
    }

}
