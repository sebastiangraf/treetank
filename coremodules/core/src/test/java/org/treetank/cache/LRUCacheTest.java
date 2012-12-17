/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.interfaces.IPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LRUCacheTest {

    private ICachedLog cache;

    @BeforeMethod
    public void setUp() throws TTException {
        cache = new LRUCache(new NullCache());
        CacheTestHelper.setUp(cache);
    }

    @Test
    public void test() throws TTIOException {
        for (int i = 1; i < CacheTestHelper.PAGES.length; i++) {
            for (int j = 1; j < CacheTestHelper.PAGES[i].length; j++) {
                final NodePageContainer cont = cache.get(new LogKey(true, i, j));
                final IPage current = cont.getComplete();
                assertEquals(CacheTestHelper.PAGES[i][j][0], current);
            }
        }

        final NodePageContainer page = cache.get(new LogKey(true, 0, 0));
        assertNull(page);

    }

    static class NullCache implements ICachedLog {
        /**
         * Constructor.
         */
        public NullCache() {
            super();
        }

        @Override
        public void clear() {
            // Not used over here
        }

        @Override
        public NodePageContainer get(final LogKey mKey) {
            return null;
        }

        @Override
        public void put(final LogKey mKey, final NodePageContainer mPage) {
            // Not used over here
        }

        @Override
        public CacheLogIterator getIterator() {
            // Not used over here
            return null;
        }

    }

}
