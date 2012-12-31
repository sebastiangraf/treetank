/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.page.interfaces.IPage;

/**
 * Test class for all classes implementing the {@link IPage} interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IPageTest {

    /**
     * Test method for {@link org.treetank.page.interfaces.IPage} and
     * {@link org.treetank.page.interfaces.IPage#getByteRepresentation()}.
     * 
     * @param clazz
     *            IPage as class
     * @param pHandlers
     *            different pages
     */
    @Test(dataProvider = "instantiatePages")
    public void testByteRepresentation(Class<IPage> clazz, IPage[] pHandlers) {
        final PageFactory fac = new PageFactory(new DumbNodeFactory());

        for (final IPage handler : pHandlers) {
            final byte[] pageBytes = handler.getByteRepresentation();

            final IPage serializedPage = fac.deserializePage(pageBytes);
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
        IndirectPage indirectPage = new IndirectPage(CoreTestHelper.random.nextLong());
        // RevisionRootPage setup
        RevisionRootPage revRootPage =
            new RevisionRootPage(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
        // NodePage setup
        NodePage nodePage = new NodePage(CoreTestHelper.random.nextLong());
        for (int i = 0; i < IConstants.CONTENT_COUNT - 1; i++) {
            nodePage.setNode(i, CoreTestHelper.generateOne());
        }
        // NamePage setup
        NamePage namePage = new NamePage(CoreTestHelper.random.nextLong());
        namePage.setName(CoreTestHelper.random.nextInt(), new String(CoreTestHelper.generateRandomBytes(256)));

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
