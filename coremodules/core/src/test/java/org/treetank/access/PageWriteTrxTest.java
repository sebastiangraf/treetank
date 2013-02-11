/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
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
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IPageWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.page.DumbMetaEntryFactory.DumbKey;
import org.treetank.page.DumbMetaEntryFactory.DumbValue;
import org.treetank.page.DumbNodeFactory.DumbNode;

import com.google.inject.Inject;

/**
 * Test case for the PageWriteTrx-class
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class PageWriteTrxTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private Holder mHolder;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        final ResourceConfiguration config =
            mResourceConfig.create(StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1
                .getFile().getAbsolutePath(), CoreTestHelper.RESOURCENAME));
        mHolder = CoreTestHelper.Holder.generateSession(config);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#prepareNodeForModification(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testPrepareNodeForModification() throws TTException {
        IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        wtx.close();
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getRevision()}.
     * 
     * @throws TTException
     */
    @Test
    public void testRevision() throws TTException {
        CoreTestHelper.createTestData(mHolder);
        PageReadTrxTest.testRevision(mHolder.getSession());
        IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        assertEquals(mHolder.getSession().getMostRecentVersion() + 1, wtx.getRevision());
        wtx.close();
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#finishNodeModification(org.treetank.api.INode)}
     * .
     */
    @Test
    public void testFinishNodeModification() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getMetaPage()}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetMetaPage() throws TTException {
        List<List<Map.Entry<DumbKey, DumbValue>>> meta = CoreTestHelper.createTestMeta(mHolder);
        PageReadTrxTest.testMeta(mHolder.getSession(), meta);
        IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        CoreTestHelper.checkStructure(meta.get(meta.size() - 1), wtx, false);
        wtx.commit();
        assertTrue(wtx.close());
        wtx = mHolder.getSession().beginPageWriteTransaction();
        assertEquals(0, wtx.getMetaPage().getMetaMap().size());

    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getNode(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetNode() throws TTException {
        DumbNode[][] nodes = CoreTestHelper.createTestData(mHolder);
        PageReadTrxTest.testGet(mHolder.getSession(), nodes);
        List<DumbNode> list = CoreTestHelper.combineNodes(nodes);
        final IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        CoreTestHelper.checkStructure(list, wtx, 0);
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#setNode(org.treetank.api.INode)}.
     * 
     * @throws TTException
     */
    @Test
    public void testSetNode() throws TTException {
        final IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        int elementsToSet = 16385;
        List<DumbNode> nodes = new ArrayList<DumbNode>();
        for (int i = 0; i < elementsToSet; i++) {
            long nodeKey = wtx.incrementNodeKey();
            nodes.add(new DumbNode(nodeKey, CoreTestHelper.random.nextLong()));
            if (i == 0) {
                assertNull(wtx.getNode(nodeKey));
            } else {
                try {
                    wtx.getNode(nodeKey);
                    fail();
                } catch (IllegalStateException exc) {
                }
            }
            wtx.setNode(nodes.get(i));
            assertEquals(nodes.get(i), wtx.getNode(nodeKey));
        }
        wtx.commit();

        for (int i = 0; i < elementsToSet; i++) {
            assertEquals(nodes.get(i), wtx.getNode(i));
            nodes.set(i, new DumbNode(i, CoreTestHelper.random.nextLong()));
            wtx.setNode(nodes.get(i));
            assertEquals(nodes.get(i), wtx.getNode(i));
        }

    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#removeNode(org.treetank.api.INode)}.
     * 
     * @throws TTException
     */
    @Test
    public void testRemoveNode() throws TTException {
        // DumbNode[][] nodes = CoreTestHelper.createNodesInTreetank(mHolder);
        // List<DumbNode> list = CoreTestHelper.combineNodes(nodes);
        // final IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        // int elementsDeleted = 10;
        // int revisions = 1;
        // for (int i = 0; i < revisions; i++) {
        // for (int j = 0; j < elementsDeleted; j++) {
        // int nextElementKey = (int)Math.abs(CoreTestHelper.random.nextLong() % list.size());
        // if (list.get(nextElementKey) != null) {
        // wtx.removeNode(list.get(nextElementKey));
        // list.set(nextElementKey, null);
        // }
        // }
        // }
        // wtx.close();
    }

    /**
     * Test method for
     * {@link org.treetank.access.PageWriteTrx#createEntry(org.treetank.api.IMetaEntry, org.treetank.api.IMetaEntry)}
     * .
     */
    @Test
    public void testCreateEntry() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#commit()}.
     */
    @Test
    public void testCommit() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#incrementNodeKey()}.
     */
    @Test
    public void testIncrementNodeKey() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#close()} and
     * {@link org.treetank.access.PageWriteTrx#isClosed()}.
     * 
     * @throws TTException
     */
    @Test
    public void testCloseAndIsClosed() throws TTException {
        IPageWriteTrx rtx = mHolder.getSession().beginPageWriteTransaction();
        PageReadTrxTest.testClose(mHolder.getStorage(), mHolder.getSession(), rtx);
    }

}
