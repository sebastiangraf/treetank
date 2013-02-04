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

import java.io.File;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.NodePage;
import org.treetank.page.interfaces.IPage;

import com.google.inject.Inject;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class LRULogTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private NodePage[][] mPages;

    private LRULog cache;

    private static final int LEVEL = 100;
    private static final int ELEMENTS = 100;

    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.getDatabase(CoreTestHelper.PATHS.PATH1.getFile());
        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        ResourceConfiguration conf = mResourceConfig.create(props);
        CoreTestHelper.createResource(conf);
        cache =
            new LRULog(new File(new File(CoreTestHelper.PATHS.PATH1.getFile(),
                StorageConfiguration.Paths.Data.getFile().getName()), CoreTestHelper.RESOURCENAME),
                conf.mNodeFac, conf.mMetaFac);

        mPages = new NodePage[LEVEL][ELEMENTS];
        insertData();
    }

    @Test
    public void testSimpleInsert() throws TTIOException {
        // testing for elements
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = cache.get(toRetrieve);
                final IPage current = cont.getComplete();
                assertEquals(mPages[i][j], current);
            }
        }
    }

    @Test
    public void testClearAndNull() throws TTIOException {
        // testing for null
        LogValue nullValue = cache.get(new LogKey(true, -1, -1));
        assertNull(nullValue);
        LogValue value = cache.get(new LogKey(true, 0, 0));
        assertNotNull(value);
        cache.clear();
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = cache.get(toRetrieve);
                assertNull(cont);
            }
        }
    }

    @Test
    public void testClearAndReInsert() throws TTIOException {
        // testing for clear
        cache.clear();
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = cache.get(toRetrieve);
                assertNull(cont);
            }
        }

        // inserting data again
        insertData();
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = cache.get(toRetrieve);
                final IPage current = cont.getComplete();
                assertEquals(mPages[i][j], current);
            }
        }
    }

    private void insertData() throws TTIOException {
        for (int i = 0; i < mPages.length; i++) {
            for (int j = 0; j < mPages[i].length; j++) {
                LogKey toStore = new LogKey(true, i, j);
                mPages[i][j] = CoreTestHelper.getNodePage(0, 0, CoreTestHelper.random.nextLong());
                cache.put(toStore, new LogValue(mPages[i][j], mPages[i][j]));
            }
        }
    }

}
