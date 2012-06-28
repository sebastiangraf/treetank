/**
 * 
 */
package org.treetank.page;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * Test Case for RevisionRootPage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class RevisionRootPageTest {

    /**
     * Test method for
     * {@link org.treetank.page.RevisionRootPage#RevisionRootPage(byte[])} and
     * {@link org.treetank.page.RevisionRootPage#getByteRepresentation()}.
     */
    @Test
    public void testNodePageByteArray() {
        final RevisionRootPage freshPage = new RevisionRootPage();
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final RevisionRootPage serializedPage = (RevisionRootPage) PageFactory.getInstance(FactoriesForTest.INSTANCE)
                .deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes,
                serializedPage.getByteRepresentation()));
    }
}