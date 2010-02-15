package com.treetank.settings;

import static com.treetank.TestHelper.getNodePage;
import static com.treetank.TestHelper.random;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.page.NodePage;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.ERevisioning;

public class ERevisioningTest {

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testDifferentialCombinePages() {
        final NodePage[] pages = prepareNormal(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);
    }

    @Test
    public void testDifferentialCombineOverlappingPages() {
        final NodePage[] pages = prepareOverlapping(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);
    }

    @Test
    public void testIncrementalCombinePages() {
        final NodePage[] pages = prepareNormal(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);
    }

    @Test
    public void testIncrementalCombineOverlappingPages() {
        final NodePage[] pages = prepareOverlapping(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);
    }

    @Test
    public void testSnapshotCombinePages() {
        final NodePage[] pages = prepareNormal(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);
    }

    @Test
    public void testSnapshotCombineOverlappingPages() {
        final NodePage[] pages = prepareOverlapping(4);
        final NodePage page = ERevisioning.SLIDING_SNAPSHOT.combinePages(pages,
                Integer.parseInt(EDatabaseSetting.MILESTONE_REVISION
                        .getStandardProperty()));
        checkCombined(pages, page);

    }

    private static NodePage[] prepareNormal(final int length) {
        final NodePage[] pages = new NodePage[length];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = getNodePage(pages.length - i - 1, i * 32, (i * 32) + 32);
        }
        return pages;
    }

    private static NodePage[] prepareOverlapping(final int length) {
        final NodePage[] pages = new NodePage[length];
        final int[] borders = new int[4];
        pages[0] = getNodePage(0, 0, 32);
        for (int i = 1; i < pages.length; i++) {
            borders[i] = random.nextInt(32) + ((i - 1) * 32);
            pages[i] = getNodePage(pages.length - i - 1, borders[i],
                    (i * 32) + 32);
        }
        return pages;

    }

    private static void checkCombined(final NodePage[] toCheck,
            final NodePage page) {
        for (int i = 0; i < toCheck.length; i++) {
            for (int j = i * 32; j < (i * 32) + 32; j++) {
                assertEquals(toCheck[i].getNode(j), page.getNode(j));
            }
        }
    }

}
