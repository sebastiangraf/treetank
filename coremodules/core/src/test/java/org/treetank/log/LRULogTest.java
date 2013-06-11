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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IOUtils;
import org.treetank.log.LRULog.LogIterator;

import com.google.inject.Inject;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class LRULogTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    private NodeBucket[][] mPages;
    private LRULog mCache;
    private Set<NodeBucket> mPageSet;

    private static final int LEVEL = 100;
    private static final int ELEMENTS = 100;

    @BeforeMethod
    public void setUp() throws TTException {
        IOUtils.recursiveDelete(CoreTestHelper.PATHS.PATH1.getFile());
        IOUtils.createFolderStructure(CoreTestHelper.PATHS.PATH1.getFile(), ResourceConfiguration.Paths
            .values());
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        props.setProperty(ConstructorProps.RESOURCEPATH, CoreTestHelper.PATHS.PATH1.getFile()
            .getAbsolutePath());
        mResource = mResourceConfig.create(props);
        mCache = new LRULog(CoreTestHelper.PATHS.PATH1.getFile(), mResource.mNodeFac, mResource.mMetaFac);

        mPages = new NodeBucket[LEVEL][ELEMENTS];
        insertData();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        mCache.close();
        IOUtils.recursiveDelete(CoreTestHelper.PATHS.PATH1.getFile());
    }

    @Test
    public void testSimpleInsert() throws TTIOException {
        checkContent();
    }

    @Test
    public void testClearAndNull() throws TTIOException {
        // testing for null
        LogValue nullValue = mCache.get(new LogKey(true, -1, -1));
        assertNull(nullValue.getModified());
        LogValue value = mCache.get(new LogKey(true, 0, 0));
        assertNotNull(value);
        mCache.close();
        mCache = new LRULog(CoreTestHelper.PATHS.PATH1.getFile(), mResource.mNodeFac, mResource.mMetaFac);
        checkNull();
    }

    @Test
    public void testClearAndReInsert() throws TTIOException {
        // testing for clear
        mCache.close();
        mCache = new LRULog(CoreTestHelper.PATHS.PATH1.getFile(), mResource.mNodeFac, mResource.mMetaFac);
        checkNull();

        // inserting data again
        insertData();
        checkContent();
    }

    private void checkContent() throws TTIOException {
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = mCache.get(toRetrieve);
                final IBucket current = cont.getComplete();
                assertEquals(mPages[i][j], current);
            }
        }
        LogIterator it = mCache.getIterator();
        for (LogValue val : it) {
            assertEquals(val.getComplete(), val.getModified());
            assertTrue(mPageSet.contains(val.getComplete()));
        }
    }

    private void checkNull() throws TTIOException {
        for (int i = 0; i < LEVEL; i++) {
            for (int j = 0; j < ELEMENTS; j++) {
                LogKey toRetrieve = new LogKey(true, i, j);
                final LogValue cont = mCache.get(toRetrieve);
                assertNull(cont.getModified());
            }
        }
    }

    private void insertData() throws TTIOException {
        mPageSet = new HashSet<NodeBucket>();
        long key = 0;
        for (int i = 0; i < mPages.length; i++) {
            for (int j = 0; j < mPages[i].length; j++) {
                LogKey toStore = new LogKey(true, i, j);
                mPages[i][j] = CoreTestHelper.getNodePage(0, IConstants.CONTENT_COUNT, key, key - 1);
                mCache.put(toStore, new LogValue(mPages[i][j], mPages[i][j]));
                mPageSet.add(mPages[i][j]);
                key++;
            }
        }
        assertEquals(LEVEL * ELEMENTS, mPageSet.size());
    }

}
