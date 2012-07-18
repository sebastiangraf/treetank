/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.DumpFactoryModule;

import com.google.inject.Inject;

/**
 * Test Case for RevisionRootPage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(modules = DumpFactoryModule.class)
public class RevisionRootPageTest {

    @Inject
    private PageFactory mFac;

    /**
     * Test method for {@link org.treetank.page.RevisionRootPage#RevisionRootPage(byte[])} and
     * {@link org.treetank.page.RevisionRootPage#getByteRepresentation()}.
     */
    @Test
    public void testRevisionPageByteArray() {
        final RevisionRootPage freshPage = new RevisionRootPage(IConstants.UBP_ROOT_REVISION_NUMBER);
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final RevisionRootPage serializedPage = (RevisionRootPage)mFac.deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
