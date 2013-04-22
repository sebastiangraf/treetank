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

import org.jscsi.target.storage.IStorageModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

/**
 * This test checks functionalities of {@link TreetankStorageModule}
 * 
 * @author Andreas Rain, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class TreetankStorageModuleTest {

    private final static int NODENUMBER = 128;

    private TreetankStorageModule storageModule;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    /**
     * Setup method for this test.
     * 
     * @throws TTException
     * @throws InterruptedException 
     */
    @BeforeMethod
    public void setUp() throws TTException, InterruptedException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        CoreTestHelper.Holder.generateSession(holder, mResource);
        storageModule = new TreetankStorageModule(NODENUMBER, holder.getSession());
        
        while(!storageModule.isReady()){
            Thread.sleep(500);
        }
    }

    /**
     * Method to clear data that has been written during tests.
     * 
     * @throws TTException
     */
    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Check the logic of the checkBounds method.
     */
    @Test
    public void testBoundaries() {
        // invalid logical block address
        assertEquals(1, storageModule.checkBounds(-1, 1));
        // block addess out of range
        assertEquals(1, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE, 1));
        // length invalid
        assertEquals(2, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, -1));
        // length out of range
        assertEquals(2, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, 2));
        // correct check
        assertEquals(0, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, 1));
    }

    /**
     * Test functionality of read and write transactions.
     * 
     * @throws TTException
     * @throws IOException
     */
    @Test
    public void testReadAndWrite() throws TTException, IOException {

        final byte[] writeArray = new byte[2 * TreetankStorageModule.BYTES_IN_NODE];
        CoreTestHelper.random.nextBytes(writeArray);

        final byte[] readArray = new byte[writeArray.length];
        // write
        storageModule.write(writeArray, 1 * IStorageModule.VIRTUAL_BLOCK_SIZE);
        // read
        storageModule.read(readArray, 1 * IStorageModule.VIRTUAL_BLOCK_SIZE);
        // check for errors
        assertTrue(Arrays.equals(writeArray, readArray));

    }
}
