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

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.log.LogIterator;
import org.treetank.log.ILog;
import org.treetank.log.LRULog;
import org.treetank.log.LogValue;
import org.treetank.log.LogKey;
import org.treetank.page.interfaces.IPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LRULogTest {

    private ILog cache;

    @BeforeMethod
    public void setUp() throws TTException {
        cache = new LRULog(new NullCache());
        LogTestHelper.setUp(false, cache);
    }

    @Test
    public void test() throws TTIOException {
        for (int i = 1; i < LogTestHelper.PAGES.length; i++) {
            for (int j = 1; j < LogTestHelper.PAGES[i].length; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue<? extends IPage> cont = cache.get(toRetrieve);
                final IPage current = cont.getComplete();
                assertEquals(LogTestHelper.PAGES[i][j], current);
            }
        }

    }

    static class NullCache implements ILog {
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
        public LogValue<IPage> get(final LogKey mKey) {
            return null;
        }

        @Override
        public void put(final LogKey mKey, final LogValue<IPage> mPage) {
            // Not used over here
        }

        @Override
        public LogIterator getIterator() {
            // Not used over here
            return null;
        }

    }

}
