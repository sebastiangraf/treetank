/**
 * 
 */
package org.treetank.revisioning;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.CoreTestHelper.getNodePage;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ContructorProps;
import org.treetank.exception.TTByteHandleException;
import org.treetank.page.NodePage;
import org.treetank.page.RevisionRootPage;

/**
 * Test for {@link IRevisioning}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IRevisioningTest {

    // Parameter to fix the number needed to restore one entire status
    private final static int REVTORESTORE = 4;
    // overhead to be skipped since the entire revision should already be complete after
    // REVTORESTORE-revisions
    private final static int OVERHEAD = 1;

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
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testCompletePages(Class<IRevisioning> pRevisioningClass, IRevisioning[] pRevisioning,
        Class<IRevisionChecker> pRevisionCheckerClass, IRevisionChecker[] pRevisionChecker) {
        for (final IRevisioning handler : pRevisioning) {
            final NodePage[] pages = new NodePage[REVTORESTORE + OVERHEAD];
            for (int i = 0; i < pages.length; i++) {
                pages[i] = getNodePage(0, 128, i);
            }

            final NodePage page = handler.combinePages(pages);

            for (int j = 0; j < page.getNodes().length; j++) {
                assertEquals(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                    .toString(), pages[0].getNode(j), page.getNode(j));
            }
        }
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
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testFragmentedPages(Class<IRevisioning> pRevisioningClass, IRevisioning[] pRevisioning,
        Class<IRevisionChecker> pRevisionCheckerClass, IRevisionChecker[] pRevisionChecker) {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pRevisioning.length, pRevisionChecker.length);

        for (int i = 0; i < pRevisioning.length; i++) {
            // initialize all framents first...
            final NodePage[] pages = new NodePage[REVTORESTORE + OVERHEAD];
            // fill all pages up to number of restores first...
            for (int j = 0; j < REVTORESTORE - 1; j++) {
                // filling nodepages from end to start with 32 elements each slot
                pages[j] = getNodePage(j * 32, (j * 32) + 32, pages.length - j - 1);
            }
            // ...set the full-dump on the parameter of number of restores..
            pages[REVTORESTORE - 1] = getNodePage(0, 128, REVTORESTORE - 1);
            // ...then generate the overhead...
            int k = 0;
            for (int j = REVTORESTORE; j < REVTORESTORE + OVERHEAD; j++) {
                // filling nodepages from end to start with 32 elements each slot
                pages[j] = getNodePage(k * 32, (k * 32) + 32, k);
                k++;
            }

            // ..and recombine them...
            final NodePage page = pRevisioning[i].combinePages(pages);
            // ...and check them suitable to the versioning approach
            pRevisionChecker[i].checkRevisions(page, pages);
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
        Properties props = new Properties();
        props.put(ContructorProps.NUMBERTORESTORE, Integer.toString(REVTORESTORE));

        Object[][] returnVal = {
            {
                IRevisioning.class, new IRevisioning[] {
                    new FullDump(props), new Incremental(props), new Differential(props)
                }, IRevisionChecker.class, new IRevisionChecker[] {
                    // Checker for FullDump
                    new IRevisionChecker() {
                        @Override
                        public void checkRevisions(NodePage pComplete, NodePage[] pFragments) {
                            // Check only the last version since the complete dump consists out of the last
                            // version within the FullDump
                            for (int i = 0; i < pComplete.getNodes().length; i++) {
                                assertEquals("Check for FullDump failed.", pFragments[0].getNode(i),
                                    pComplete.getNode(i));
                            }
                        }
                    },
                    // Checker for Incremental
                    new IRevisionChecker() {
                        @Override
                        public void checkRevisions(NodePage pComplete, NodePage[] pFragments) {
                            // Incrementally iterate through all pages to reconstruct the complete page.
                            for (int i = 0; i < REVTORESTORE; i++) {
                                for (int j = i * 32; j < (i * 32) + 32; j++) {
                                    assertEquals("Check for Incremental failed.", pFragments[i].getNode(j),
                                        pComplete.getNode(j));
                                }
                            }
                        }
                    }// Checker for Differential
                    , new IRevisionChecker() {
                        @Override
                        public void checkRevisions(NodePage pComplete, NodePage[] pFragments) {
                            // Take the last version first, to get the data out there...
                            for (int j = 0; j < 32; j++) {
                                assertEquals("Check for Incremental failed.", pFragments[0].getNode(j),
                                    pComplete.getNode(j));
                            }
                            // ...and iterate through the first version afterwards for the rest of the
                            // reconstruction
                            for (int j = 32; j < pComplete.getNodes().length; j++) {
                                assertEquals(new StringBuilder("Check for Incremental: ").append(" failed.")
                                    .toString(), pFragments[pFragments.length - 1].getNode(j), pComplete
                                    .getNode(j));
                            }
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
        void checkRevisions(NodePage pComplete, NodePage[] pFragments);

    }

}
