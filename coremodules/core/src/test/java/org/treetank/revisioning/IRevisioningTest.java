/**
 * 
 */
package org.treetank.revisioning;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.treetank.CoreTestHelper.getNodePage;

import java.util.Arrays;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.log.LogValue;
import org.treetank.page.IConstants;
import org.treetank.page.NodePage;

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
     * {@link org.treetank.revisioning.IRevisioning#combinePages(org.treetank.page.NodePage[])}.
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
    public void testCombinePagesForModification(Class<IRevisioning> pRevisioningClass,
        IRevisioning[] pRevisioning, Class<IRevisionChecker> pRevisionCheckerClass,
        IRevisionChecker[] pRevisionChecker, Class<INodePageGenerator> pNodeGeneratorClass,
        INodePageGenerator[] pNodeGenerator) {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevisionChecker.length);
        assertEquals(pRevisioning.length, pNodeGenerator.length);

        // test for full-dumps-including versionings
        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...check if revision is not SlidingSnapshot (since SlidingSnapshot is not working with entire
            // full-dump...
            if (!(pRevisioning[i] instanceof SlidingSnapshot)) {
                // ...get the node pages for not full-dump test and...
                final NodePage[] pages = pNodeGenerator[i].generateNodePages();
                // ..recombine them...
                final LogValue page =
                    pRevisioning[i].combinePagesForModification(pages.length, 0, pages, true);
                // ...and check them suitable to the versioning approach
                pRevisionChecker[i].checkCompletePagesForModification(page, pages, true);
            }
        }

        // test for non-full-dumps-including versionings
        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...check if revision is not FullDump (since FullDump must always be used within FullDump)
            // and...
            if (!(pRevisioning[i] instanceof FullDump)) {
                // ...get the node pages for full-dump test and...
                final NodePage[] pages = pNodeGenerator[i].generateNodePages();
                // ..recombine them...
                final LogValue page =
                    pRevisioning[i].combinePagesForModification(pages.length - 1, 0, pages, false);
                // ...and check them suitable to the versioning approach
                pRevisionChecker[i].checkCompletePagesForModification(page, pages, false);
            }
        }
    }

    /**
     * Test method for
     * {@link org.treetank.revisioning.IRevisioning#combinePagesForModification(long, NodePage[])}.
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
    public void testCombinePages(Class<IRevisioning> pRevisioningClass, IRevisioning[] pRevisioning,
        Class<IRevisionChecker> pRevisionCheckerClass, IRevisionChecker[] pRevisionChecker,
        Class<INodePageGenerator> pNodeGeneratorClass, INodePageGenerator[] pNodeGenerator) {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevisionChecker.length);
        assertEquals(pRevisioning.length, pNodeGenerator.length);

        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            // ...get the node pages and...
            final NodePage[] pages = pNodeGenerator[i].generateNodePages();
            // ..and recombine them...
            final NodePage page = pRevisioning[i].combinePages(pages);
            // ...and check them suitable to the versioning approach
            pRevisionChecker[i].checkCompletePages(page, pages);
        }
    }

    /**
     * Test method for
     * {@link org.treetank.revisioning.IRevisioning#getRevRootKeys(int, long, long, IBackendReader)}.
     * 
     * @param pRevisioningClass
     *            class for the revisioning approaches
     * @param pRevisioning
     *            the different revisioning approaches
     * @param pBackendReaderGeneratorClass
     *            class for the backend generator
     * @param pBackendReaderGenerator
     *            the different backend generators
     * @throws TTIOException
     * 
     */
    @Test(dataProvider = "initiateRevRootPages")
    public void testRevRootRetrieval(Class<IRevisioning> pRevisioningClass, IRevisioning[] pRevisioning,
        Class<ILoadRevRootChecker> pRevRootCheckerClass, ILoadRevRootChecker[] pRevRootChecker)
        throws TTIOException {
        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevRootChecker.length);

        int[] offsets = {
            0, 0, 0, 0, 127
        };
        IBackendReader backend = CoreTestHelper.getFakedStructure(offsets);

        // for all revision-approaches...
        for (int i = 0; i < pRevisioning.length; i++) {
            long[] revRoots = pRevisioning[i].getRevRootKeys(10, 1, 66, backend);
            pRevRootChecker[i].checkLoadRevRootNotFull(revRoots);

            revRoots = pRevisioning[i].getRevRootKeys(10, 1, 60, backend);
            pRevRootChecker[i].checkLoadRevRootFull(revRoots);
        }
    }

    /**
     * Providing different implementations of the {@link IRevisioning} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IRevisioning} and <code>IRevisionChecker</code>
     * @throws TTByteHandleException
     */
    @DataProvider(name = "initiateRevRootPages")
    public Object[][] initiateRevRootPages() throws TTByteHandleException {
        Object[][] returnVal = {
            {
                IRevisioning.class, new IRevisioning[] {
                    new FullDump(), new Incremental(), new Differential(), new SlidingSnapshot()
                }, ILoadRevRootChecker.class, new ILoadRevRootChecker[] {
                    // test for fulldump
                    new ILoadRevRootChecker() {
                        @Override
                        public void checkLoadRevRootNotFull(long[] revRoots) {
                            assertEquals(1, revRoots.length);
                            assertEquals(72, revRoots[0]);
                        }

                        @Override
                        public void checkLoadRevRootFull(long[] revRoots) {
                            assertEquals(1, revRoots.length);
                            assertEquals(66, revRoots[0]);
                        }
                    }, // test for incremental
                    new ILoadRevRootChecker() {
                        @Override
                        public void checkLoadRevRootNotFull(long[] revRoots) {
                            long[] revs = {
                                72, 71, 70, 69, 68, 67, 66
                            };
                            assertEquals(7, revRoots.length);
                            assertTrue(Arrays.equals(revs, revRoots));
                        }

                        @Override
                        public void checkLoadRevRootFull(long[] revRoots) {
                            assertEquals(1, revRoots.length);
                            assertEquals(66, revRoots[0]);
                        }
                    },// test for differential
                    new ILoadRevRootChecker() {
                        @Override
                        public void checkLoadRevRootNotFull(long[] revRoots) {
                            long[] revs = {
                                72, 66
                            };
                            assertEquals(2, revRoots.length);
                            assertTrue(Arrays.equals(revs, revRoots));
                        }

                        @Override
                        public void checkLoadRevRootFull(long[] revRoots) {
                            assertEquals(1, revRoots.length);
                            assertEquals(66, revRoots[0]);
                        }
                    },// test for sliding
                    new ILoadRevRootChecker() {
                        @Override
                        public void checkLoadRevRootNotFull(long[] revRoots) {
                            long[] revs = {
                                72, 71, 70, 69, 68, 67, 66, 65, 64, 63
                            };
                            assertEquals(10, revRoots.length);
                            assertTrue(Arrays.equals(revs, revRoots));
                        }

                        @Override
                        public void checkLoadRevRootFull(long[] revRoots) {
                            long[] revs = {
                                66, 65, 64, 63, 62, 61, 60, 59, 58, 57
                            };
                            assertEquals(10, revRoots.length);
                            assertTrue(Arrays.equals(revs, revRoots));
                        }
                    }
                }
            }
        };
        return returnVal;
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
                        public void checkCompletePages(NodePage pComplete, NodePage[] pFragments) {
                            // Check only the last version since the complete dump consists out of the last
                            // version within the FullDump
                            for (int i = 0; i < pComplete.getNodes().length; i++) {
                                assertEquals("Check for FullDump failed.", pFragments[0].getNode(i),
                                    pComplete.getNode(i));
                            }
                        }

                        @Override
                        public void checkCompletePagesForModification(LogValue pComplete,
                            NodePage[] pFragments, boolean pFullDump) {
                            // must always be true since it is the Fulldump
                            assertTrue(pFullDump);
                            // Check only the last version since the complete dump consists out of the last
                            // version within the FullDump
                            NodePage complete = (NodePage)pComplete.getComplete();
                            NodePage modified = (NodePage)pComplete.getModified();
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
                        public void checkCompletePages(NodePage pComplete, NodePage[] pFragments) {
                            // Incrementally iterate through all pages to reconstruct the complete page.
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
                        public void checkCompletePagesForModification(LogValue pComplete,
                            NodePage[] pFragments, boolean pFullDump) {
                            NodePage complete = (NodePage)pComplete.getComplete();
                            NodePage modified = (NodePage)pComplete.getModified();
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
                        public void checkCompletePages(NodePage pComplete, NodePage[] pFragments) {
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
                        public void checkCompletePagesForModification(LogValue pComplete,
                            NodePage[] pFragments, boolean pFullDump) {
                            NodePage complete = (NodePage)pComplete.getComplete();
                            NodePage modified = (NodePage)pComplete.getModified();
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
                        public void checkCompletePages(NodePage pComplete, NodePage[] pFragments) {
                            for (int i = 0; i < pFragments.length; i++) {
                                for (int j = i * 2; j < (i * 2) + 2; j++) {
                                    assertEquals("Check for Sliding Snapshot failed.", pFragments[i]
                                        .getNode(j), pComplete.getNode(j));
                                }
                            }
                        }

                        @Override
                        public void checkCompletePagesForModification(LogValue pComplete,
                            NodePage[] pFragments, boolean fullDump) {
                            NodePage complete = (NodePage)pComplete.getComplete();
                            NodePage modified = (NodePage)pComplete.getModified();
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
                }, INodePageGenerator.class, new INodePageGenerator[] {
                    // Checker for FullDump
                    new INodePageGenerator() {
                        @Override
                        public NodePage[] generateNodePages() {
                            NodePage[] returnVal = {
                                getNodePage(0, IConstants.CONTENT_COUNT, 0, -1)
                            };
                            return returnVal;
                        }
                    },
                    // Checker for Incremental
                    new INodePageGenerator() {
                        @Override
                        public NodePage[] generateNodePages() {
                            // initialize all fragments first...
                            final NodePage[] pages = new NodePage[63];
                            // fill all pages up to number of restores first...
                            for (int j = 0; j < 62; j++) {
                                // filling nodepages from end to start with 2 elements each slot
                                pages[j] = getNodePage(j * 2, (j * 2) + 2, pages.length - j - 1, pages.length - j - 2);
                            }
                            // set a fulldump as last revision
                            pages[62] = getNodePage(0, 128, 0, -1);
                            return pages;
                        }
                    },
                    // Checker for Differential
                    new INodePageGenerator() {
                        @Override
                        public NodePage[] generateNodePages() {
                            // initialize all fragments first...
                            final NodePage[] pages = new NodePage[2];
                            // setting one pages to a fragment only...
                            pages[0] = getNodePage(0, 32, 0, -1);
                            // ..and the other as entire fulldump
                            pages[1] = getNodePage(0, 128, 1, 0);
                            return pages;
                        }

                    },
                    // Checker for Sliding Snapshot
                    new INodePageGenerator() {
                        @Override
                        public NodePage[] generateNodePages() {
                            // initialize all fragments first...
                            final NodePage[] pages = new NodePage[64];
                            // fill all pages up to number of restores first...
                            for (int j = 0; j < 64; j++) {
                                // filling nodepages from end to start with 2 elements each slot
                                pages[j] = getNodePage(j * 2, (j * 2) + 2, pages.length - j - 1, pages.length - j - 2);
                            }
                            return pages;
                        }
                    }
                }
            }
        };
        return returnVal;
    }

    /**
     * Interface to check reconstructed pages.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IRevisionChecker {
        void checkCompletePages(NodePage pComplete, NodePage[] pFragments);

        void checkCompletePagesForModification(LogValue pComplete, NodePage[] pFragments, boolean fullDump);
    }

    /**
     * Node Page Generator for new NodePages.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface INodePageGenerator {
        NodePage[] generateNodePages();
    }

    /**
     * Interface to check loaded rev root pages.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface ILoadRevRootChecker {
        void checkLoadRevRootNotFull(long[] revRoots);

        void checkLoadRevRootFull(long[] revRoots);
    }

}
