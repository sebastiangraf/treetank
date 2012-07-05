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
 * Test Case for RevisionRootPage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class RevisionRootPageTest {

    @BeforeMethod
    public void setUp() {
        new PageFactory(FactoriesForTest.INSTANCE);
    }

    @AfterMethod
    public void tearDown() {
        new PageFactory(new NodeFactory());
    }

    /**
     * Test method for {@link org.treetank.page.RevisionRootPage#RevisionRootPage(byte[])} and
     * {@link org.treetank.page.RevisionRootPage#getByteRepresentation()}.
     */
    @Test
    public void testRevisionPageByteArray() {
        final RevisionRootPage freshPage = new RevisionRootPage(IConstants.UBP_ROOT_REVISION_NUMBER);
        final byte[] pageBytes = freshPage.getByteRepresentation();

        final RevisionRootPage serializedPage =
            (RevisionRootPage)PageFactory.getInstance().deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }
}
