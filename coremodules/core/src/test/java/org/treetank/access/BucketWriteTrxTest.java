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
import java.util.Properties;

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
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.IDataFactory;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.DumbDataFactory;
import org.treetank.bucket.DumbDataFactory.DumbData;
import org.treetank.exception.TTException;
import org.treetank.io.IBackend;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.revisioning.IRevisioning;
import org.treetank.revisioning.SlidingSnapshot;

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
        assertEquals(0, wtx.getMetaBucket().size());

    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#getData(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetData() throws TTException {
        DumbData[][] datas = CoreTestHelper.createTestData(mHolder);
        BucketReadTrxTest.testGet(mHolder.getSession(), datas);
        List<DumbData> list = CoreTestHelper.combineDatas(datas);
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        CoreTestHelper.checkStructure(list, wtx, 0);
    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#setData(org.treetank.api.IData)}.
     * 
     * @throws TTException
     */
    @Test
    public void testSetData() throws TTException {
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        final int elementsToSet = 16385;
        final int versionToWrite = 10;
        // int elementsToSet = 10;
        final List<DumbData> datas = new ArrayList<DumbData>();
        for (int i = 0; i < elementsToSet; i++) {
            long dataKey = wtx.incrementDataKey();
            DumbData data = CoreTestHelper.generateOne();
            data.setDataKey(dataKey);
            datas.add(data);
            if (i == 0) {
                assertNull(wtx.getData(dataKey));
            } else {
                try {
                    wtx.getData(dataKey);
                    fail();
                } catch (NullPointerException | IllegalStateException exc) {
                }
            }
            wtx.setData(datas.get(i));
            assertEquals(datas.get(i), wtx.getData(dataKey));
        }
        wtx.commit();

        int numbersToAdapt = 16;
        for (int j = 0; j < versionToWrite; j++) {
            for (int i = 0; i < elementsToSet; i++) {
                assertEquals(new StringBuilder("Datas differ in version ").append(j).append(" and data ")
                    .append(i).toString(), datas.get(i), wtx.getData(i));
                if (i % numbersToAdapt == 0) {
                    DumbData data = CoreTestHelper.generateOne();
                    data.setDataKey(i);
                    datas.set(i, data);
                    wtx.setData(datas.get(i));
                    assertEquals(datas.get(i), wtx.getData(i));
                }
            }
            CoreTestHelper.checkStructure(datas, wtx, 0);
            wtx.commit();
            CoreTestHelper.checkStructure(datas, wtx, 0);
            numbersToAdapt++;
        }

        final IBucketReadTrx rtx =
            mHolder.getSession().beginBucketRtx(mHolder.getSession().getMostRecentVersion());
        CoreTestHelper.checkStructure(datas, rtx, 0);
        CoreTestHelper.checkStructure(datas, wtx, 0);

    }

    /**
     * Test method for {@link org.treetank.access.BucketWriteTrx#removeData(org.treetank.api.IData)}.
     * 
     * @throws TTException
     */
    @Test
    public void testRemoveData() throws TTException {
        DumbData[][] datas = CoreTestHelper.createTestData(mHolder);
        List<DumbData> list = CoreTestHelper.combineDatas(datas);
        final IBucketWriteTrx wtx = mHolder.getSession().beginBucketWtx();
        int elementsDeleted = 10;
        int revisions = 1;
        for (int i = 0; i < revisions; i++) {
            for (int j = 0; j < elementsDeleted; j++) {
                int nextElementKey = (int)Math.abs((CoreTestHelper.random.nextLong() + 1) % list.size());
                if (list.get(nextElementKey) != null) {
                    wtx.removeData(list.get(nextElementKey));
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

    public static void main(String[] args) {
        try {
            System.out.println("STARTING!!!!");
            long time = System.currentTimeMillis();
            BucketWriteTrxTest test = new BucketWriteTrxTest();
            CoreTestHelper.deleteEverything();

            final IRevisioning revision = new SlidingSnapshot();
            final IDataFactory dataFac = new DumbDataFactory();
            final IMetaEntryFactory metaFac = new DumbMetaEntryFactory();
            final Properties props =
                StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                    CoreTestHelper.RESOURCENAME);
            final IBackend backend = new JCloudsStorage(props, dataFac, metaFac, new ByteHandlerPipeline());

            ResourceConfiguration config =
                new ResourceConfiguration(props, backend, revision, dataFac, metaFac);
            test.mHolder = CoreTestHelper.Holder.generateStorage();
            CoreTestHelper.Holder.generateSession(test.mHolder, config);

            test.testGetData();

            CoreTestHelper.closeEverything();
            System.out.println(System.currentTimeMillis() - time + "ms");
            System.out.println("ENDING!!!!");
            CoreTestHelper.deleteEverything();
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(-1);
        }
    }
}
