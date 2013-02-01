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
import org.treetank.api.IPageReadTrx;
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
     * Test method for {@link org.treetank.access.PageWriteTrx#setNode(org.treetank.api.INode)} and
     * {@link org.treetank.access.PageWriteTrx#getNode(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testSetAndGetNode() throws TTException {
        DumbNode[][] nodes = CoreTestHelper.createNodesInTreetank(mHolder);
        List<DumbNode> list = CoreTestHelper.combineNodes(nodes);
        final IPageWriteTrx wtx = mHolder.getSession().beginPageWriteTransaction();
        CoreTestHelper.checkStructure(list, wtx);
    }

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#removeNode(org.treetank.api.INode)}.
     */
    @Test
    public void testRemoveNode() {
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

    /**
     * Test method for {@link org.treetank.access.PageWriteTrx#getMetaPage()}.
     */
    @Test
    public void testGetMetaPage() {
        // fail("Not yet implemented");
    }

}
