/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.node.NodeFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * Test Case for NamePage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NamePageTest {

    private final static Random mRan = new Random();

    @BeforeMethod
    public void setUp() {
        PageFactory.registerNewInstance(new PageFactory(FactoriesForTest.INSTANCE));
    }

    @AfterMethod
    public void tearDown() {
        PageFactory.registerNewInstance(new PageFactory(new NodeFactory()));
    }

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

        final NamePage serializedPage = (NamePage)PageFactory.getInstance().deserializePage(pageBytes);

        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
