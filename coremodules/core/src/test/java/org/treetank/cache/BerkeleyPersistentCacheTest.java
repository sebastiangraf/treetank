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

import java.io.File;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.interfaces.IPage;

import com.google.inject.Inject;

@Guice(moduleFactory = ModuleFactory.class)
public class BerkeleyPersistentCacheTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ICachedLog cache;

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
            new BerkeleyPersistenceLog(new File(new File(CoreTestHelper.PATHS.PATH1.getFile(),
                StorageConfiguration.Paths.Data.getFile().getName()), CoreTestHelper.RESOURCENAME),
                conf.mNodeFac, conf.mMetaFac);
        CacheTestHelper.setUp(true, cache);
    }

    @Test
    public void test() throws TTIOException {
        for (int i = 0; i < CacheTestHelper.PAGES.length; i++) {
            for (int j = 0; j < CacheTestHelper.PAGES[i].length; j++) {
                final LogContainer<? extends IPage> cont = cache.get(new LogKey(true, i, j));
                final IPage current = cont.getComplete();
                assertEquals(CacheTestHelper.PAGES[i][j], current);
            }

        }
        cache.clear();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.closeEverything();
    }
}
