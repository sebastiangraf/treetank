/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import static org.treetank.testutil.CoreTestHelper.getFakedStructure;

import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.DumbDataFactory.DumbData;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.IConstants;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.CoreTestHelper.Holder;
import org.treetank.testutil.ModuleFactory;

import com.google.inject.Inject;

/**
 * 
 * Test-case for BucketReadTrx.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class BucketReadTrxTest {

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
     * Test method for {@link org.treetank.access.BucketReadTrx#getData(long)}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetData() throws TTException {
        DumbData[][] datas = CoreTestHelper.createTestData(mHolder);
        testGet(mHolder.getSession(), datas);
    }

    /**
     * Test method for {@link org.treetank.access.BucketReadTrx#close()} and
     * {@link org.treetank.access.BucketReadTrx#isClosed()}.
     * 
     * @throws TTException
     */
    @Test
    public void testCloseAndIsClosed() throws TTException {
        IBucketReadTrx rtx = mHolder.getSession().beginBucketRtx(mHolder.getSession().getMostRecentVersion());
        testClose(mHolder.getStorage(), mHolder.getSession(), rtx);
    }

    /**
     * Test method for {@link org.treetank.access.BucketReadTrx#getRevision()}.
     * 
     * @throws TTException
     */
    @Test
    public void testRevision() throws TTException {
        CoreTestHelper.createTestData(mHolder);
        testRevision(mHolder.getSession());
    }

    /**
     * Test method for {@link org.treetank.access.BucketReadTrx#getMetaBucket()}.
     * 
     * @throws TTException
     */
    @Test
    public void testGetMetaBucket() throws TTException {
        List<List<Map.Entry<DumbKey, DumbValue>>> data = CoreTestHelper.createTestMeta(mHolder);
        testMeta(mHolder.getSession(), data);
    }

    /**
     * Test method for
     * {@link org.treetank.access.BucketReadTrx#dereferenceLeafOfTree(org.treetank.io.IBackendReader, long, long)}
     * .
     */
    @Test
    public void testDereferenceLeafOfTree() throws TTIOException {
        int[] offsets = new int[5];

        IBackendReader reader = getFakedStructure(offsets);

        long key = BucketReadTrx.dereferenceLeafOfTree(reader, 1, 0)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 is base key because of 5 layers plus the 1 as start key
        assertEquals(6, key);

        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = BucketReadTrx.dereferenceLeafOfTree(reader, 1, 127)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus 127 as offset on last bucket
        assertEquals(133, key);

        offsets[3] = 1;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = BucketReadTrx.dereferenceLeafOfTree(reader, 1, 128)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus one additional offset on one level above
        assertEquals(7, key);

        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = BucketReadTrx.dereferenceLeafOfTree(reader, 1, 16383)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus two times 127 as offsets on level above
        assertEquals(260, key);

        offsets[2] = 1;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = BucketReadTrx.dereferenceLeafOfTree(reader, 1, 16384)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus one additional offset on two levels above
        assertEquals(7, key);

        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 2097151)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus three times 127 as offsets on levels above
        assertEquals(387, key);

        offsets[1] = 1;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 2097152)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 268435455)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus four times 127 as offsets on levels above
        assertEquals(514, key);

        offsets[0] = 1;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 268435456)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[0] = 127;
        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l)[IConstants.INDIRECT_BUCKET_COUNT.length];
        // 6 as base plus five times 127 as offsets on levels above
        assertEquals(641, key);

        // false offset, not existing
        offsets[0] = 0;
        reader = getFakedStructure(offsets);
        key =
            BucketReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l)[IConstants.INDIRECT_BUCKET_COUNT.length];
        assertEquals(-1, key);
    }

    /**
     * Test method for {@link org.treetank.access.BucketReadTrx#dataBucketOffset(long)}.
     */
    @Test
    public void testDataBucketOffset() {
        assertEquals(0, BucketReadTrx.dataBucketOffset(0));
        assertEquals(127, BucketReadTrx.dataBucketOffset(127));
        assertEquals(0, BucketReadTrx.dataBucketOffset(128));
        assertEquals(127, BucketReadTrx.dataBucketOffset(16383));
        assertEquals(0, BucketReadTrx.dataBucketOffset(16384));
        assertEquals(127, BucketReadTrx.dataBucketOffset(2097151));
        assertEquals(0, BucketReadTrx.dataBucketOffset(2097152));
    }

    protected static void testMeta(final ISession pSession,
        final List<List<Map.Entry<DumbKey, DumbValue>>> pEntries) throws TTException {
        long i = 0;
        for (List<Map.Entry<DumbKey, DumbValue>> entriesPerRev : pEntries) {
            final IBucketReadTrx rtx = pSession.beginBucketRtx(i);
            CoreTestHelper.checkStructure(entriesPerRev, rtx, true);
            i++;
        }
    }

    protected static void testGet(final ISession pSession, final DumbData[][] pDatas) throws TTException {
        // check against invalid datakey
        IBucketReadTrx rtx = pSession.beginBucketRtx(0);
        try {
            rtx.getData(-1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }
        rtx.close();
        // check against stored structure
        long dataKey = 0;
        for (int i = 0; i < pDatas.length; i++) {
            rtx = pSession.beginBucketRtx(i + 1);
            for (DumbData data : pDatas[i]) {
                assertEquals(data, rtx.getData(dataKey));
                dataKey++;
            }
            rtx.close();
        }
    }

    protected static void testRevision(final ISession pSession) throws TTException {
        for (long i = 0; i <= pSession.getMostRecentVersion(); i++) {
            final IBucketReadTrx rtx = pSession.beginBucketRtx(i);
            assertEquals(i, rtx.getRevision());
            rtx.close();
        }
    }

    protected static void testClose(final IStorage pStorage, final ISession pSession,
        final IBucketReadTrx pRtx) throws TTException {

        IBucketReadTrx rtx = pRtx;

        // explicit closing of one transaction
        rtx.getMetaBucket();
        assertFalse(rtx.isClosed());
        assertTrue(rtx.close());
        try {
            rtx.getMetaBucket();
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
            rtx.getData(0);
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }

        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());

        // implicit closing over session
        rtx = pSession.beginBucketRtx(pSession.getMostRecentVersion());
        assertFalse(rtx.isClosed());
        assertTrue(pSession.close());
        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());

        // implicit closing over storage
        ISession session = pStorage.getSession(new SessionConfiguration(CoreTestHelper.RESOURCENAME, null));
        rtx = session.beginBucketRtx(session.getMostRecentVersion());
        assertFalse(rtx.isClosed());
        assertTrue(pStorage.close());
        assertFalse(rtx.close());
        assertTrue(rtx.isClosed());
        assertFalse(session.close());
    }

}
