/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.page.DumbMetaEntryFactory.DumbKey;
import org.treetank.page.DumbMetaEntryFactory.DumbValue;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
     * @param pPages
     *            different pages
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiatePages")
    public void testByteRepresentation(Class<IPage> clazz, IPage[] pPages) throws TTIOException {
        final PageFactory fac = new PageFactory(new DumbNodeFactory(), new DumbMetaEntryFactory());

        for (final IPage page : pPages) {
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            page.serialize(output);
            byte[] firstSerialized = output.toByteArray();
            ByteArrayDataInput input = ByteStreams.newDataInput(firstSerialized);

            final IPage serializedPage = fac.deserializePage(input);
            output = ByteStreams.newDataOutput();
            serializedPage.serialize(output);
            byte[] secondSerialized = output.toByteArray();
            assertTrue(new StringBuilder("Check for ").append(page.getClass()).append(" failed.").toString(),
                Arrays.equals(firstSerialized, secondSerialized));

        }
    }

    /**
     * Test method for {@link org.treetank.page.interfaces.IPage} and
     * {@link org.treetank.page.interfaces.IPage#getByteRepresentation()}.
     * 
     * @param clazz
     *            IPage as class
     * @param pPages
     *            different pages
     * @throws TTIOException
     */
    @Test(dataProvider = "instantiatePages")
    public void testEqualsAndHashCode(Class<IPage> clazz, IPage[] pPages) throws TTIOException {

        for (int i = 0; i < pPages.length; i++) {
            IPage onePage = pPages[i % pPages.length];
            IPage secondPage = pPages[(i + 1) % pPages.length];
            assertEquals(onePage.hashCode(), onePage.hashCode());
            assertNotSame(onePage.hashCode(), secondPage.hashCode());
            assertTrue(onePage.equals(onePage));
            assertFalse(onePage.equals(secondPage));
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
        // UberPage setup
        UberPage uberPage =
            new UberPage(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                CoreTestHelper.random.nextLong());
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
        // MetaPage setup
        MetaPage metaPage = new MetaPage(CoreTestHelper.random.nextLong());
        metaPage.setEntry(new DumbKey(CoreTestHelper.random.nextLong()), new DumbValue(CoreTestHelper.random
            .nextLong()));

        Object[][] returnVal = {
            {
                IPage.class, new IPage[] {
                    indirectPage, revRootPage, nodePage, metaPage, uberPage
                }
            }
        };
        return returnVal;
    }
}
