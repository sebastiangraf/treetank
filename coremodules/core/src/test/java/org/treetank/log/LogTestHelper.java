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
package org.treetank.log;

import org.treetank.CoreTestHelper;
import org.treetank.exception.TTException;
import org.treetank.page.NodePage;

/**
 * Helper class for testing the cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LogTestHelper {

    // private final static int VERSIONSTORESTORE = 100;

    protected static NodePage[][] PAGES;

    public static void setUp(boolean overflow, final BerkeleyPersistenceLog cache) throws TTException {
        if (overflow) {
            PAGES = new NodePage[LRULog.CACHE_CAPACITY / 5][LRULog.CACHE_CAPACITY / 5];
        } else {
            PAGES = new NodePage[LRULog.CACHE_CAPACITY / 10][LRULog.CACHE_CAPACITY / 10];
        }
        for (int i = 0; i < PAGES.length; i++) {
            for (int j = 0; j < PAGES[i].length; j++) {

                LogKey toStore = new LogKey(true, i, j);
                PAGES[i][j] = CoreTestHelper.getNodePage(0, 0, CoreTestHelper.random.nextLong());
                cache.put(toStore, new LogValue(PAGES[i][j], PAGES[i][j]));
            }
        }
    }

    public static void setUp(boolean overflow, final LRULog cache) throws TTException {
        if (overflow) {
            PAGES = new NodePage[LRULog.CACHE_CAPACITY / 5][LRULog.CACHE_CAPACITY / 5];
        } else {
            PAGES = new NodePage[LRULog.CACHE_CAPACITY / 10][LRULog.CACHE_CAPACITY / 10];
        }
        for (int i = 0; i < PAGES.length; i++) {
            for (int j = 0; j < PAGES[i].length; j++) {

                LogKey toStore = new LogKey(true, i, j);
                PAGES[i][j] = CoreTestHelper.getNodePage(0, 0, CoreTestHelper.random.nextLong());
                cache.put(toStore, new LogValue(PAGES[i][j], PAGES[i][j]));
            }
        }
    }
}
