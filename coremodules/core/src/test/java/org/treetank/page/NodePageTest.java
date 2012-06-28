/**
 * 
 */
package org.treetank.page;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.treetank.api.INode;

/**
 * Test Case for NodePage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodePageTest {

    /**
     * Test method for {@link org.treetank.page.NodePage#NodePage(byte[])} and
     * {@link org.treetank.page.NodePage#getByteRepresentation()}.
     */
    @Test
    public void testNodePageByteArray() {
        final NodePage freshPage = new NodePage(0, 0);
        List<INode> nodes = new ArrayList<INode>();
        for (int i = 0; i < /*IConstants.NDP_NODE_COUNT*/2 - 1; i++) {
            nodes.add(FactoriesForTest.generateOne());
            freshPage.setNode(i, nodes.get(i));
        }

        final byte[] pageBytes = freshPage.getByteRepresentation();
        final NodePage serializedPage = (NodePage) PageFactory.getInstance(
                FactoriesForTest.INSTANCE).deserializePage(pageBytes);
        assertTrue(Arrays.equals(pageBytes,
                serializedPage.getByteRepresentation()));
    }

}