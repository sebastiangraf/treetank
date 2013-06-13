/**
 * 
 */
package org.treetank.revisioning;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.treetank.CoreTestHelper.getNodeBucket;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.NodeBucket;
import org.treetank.exception.TTByteHandleException;
import org.treetank.io.LogValue;

/**
 * Test for {@link IRevisioning}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IRevisioningTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.revisioning.IRevisioning#combineBuckets(org.treetank.bucket.NodeBucket[])}.
     * This test just takes two versions and checks if the version-counter is interpreted correctly.
     * 
     * @param pRevisioningClass
     *            class for the revisioning approaches
     * @param pRevisioning
     *            the different revisioning approaches
     * @param pRevisionCheckerClass
     *            class for the revisioning-check approaches
     * @param pRevisionChecker
     *            the different revisioning-check approaches
     * @param pNodeGeneratorClass
     *            class for node-generator
     * @param pNodeGenerator
     *            different node-generators
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testCombineBucketsForModification(Class<IRevisioning> pRevisioningClass,
        IRevisioning[] pRevisioning, Class<IRevisionChecker> pRevisionCheckerClass,
        IRevisionChecker[] pRevisionChecker, Class<INodeBucketGenerator> pNodeGeneratorClass,
        INodeBucketGenerator[] pNodeGenerator) {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevisionChecker.length);
        assertEquals(pRevisioning.length, pNodeGenerator.length);

        // test for full-dumps-including versionings
        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...check if revision is not SlidingSnapshot (since SlidingSnapshot is not working with entire
            // full-dump...
            if (!(pRevisioning[i] instanceof SlidingSnapshot)) {
                // ...get the node buckets for not full-dump test and...
                final NodeBucket[] buckets = pNodeGenerator[i].generateNodeBuckets();
                // ..recombine them...
                final LogValue bucket =
                    pRevisioning[i].combineBucketsForModification(buckets.length, 0, buckets, true);
                // ...and check them suitable to the versioning approach
                pRevisionChecker[i].checkCompleteBucketsForModification(bucket, buckets, true);
            }
        }

        // test for non-full-dumps-including versionings
        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...check if revision is not FullDump (since FullDump must always be used within FullDump)
            // and...
            if (!(pRevisioning[i] instanceof FullDump)) {
                // ...get the node buckets for full-dump test and...
                final NodeBucket[] buckets = pNodeGenerator[i].generateNodeBuckets();
                // ..recombine them...
                final LogValue bucket =
                    pRevisioning[i].combineBucketsForModification(buckets.length - 1, 0, buckets, false);
                // ...and check them suitable to the versioning approach
                pRevisionChecker[i].checkCompleteBucketsForModification(bucket, buckets, false);
            }
        }
    }

    /**
     * Test method for
     * {@link org.treetank.revisioning.IRevisioning#combineBucketsForModification(int, long, NodeBucket[], boolean)}.
     * This test just takes two versions and checks if the version-counter is interpreted correctly.
     * 
     * @param pRevisioningClass
     *            class for the revisioning approaches
     * @param pRevisioning
     *            the different revisioning approaches
     * @param pRevisionCheckerClass
     *            class for the revisioning-check approaches
     * @param pRevisionChecker
     *            the different revisioning-check approaches
     * @param pNodeGeneratorClass
     *            class for node-generator
     * @param pNodeGenerator
     *            different node-generators
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testCombineBuckets(Class<IRevisioning> pRevisioningClass, IRevisioning[] pRevisioning,
        Class<IRevisionChecker> pRevisionCheckerClass, IRevisionChecker[] pRevisionChecker,
        Class<INodeBucketGenerator> pNodeGeneratorClass, INodeBucketGenerator[] pNodeGenerator) {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevisionChecker.length);
        assertEquals(pRevisioning.length, pNodeGenerator.length);

        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...get the node buckets and...
            final NodeBucket[] buckets = pNodeGenerator[i].generateNodeBuckets();
            // ..and recombine them...
            final NodeBucket bucket = pRevisioning[i].combineBuckets(buckets);
            // ...and check them suitable to the versioning approach
            pRevisionChecker[i].checkCompleteBuckets(bucket, buckets);
        }
    }

    /**
     * Providing different implementations of the {@link IRevisioning} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IRevisioning} and <code>IRevisionChecker</code>
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateVersioning")
    public Object[][] instantiateVersioning() throws TTByteHandleException {

        Object[][] returnVal = {
            {
                IRevisioning.class, new IRevisioning[] {
                    new FullDump(), new Incremental(), new Differential(), new SlidingSnapshot()
                }, IRevisionChecker.class, new IRevisionChecker[] {
                    // Checker for FullDump
                    new IRevisionChecker() {
                        @Override
                        public void checkCompleteBuckets(NodeBucket pComplete, NodeBucket[] pFragments) {
                            // Check only the last version since the complete dump consists out of the last
                            // version within the FullDump
                            for (int i = 0; i < pComplete.getNodes().length; i++) {
                                assertEquals("Check for FullDump failed.", pFragments[0].getNode(i),
                                    pComplete.getNode(i));
                            }
                        }

                        @Override
                        public void checkCompleteBucketsForModification(LogValue pComplete,
                            NodeBucket[] pFragments, boolean pFullDump) {
                            // must always be true since it is the Fulldump
                            assertTrue(pFullDump);
                            // Check only the last version since the complete dump consists out of the last
                            // version within the FullDump
                            NodeBucket complete = (NodeBucket)pComplete.getComplete();
                            NodeBucket modified = (NodeBucket)pComplete.getModified();
                            for (int i = 0; i < complete.getNodes().length; i++) {
                                assertEquals("Check for FullDump failed.", pFragments[0].getNode(i), complete
                                    .getNode(i));
                                assertEquals("Check for FullDump failed.", pFragments[0].getNode(i), modified
                                    .getNode(i));
                            }

                        }
                    },
                    // Checker for Incremental
                    new IRevisionChecker() {
                        @Override
                        public void checkCompleteBuckets(NodeBucket pComplete, NodeBucket[] pFragments) {
                            // Incrementally iterate through all buckets to reconstruct the complete bucket.
                            int j = 0;
                            // taking first the fragments into account and..
                            for (int i = 0; i < pFragments.length - 1; i++) {
                                for (j = i * 2; j < (i * 2) + 2; j++) {
                                    assertEquals("Check for Incremental failed.", pFragments[i].getNode(j),
                                        pComplete.getNode(j));
                                }
                            }
                            // ...fill the test up with the rest
                            for (; j < pComplete.getNodes().length; j++) {
                                assertEquals("Check for Incremental failed.",
                                    pFragments[pFragments.length - 1].getNode(j), pComplete.getNode(j));
                            }
                        }

                        @Override
                        public void checkCompleteBucketsForModification(LogValue pComplete,
                            NodeBucket[] pFragments, boolean pFullDump) {
                            NodeBucket complete = (NodeBucket)pComplete.getComplete();
                            NodeBucket modified = (NodeBucket)pComplete.getModified();
                            int j = 0;
                            // taking first the fragments into account and..
                            for (int i = 0; i < pFragments.length - 1; i++) {
                                for (j = i * 2; j < (i * 2) + 2; j++) {
                                    assertEquals("Check for Incremental failed.", pFragments[i].getNode(j),
                                        complete.getNode(j));
                                    if (pFullDump) {
                                        assertEquals("Check for Incremental failed.", pFragments[i]
                                            .getNode(j), modified.getNode(j));
                                    } else {
                                        assertNull(modified.getNode(j));
                                    }
                                }
                            }
                            // ...fill the test up with the rest
                            for (; j < complete.getNodes().length; j++) {
                                assertEquals("Check for Incremental failed.",
                                    pFragments[pFragments.length - 1].getNode(j), complete.getNode(j));
                                if (pFullDump) {
                                    assertEquals("Check for Incremental failed.",
                                        pFragments[pFragments.length - 1].getNode(j), modified.getNode(j));
                                } else {
                                    assertNull(modified.getNode(j));
                                }
                            }

                        }
                    }// Checker for Differential
                    , new IRevisionChecker() {
                        @Override
                        public void checkCompleteBuckets(NodeBucket pComplete, NodeBucket[] pFragments) {
                            int j = 0;
                            // Take the last version first, to get the data out there...
                            for (j = 0; j < 32; j++) {
                                assertEquals("Check for Differential failed.", pFragments[0].getNode(j),
                                    pComplete.getNode(j));
                            }
                            // ...and iterate through the first version afterwards for the rest of the
                            // reconstruction
                            for (; j < pComplete.getNodes().length; j++) {
                                assertEquals(new StringBuilder("Check for Differential: ").append(" failed.")
                                    .toString(), pFragments[pFragments.length - 1].getNode(j), pComplete
                                    .getNode(j));
                            }
                        }

                        @Override
                        public void checkCompleteBucketsForModification(LogValue pComplete,
                            NodeBucket[] pFragments, boolean pFullDump) {
                            NodeBucket complete = (NodeBucket)pComplete.getComplete();
                            NodeBucket modified = (NodeBucket)pComplete.getModified();
                            int j = 0;
                            // Take the last version first, to get the data out there...
                            for (j = 0; j < 32; j++) {
                                assertEquals("Check for Differential failed.", pFragments[0].getNode(j),
                                    complete.getNode(j));
                                assertEquals("Check for Differential failed.", pFragments[0].getNode(j),
                                    modified.getNode(j));
                            }
                            // ...and iterate through the first version afterwards for the rest of the
                            // reconstruction
                            for (; j < complete.getNodes().length; j++) {
                                assertEquals("Check for Differential failed.", pFragments[1].getNode(j),
                                    complete.getNode(j));
                                if (pFullDump) {
                                    assertEquals("Check for Differential failed.", pFragments[1].getNode(j),
                                        modified.getNode(j));
                                } else {
                                    assertNull(modified.getNode(j));
                                }
                            }
                        }
                    },// check for Sliding Snapshot
                    new IRevisionChecker() {
                        @Override
                        public void checkCompleteBuckets(NodeBucket pComplete, NodeBucket[] pFragments) {
                            for (int i = 0; i < pFragments.length; i++) {
                                for (int j = i * 2; j < (i * 2) + 2; j++) {
                                    assertEquals("Check for Sliding Snapshot failed.", pFragments[i]
                                        .getNode(j), pComplete.getNode(j));
                                }
                            }
                        }

                        @Override
                        public void checkCompleteBucketsForModification(LogValue pComplete,
                            NodeBucket[] pFragments, boolean fullDump) {
                            NodeBucket complete = (NodeBucket)pComplete.getComplete();
                            NodeBucket modified = (NodeBucket)pComplete.getModified();
                            int j = 0;
                            // Taking all fragments in the middle, only checking against
                            // complete-fragment and..
                            for (int i = 0; i < pFragments.length - 1; i++) {
                                for (j = i * 2; j < (i * 2) + 2; j++) {
                                    assertEquals("Check for Sliding Snapshot failed.", pFragments[i]
                                        .getNode(j), complete.getNode(j));
                                }
                            }
                            // ..at last, checking the last fragment, against write- and read-fragment
                            for (; j < complete.getNodes().length; j++) {
                                assertEquals("Check for Sliding Snapshot failed.",
                                    pFragments[pFragments.length - 1].getNode(j), complete.getNode(j));
                                assertEquals("Check for Sliding Snapshot failed.",
                                    pFragments[pFragments.length - 1].getNode(j), modified.getNode(j));
                            }

                        }

                    }
                }, INodeBucketGenerator.class, new INodeBucketGenerator[] {
                    // Checker for FullDump
                    new INodeBucketGenerator() {
                        @Override
                        public NodeBucket[] generateNodeBuckets() {
                            NodeBucket[] returnVal = {
                                getNodeBucket(0, IConstants.CONTENT_COUNT, 0, -1)
                            };
                            return returnVal;
                        }
                    },
                    // Checker for Incremental
                    new INodeBucketGenerator() {
                        @Override
                        public NodeBucket[] generateNodeBuckets() {
                            // initialize all fragments first...
                            final NodeBucket[] buckets = new NodeBucket[63];
                            // fill all buckets up to number of restores first...
                            for (int j = 0; j < 62; j++) {
                                // filling nodebuckets from end to start with 2 elements each slot
                                buckets[j] =
                                    getNodeBucket(j * 2, (j * 2) + 2, buckets.length - j - 1, buckets.length - j
                                        - 2);
                            }
                            // set a fulldump as last revision
                            buckets[62] = getNodeBucket(0, 128, 0, -1);
                            return buckets;
                        }
                    },
                    // Checker for Differential
                    new INodeBucketGenerator() {
                        @Override
                        public NodeBucket[] generateNodeBuckets() {
                            // initialize all fragments first...
                            final NodeBucket[] buckets = new NodeBucket[2];
                            // setting one buckets to a fragment only...
                            buckets[0] = getNodeBucket(0, 32, 0, -1);
                            // ..and the other as entire fulldump
                            buckets[1] = getNodeBucket(0, 128, 1, 0);
                            return buckets;
                        }

                    },
                    // Checker for Sliding Snapshot
                    new INodeBucketGenerator() {
                        @Override
                        public NodeBucket[] generateNodeBuckets() {
                            // initialize all fragments first...
                            final NodeBucket[] buckets = new NodeBucket[64];
                            // fill all buckets up to number of restores first...
                            for (int j = 0; j < 64; j++) {
                                // filling nodebuckets from end to start with 2 elements each slot
                                buckets[j] =
                                    getNodeBucket(j * 2, (j * 2) + 2, buckets.length - j - 1, buckets.length - j
                                        - 2);
                            }
                            return buckets;
                        }
                    }
                }
            }
        };
        return returnVal;
    }

    /**
     * Interface to check reconstructed buckets.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IRevisionChecker {
        void checkCompleteBuckets(NodeBucket pComplete, NodeBucket[] pFragments);

        void checkCompleteBucketsForModification(LogValue pComplete, NodeBucket[] pFragments, boolean fullDump);
    }

    /**
     * Node Bucket Generator for new NodeBuckets.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface INodeBucketGenerator {
        NodeBucket[] generateNodeBuckets();
    }

}
