/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
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
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.DumbNodeFactory.DumbNode;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

/**
 * Test case for the BucketWriteTrx-class
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class BucketWriteTrxTest {

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
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME));
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
     * Test method for {@link org.treetank.access.BucketWriteTrx#getRevision()}.
     * 
     * @throws TTException
     */
    @Test
    public void testRevision() throws TTException {
        CoreTestHelper.createTestData(mHolder);
        BucketReadTrxTest.testRevision(mHolder.getSession());
        IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        assertEquals(mHolder.getSession().getMostRecentVersion() + 1, wtx.getRevision());
        wtx.close();
    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#getMetaBucket()}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetMetaBucket() throws TTException {
        List<List<Map.Entry<DumbKey, DumbValue>>> meta = CoreTestHelper.createTestMeta(mHolder);
        BucketReadTrxTest.testMeta(mHolder.getSession(), meta);
        IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        CoreTestHelper.checkStructure(meta.get(meta.size() - 1), wtx, false);
        wtx.commit();
        assertTrue(wtx.close());
        wtx = mHolder.getSession().beginBucketWtx();
        assertEquals(0, wtx.getMetaBucket().getMetaMap().size());

    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#getNode(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetNode() throws TTException {
        DumbNode[][] nodes = CoreTestHelper.createTestData(mHolder);
        BucketReadTrxTest.testGet(mHolder.getSession(), nodes);
        List<DumbNode> list = CoreTestHelper.combineNodes(nodes);
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        CoreTestHelper.checkStructure(list, wtx, 0);
    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#setNode(org.treetank.api.INode)}.
     * 
     * @throws TTException
     */
    @Test
    public void testSetNode() throws TTException {
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        final int elementsToSet = 16385;
        final List<DumbNode> nodes = new ArrayList<DumbNode>();
        final int newVersions = 100;
        for (int i = 0; i < elementsToSet; i++) {
            long nodeKey = wtx.incrementNodeKey();
            DumbNode node = CoreTestHelper.generateOne();
            node.setNodeKey(nodeKey);
            nodes.add(node);
            if (i == 0) {
                assertNull(wtx.getNode(nodeKey));
            } else {
                try {
                    wtx.getNode(nodeKey);
                    fail();
                } catch (NullPointerException | IllegalStateException exc) {
                }
            }
            wtx.setNode(nodes.get(i));
            assertEquals(nodes.get(i), wtx.getNode(nodeKey));
        }
        wtx.commit();

        for (int j = 0; j < newVersions; j++) {
            for (int i = 0; i < elementsToSet; i++) {
                assertEquals(nodes.get(i), wtx.getNode(i));
                DumbNode node = CoreTestHelper.generateOne();
                node.setNodeKey(i);
                nodes.set(i, node);
                wtx.setNode(nodes.get(i));
                assertEquals(nodes.get(i), wtx.getNode(i));
            }
            CoreTestHelper.checkStructure(nodes, wtx, 0);
            wtx.commit();
            CoreTestHelper.checkStructure(nodes, wtx, 0);
        }
        final IBucketReadTrx rtx =
            mHolder.getSession().beginBucketRtx(mHolder.getSession().getMostRecentVersion());
        CoreTestHelper.checkStructure(nodes, rtx, 0);
        CoreTestHelper.checkStructure(nodes, wtx, 0);

    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#removeNode(org.treetank.api.INode)}.
     * 
     * @throws TTException
     */
    @Test
    public void testRemoveNode() throws TTException {
        DumbNode[][] nodes = CoreTestHelper.createTestData(mHolder);
        List<DumbNode> list = CoreTestHelper.combineNodes(nodes);
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        int elementsDeleted = 10;
        int revisions = 1;
        for (int i = 0; i < revisions; i++) {
            for (int j = 0; j < elementsDeleted; j++) {
                int nextElementKey = (int)Math.abs((CoreTestHelper.random.nextLong() + 1) % list.size());
                if (list.get(nextElementKey) != null) {
                    wtx.removeNode(list.get(nextElementKey));
                    list.set(nextElementKey, null);
                }
            }
            CoreTestHelper.checkStructure(list, wtx, 0);
            wtx.commit();
            CoreTestHelper.checkStructure(list, wtx, 0);
            final IBucketReadTrx rtx =
                mHolder.getSession().beginBucketRtx(mHolder.getSession().getMostRecentVersion());
            CoreTestHelper.checkStructure(list, rtx, 0);
            rtx.close();
        }
        wtx.close();
        final IBucketReadTrx rtx =
            mHolder.getSession().beginBucketRtx(mHolder.getSession().getMostRecentVersion());
        CoreTestHelper.checkStructure(list, rtx, 0);
    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#close()} and
     * {@link org.treetank.access.BucketWriteTrx#isClosed()}.
     * 
     * @throws TTException
     */
    @Test
    public void testCloseAndIsClosed() throws TTException {
        IBucketWriteTrx rtx = mHolder.getSession().beginBucketWtx();
        BucketReadTrxTest.testClose(mHolder.getStorage(), mHolder.getSession(), rtx);
    }

}
