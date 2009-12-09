package com.treetank.utils;

import static com.treetank.TestHelper.getNodePage;
import static com.treetank.TestHelper.random;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.constants.ERevisioning;
import com.treetank.constants.ESettable;
import com.treetank.page.NodePage;

public class SlidingSnapshotTest {

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    //
    // @Test
    // public void testGetRealWindowSize() {
    // assertEquals(1, SlidingSnapshot.getRealWindowSize(0));
    // assertEquals(2, SlidingSnapshot.getRealWindowSize(1));
    // assertEquals(
    // SettableProperties.SNAPSHOT_WINDOW.getStandardProperty(),
    // SlidingSnapshot
    // .getRealWindowSize((Integer) SettableProperties.SNAPSHOT_WINDOW
    // .getStandardProperty()));
    // assertEquals(
    // SettableProperties.SNAPSHOT_WINDOW.getStandardProperty(),
    // SlidingSnapshot
    // .getRealWindowSize((Integer) SettableProperties.SNAPSHOT_WINDOW
    // .getStandardProperty() + 20));
    // }

    @Test
    public void testCombinePages() {
        final NodePage[] pages = new NodePage[4];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = getNodePage(0, i * 32, (i * 32) + 32);
        }
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                (Integer) ESettable.MILESTONE_REVISION
                        .getStandardProperty());
        for (int i = 0; i < pages.length; i++) {
            for (int j = i * 32; j < (i * 32) + 32; j++) {
                assertEquals(pages[i].getNode(j), page.getNode(j));
            }
        }
    }

    @Test
    public void testCombineOverlappingPages() {
        final NodePage[] pages = new NodePage[4];
        final int[] borders = new int[4];
        pages[0] = getNodePage(0, 0, 32);
        for (int i = 1; i < pages.length; i++) {
            borders[i] = random.nextInt(32) + ((i - 1) * 32);
            pages[i] = getNodePage(0, borders[i], (i * 32) + 32);
        }
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                (Integer) ESettable.MILESTONE_REVISION
                        .getStandardProperty());
        for (int i = 0; i < pages.length; i++) {
            for (int j = i * 32; j < (i * 32) + 32; j++) {
                assertEquals(pages[i].getNode(j), page.getNode(j));
            }
        }
    }

}
