/**
 * 
 */
package org.treetank.page;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/**
 * Test Case for NamePage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NamePageTest {

    private final static Random mRan = new Random();

    /**
     * Test method for {@link org.treetank.page.NamePage#NamePage(long)} and
     * {@link org.treetank.page.NamePage#getByteRepresentation()}.
     */
    @Test
    public void testNamePageByteArray() {
        final NamePage freshPage = new NamePage(0);
        freshPage.setName(mRan.nextInt(), "bla");
        freshPage.setName(mRan.nextInt(), "blubb");
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final NamePage serializedPage = (NamePage) PageFactory.getInstance(
                FactoriesForTest.INSTANCE).deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes,
                serializedPage.getByteRepresentation()));
    }
}