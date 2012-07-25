/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.DumpFactoryModule;
import org.treetank.TestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.Inject;

/**
 * Test class for all classes implementing the {@link IPage} interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(modules = DumpFactoryModule.class)
public class IPageTest {

    @Inject
    private PageFactory mFac;

    /**
     * Test method for {@link org.treetank.page.IPage#IPage(long)} and
     * {@link org.treetank.page.IPage#getByteRepresentation()}.
     * 
     * @param clazz
     *            IPage as class
     * @param pHandlers
     *            different pages
     */
    @Test(dataProvider = "instantiatePages")
    public void testByteRepresentation(Class<IRevisioning> clazz, IPage[] pHandlers) {
        for (final IPage handler : pHandlers) {
            final byte[] pageBytes = handler.getByteRepresentation();

            final IPage serializedPage = mFac.deserializePage(pageBytes);
            assertTrue(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                .toString(), Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
        }
    }

    /**
     * Providing different implementations of the {@link IPage} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IByteHandler}
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiatePages")
    public Object[][] instantiatePages() throws TTByteHandleException {
        // IndirectPage setup
        IndirectPage indirectPage = new IndirectPage(TestHelper.random.nextLong());
        // RevisionRootPage setup
        RevisionRootPage revRootPage = new RevisionRootPage(TestHelper.random.nextLong());
        // NodePage setup
        NodePage nodePage = new NodePage(TestHelper.random.nextLong(), TestHelper.random.nextLong());
        for (int i = 0; i < IConstants.NDP_NODE_COUNT - 1; i++) {
            nodePage.setNode(i, DumbNodeFactory.generateOne());
        }
        // NamePage setup
        NamePage namePage = new NamePage(TestHelper.random.nextLong());
        namePage.setName(TestHelper.random.nextInt(), new String(TestHelper.generateRandomBytes(256)));

        Object[][] returnVal = {
            {
                IPage.class, new IPage[] {
                    indirectPage, revRootPage, nodePage, namePage
                }
            }
        };
        return returnVal;
    }
}
