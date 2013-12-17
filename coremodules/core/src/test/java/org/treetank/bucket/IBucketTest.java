/**
 * 
 */
package org.treetank.bucket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.testutil.CoreTestHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Test class for all classes implementing the {@link IBucket} interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IBucketTest {

    /**
     * Test method for {@link org.treetank.bucket.interfaces.IBucket} and
     * {@link org.treetank.bucket.interfaces.IBucket#serialize(java.io.DataOutput)}.
     * 
     * @param clazz
     *            IBucket as class
     * @param pBuckets
     *            different buckets
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiateBuckets")
    public void testByteRepresentation(Class<IBucket> clazz, IBucket[] pBuckets) throws TTIOException {
        final BucketFactory fac = new BucketFactory(new DumbDataFactory(), new DumbMetaEntryFactory());

        for (final IBucket bucket : pBuckets) {
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            bucket.serialize(output);
            byte[] firstSerialized = output.toByteArray();
            ByteArrayDataInput input = ByteStreams.newDataInput(firstSerialized);

            final IBucket serializedBucket = fac.deserializeBucket(input);
            output = ByteStreams.newDataOutput();
            serializedBucket.serialize(output);
            byte[] secondSerialized = output.toByteArray();
            assertTrue(new StringBuilder("Check for ").append(bucket.getClass()).append(" failed.")
                .toString(), Arrays.equals(firstSerialized, secondSerialized));

        }
    }

    /**
     * Test method for {@link org.treetank.bucket.interfaces.IBucket} and
     * {@link org.treetank.bucket.interfaces.IBucket#serialize(java.io.DataOutput)}.
     * 
     * @param clazz
     *            IBucket as class
     * @param pBuckets
     *            different buckets
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiateBuckets")
    public void testEqualsAndHashCode(Class<IBucket> clazz, IBucket[] pBuckets) throws TTIOException {

        for (int i = 0; i < pBuckets.length; i++) {
            IBucket oneBucket = pBuckets[i % pBuckets.length];
            IBucket secondBucket = pBuckets[(i + 1) % pBuckets.length];
            assertEquals(oneBucket.hashCode(), oneBucket.hashCode());
            assertNotSame(oneBucket.hashCode(), secondBucket.hashCode());
            assertTrue(oneBucket.equals(oneBucket));
            assertFalse(oneBucket.equals(secondBucket));
        }

    }

    /**
     * Providing different implementations of the {@link IBucket} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IByteHandler}
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateBuckets")
    public Object[][] instantiateBuckets() throws TTByteHandleException {
        // UberBucket setup
        UberBucket uberBucket =
            new UberBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
        // IndirectBucket setup
        IndirectBucket indirectBucket = new IndirectBucket(CoreTestHelper.random.nextLong());
        // RevisionRootBucket setup
        RevisionRootBucket revRootBucket =
            new RevisionRootBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
        // DataBucket setup
        DataBucket dataBucket =
            new DataBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
        for (int i = 0; i < IConstants.CONTENT_COUNT - 1; i++) {
            dataBucket.setData(i, CoreTestHelper.generateOne());
        }
        // MetaBucket setup
        MetaBucket metaBucket = new MetaBucket(CoreTestHelper.random.nextLong());
        metaBucket.put(new DumbKey(CoreTestHelper.random.nextLong()), new DumbValue(CoreTestHelper.random
            .nextLong()));

        Object[][] returnVal = {
            {
                IBucket.class, new IBucket[] {
                    indirectBucket, revRootBucket, dataBucket, metaBucket, uberBucket
                }
            }
        };
        return returnVal;
    }
}
