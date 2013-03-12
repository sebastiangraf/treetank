/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.jscsi;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.jscsi.target.storage.IStorageModule;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

/**
 * This test checks functionalities of {@link TreetankStorageModule}
 * 
 * @author Andreas Rain
 *
 */
@Guice(moduleFactory = ModuleFactory.class)
public class TreetankStorageModuleTest {

    private TreetankStorageModule storageModule;

    private StorageConfiguration configuration;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    /**
     * Setup method for this test.
     * 
     * @throws TTException
     */
    @BeforeClass
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.createResource(mResource);

        configuration = CoreTestHelper.PATHS.PATH1.getConfig();
    }
    
    /**
     * Test if the storage can be created.
     * @throws TTException
     */
    @Test(groups = {"createStorage"})
    public void testCreateStorage() throws TTException{
        storageModule = new TreetankStorageModule(128, configuration);
    }

    /**
     * Check the logic of the checkBounds method.
     */
    @Test(groups = {"boundaryCheck"}, dependsOnGroups = {"createStorage"})
    public void testBoundaries() {

        // wrong logical block address
        int result = storageModule.checkBounds(-1,// logicalBlockAddress
            1);// transferLengthInBlocks
        assertEquals(1, result);
        result = storageModule.checkBounds(2,// logicalBlockAddress
            1);// transferLengthInBlocks
        assertEquals(0, result);
    }

    /**
     * Test functionality of read and write transactions.
     * 
     * @throws TTException
     * @throws IOException
     */
    @Test(dependsOnGroups = {"boundaryCheck"})
    public void testReadAndWrite() throws TTException, IOException {

        final byte[] writeArray = new byte[64 * TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE];
        Random rand = new Random(42);
        rand.nextBytes(writeArray);
        
        final byte[] readArray = new byte[64 * TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE];

        System.arraycopy(writeArray, 0, readArray, 0, 64 * TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE);

        // write
        storageModule.write(writeArray,
            512);

        // read
        storageModule.read(readArray,
            512);

        // check for errors
        assertTrue(Arrays.equals(writeArray, readArray));
        
        storageModule.close();

    }
    
    /**
     * Test whether the storageModule cold be opened.
     */
    @Test
    public void testOpen() {

        // behavior to test is performed in setUpBeforeClass()
        assertTrue(storageModule != null);
    }
}
