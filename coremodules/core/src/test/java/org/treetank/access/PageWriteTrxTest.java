/**
 * 
 */
package org.treetank.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        int nodesPerRevision[] = {
            2097153, 2097153, 2097153, 2097153
        };

        DumbNode[][] nodes = CoreTestHelper.createRevisions(nodesPerRevision, wtx);

        // CoreTestHelper.createRevisions(nodesPerRevision, wtx);
        List<DumbNode> list = new ArrayList<DumbNode>();
        for (int i = 0; i < nodes.length; i++) {
            list.addAll(Arrays.asList(nodes[i]));
        }
        CoreTestHelper.checkStructure(list, wtx);

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
     * Test method for {@link org.treetank.access.PageWriteTrx#setNode(org.treetank.api.INode)}.
     */
    @Test
    public void testSetNode() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#removeNode(org.treetank.api.INode)}.
     */
    @Test
    public void testRemoveNode() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getNode(long)}.
     */
    @Test
    public void testGetNode() {
        // fail("Not yet implemented");
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
     * Test method for {@link org.treetank.access.PageWriteTrx#close()}.
     */
    @Test
    public void testClose() {
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
     * Test method for {@link org.treetank.access.PageWriteTrx#getActualRevisionRootPage()}.
     */
    @Test
    public void testGetActualRevisionRootPage() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#isClosed()}.
     */
    @Test
    public void testIsClosed() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getMetaPage()}.
     */
    @Test
    public void testGetMetaPage() {
        // fail("Not yet implemented");
    }

}
