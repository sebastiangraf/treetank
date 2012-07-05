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

/**
 * Test Case for Indirectpage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IndirectPageTest {

    @BeforeMethod
    public void setUp() {
        PageFactory.registerNewInstance(FactoriesForTest.INSTANCE);
    }

    @AfterMethod
    public void tearDown() {
        PageFactory.registerNewInstance(new NodeFactory());
    }

    /**
     * Test method for {@link org.treetank.page.IndirectPage#IndirectPage(long)} and
     * {@link org.treetank.page.IndirectPage#getByteRepresentation()}.
     */
    @Test
    public void testIndirectPageByteArray() {
        final IndirectPage freshPage = new IndirectPage(0);
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final IndirectPage serializedPage =
            (IndirectPage)PageFactory.getInstance().deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
