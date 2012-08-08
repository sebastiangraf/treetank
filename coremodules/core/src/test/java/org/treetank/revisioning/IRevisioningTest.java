/**
 * 
 */
package org.treetank.revisioning;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.TestHelper.getNodePage;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.page.NodePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IRevisioningTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        TestHelper.deleteEverything();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.revisioning.IRevisioning#combinePages(org.treetank.page.NodePage[])}.
     * This test just takes two versions and checks if the version-counter is interpreted correctly.
     * 
     * @param clazz
     *            to check revisioning approaches
     * @param pHandlers
     *            the different revisioning approaches
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testCompletePages(Class<IRevisioning> clazz, IRevisioning[] pHandlers) {
        for (final IRevisioning handler : pHandlers) {
            final NodePage[] pages = new NodePage[2];
            pages[0] = getNodePage(1, 0, 128, 0);
            pages[1] = getNodePage(0, 0, 128, 0);

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
     * @param clazz
     *            to check revisioning approaches
     * @param pHandlers
     *            the different revisioning approaches
     */
    @Test(dataProvider = "instantiateVersioning")
    public void testFragmentedPages(Class<IRevisioning> clazz, IRevisioning[] pHandlers) {
        for (final IRevisioning handler : pHandlers) {
            NodePage[] pages = prepareNormal(4);

            final NodePage page = handler.combinePages(pages);
            for (int i = 0; i < pages.length; i++) {
                for (int j = i * 32; j < (i * 32) + 32; j++) {
                    assertEquals(new StringBuilder("Check for ").append(handler.getClass())
                        .append(" failed.").toString(), pages[i].getNode(j), page.getNode(j));
                }
            }
        }
    }

    /**
     * Providing different implementations of the {@link IRevisioning} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IByteHandler}
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateVersioning")
    public Object[][] instantiateVersioning() throws TTByteHandleException {
        Object[][] returnVal = {
            {
                IRevisioning.class, new IRevisioning[] {
                    new FullDump(), new Incremental(4), new Differential(4)
                }
            }
        };
        return returnVal;
    }

    private static NodePage[] prepareNormal(final int length) {
        final NodePage[] pages = new NodePage[length];
        // filling one entire page with revision 0 and key 0
        pages[pages.length - 1] = getNodePage(0, 0, 128, 0);
        for (int i = 0; i < pages.length - 1; i++) {
            // filling nodepages from end to start with 32 elements each slot
            pages[i] = getNodePage(pages.length - i - 1, i * 32, (i * 32) + 32, 0);
        }
        return pages;
    }

}
