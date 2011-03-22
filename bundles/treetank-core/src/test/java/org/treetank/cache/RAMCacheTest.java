/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.treetank.cache.ICache;
import org.treetank.cache.LRUCache;
import org.treetank.cache.NodePageContainer;
import org.treetank.cache.RAMCache;
import org.treetank.page.NodePage;
import org.treetank.settings.EDatabaseSetting;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class RAMCacheTest {

    private final NodePage[][] pages = new NodePage[LRUCache.CACHE_CAPACITY + 1][Integer
        .parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty()) + 1];

    private ICache cache;

    @Before
    public void setUp() {
        cache = new RAMCache();
        for (int i = 0; i < pages.length; i++) {
            final NodePage page = new NodePage(i, 0);
            final NodePage[] revs =
                new NodePage[Integer.parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty())];

            for (int j = 0; j < Integer.parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty()); j++) {
                pages[i][j + 1] = new NodePage(i, 0);
                revs[j] = pages[i][j + 1];
            }
            pages[i][0] = page;
            cache.put(i, new NodePageContainer(page));
        }
    }

    @Test
    public void test() {
        boolean foundAtLeastOne = false;
        for (int i = 0; i < pages.length; i++) {
            if (cache.get(i) != null) {
                foundAtLeastOne = true;
            }
        }
        assertTrue(foundAtLeastOne);
        cache.clear();
        foundAtLeastOne = false;
        for (int i = 0; i < pages.length; i++) {
            if (cache.get(i) != null) {
                foundAtLeastOne = true;
            }
        }
        assertFalse(foundAtLeastOne);
    }

}
