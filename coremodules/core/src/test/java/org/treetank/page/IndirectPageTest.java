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
 * Test Case for Indirectpage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(modules = DumpFactoryModule.class)
public class IndirectPageTest {

    @Inject
    private PageFactory mFac;

    /**
     * Test method for {@link org.treetank.page.IndirectPage#IndirectPage(long)} and
     * {@link org.treetank.page.IndirectPage#getByteRepresentation()}.
     */
    @Test
    public void testIndirectPageByteArray() {
        final IndirectPage freshPage = new IndirectPage(0);
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final IndirectPage serializedPage = (IndirectPage)mFac.deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
