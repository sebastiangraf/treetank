/**
 * 
 */
package org.treetank.revisioning;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.TestHelper.getNodePage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.TestHelper;
import org.treetank.page.NodePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDumpTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        TestHelper.deleteEverything();

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }

    /**
     * Test method for {@link org.treetank.revisioning.FullDump#combinePages(org.treetank.page.NodePage[])}
     * and {@link org.treetank.revisioning.FullDump#combinePagesForModification(org.treetank.page.NodePage[])}
     */
    @Test
    public void test() {
        final NodePage[] pages = new NodePage[2];
        pages[0] = getNodePage(1, 0, 128, 0);
        pages[1] = getNodePage(0, 0, 128, 0);

        final NodePage page = new FullDump().combinePages(pages);

        for (int j = 0; j < page.getNodes().length; j++) {
            assertEquals(pages[0].getNode(j), page.getNode(j));
        }
    }

}
