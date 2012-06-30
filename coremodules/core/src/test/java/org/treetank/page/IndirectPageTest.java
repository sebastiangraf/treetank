/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.Arrays;

/**
 * Test Case for Indirectpage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IndirectPageTest {

    /**
     * Test method for
     * {@link org.treetank.page.IndirectPage#IndirectPage(long)} and
     * {@link org.treetank.page.IndirectPage#getByteRepresentation()}.
     */
    @Test
    public void testIndirectPageByteArray() {
        final IndirectPage freshPage = new IndirectPage(0);
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final IndirectPage serializedPage = (IndirectPage) PageFactory.getInstance(FactoriesForTest.INSTANCE)
                .deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes,
                serializedPage.getByteRepresentation()));
    }
}
