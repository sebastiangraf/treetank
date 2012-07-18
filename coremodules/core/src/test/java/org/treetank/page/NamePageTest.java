/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.DumpFactoryModule;
import org.treetank.TestHelper;

import com.google.inject.Inject;

/**
 * Test Case for NamePage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(modules = DumpFactoryModule.class)
public class NamePageTest {

    @Inject
    private PageFactory mFac;

    /**
     * Test method for {@link org.treetank.page.NamePage#NamePage(long)} and
     * {@link org.treetank.page.NamePage#getByteRepresentation()}.
     */
    @Test
    public void testNamePageByteArray() {
        final NamePage freshPage = new NamePage(0);
        freshPage.setName(TestHelper.random.nextInt(), "bla");
        freshPage.setName(TestHelper.random.nextInt(), "blubb");
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final NamePage serializedPage = (NamePage)mFac.deserializePage(pageBytes);

        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
