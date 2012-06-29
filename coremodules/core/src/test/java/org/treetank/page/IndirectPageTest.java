/**
 * 
 */
package org.treetank.page;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

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
