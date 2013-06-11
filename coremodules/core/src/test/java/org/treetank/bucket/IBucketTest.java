/**
 * 
 */
package org.treetank.bucket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbNodeFactory;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.io.bytepipe.IByteHandler;

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
     * @param pPages
     *            different pages
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiatePages")
    public void testByteRepresentation(Class<IBucket> clazz, IBucket[] pPages) throws TTIOException {
        final BucketFactory fac = new BucketFactory(new DumbNodeFactory(), new DumbMetaEntryFactory());

        for (final IBucket bucket : pPages) {
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            bucket.serialize(output);
            byte[] firstSerialized = output.toByteArray();
            ByteArrayDataInput input = ByteStreams.newDataInput(firstSerialized);

            final IBucket serializedPage = fac.deserializeBucket(input);
            output = ByteStreams.newDataOutput();
            serializedPage.serialize(output);
            byte[] secondSerialized = output.toByteArray();
            assertTrue(new StringBuilder("Check for ").append(bucket.getClass()).append(" failed.").toString(),
                Arrays.equals(firstSerialized, secondSerialized));

        }
    }

    /**
     * Test method for {@link org.treetank.bucket.interfaces.IBucket} and
     * {@link org.treetank.bucket.interfaces.IBucket#serialize(java.io.DataOutput)}.
     * 
     * @param clazz
     *            IBucket as class
     * @param pPages
     *            different pages
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiatePages")
    public void testEqualsAndHashCode(Class<IBucket> clazz, IBucket[] pPages) throws TTIOException {

        for (int i = 0; i < pPages.length; i++) {
            IBucket onePage = pPages[i % pPages.length];
            IBucket secondPage = pPages[(i + 1) % pPages.length];
            assertEquals(onePage.hashCode(), onePage.hashCode());
            assertNotSame(onePage.hashCode(), secondPage.hashCode());
            assertTrue(onePage.equals(onePage));
            assertFalse(onePage.equals(secondPage));
        }

    }

    /**
     * Providing different implementations of the {@link IBucket} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IByteHandler}
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiatePages")
    public Object[][] instantiatePages() throws TTByteHandleException {
        // UberBucket setup
        UberBucket uberBucket =
            new UberBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
        // IndirectBucket setup
        IndirectBucket indirectBucket = new IndirectBucket(CoreTestHelper.random.nextLong());
        // RevisionRootBucket setup
        RevisionRootBucket revRootPage =
            new RevisionRootBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
        // NodeBucket setup
        NodeBucket nodeBucket = new NodeBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
        for (int i = 0; i < IConstants.CONTENT_COUNT - 1; i++) {
            nodeBucket.setNode(i, CoreTestHelper.generateOne());
        }
        // MetaBucket setup
        MetaBucket metaBucket = new MetaBucket(CoreTestHelper.random.nextLong());
        metaBucket.setEntry(new DumbKey(CoreTestHelper.random.nextLong()), new DumbValue(CoreTestHelper.random
            .nextLong()));

        Object[][] returnVal = {
            {
                IBucket.class, new IBucket[] {
                    indirectBucket, revRootPage, nodeBucket, metaBucket, uberBucket
                }
            }
        };
        return returnVal;
    }
}
