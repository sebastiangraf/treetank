/**
 * 
 */
package org.treetank.page;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.DumpFactoryModule;
import org.treetank.FactoriesForTest;
import org.treetank.api.INode;

import com.google.inject.Inject;

/**
 * Test Case for NodePage.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(modules = DumpFactoryModule.class)
public class NodePageTest {

    @Inject
    private PageFactory mFac;

    /**
     * Test method for {@link org.treetank.page.NodePage#NodePage(byte[])} and
     * {@link org.treetank.page.NodePage#getByteRepresentation()}.
     */
    @Test
    public void testNodePageByteArray() {
        final NodePage freshPage = new NodePage(0, 0);
        List<INode> nodes = new ArrayList<INode>();
        for (int i = 0; i < IConstants.NDP_NODE_COUNT - 1; i++) {
            nodes.add(FactoriesForTest.generateOne());
            freshPage.setNode(i, nodes.get(i));
        }

        final byte[] pageBytes = freshPage.getByteRepresentation();
        final NodePage serializedPage = (NodePage)mFac.deserializePage(pageBytes);

        assertTrue(Arrays.equals(pageBytes, serializedPage.getByteRepresentation()));
    }

}
