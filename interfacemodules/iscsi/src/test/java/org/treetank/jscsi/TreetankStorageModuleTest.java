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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

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

@Guice(moduleFactory = ModuleFactory.class)
public class TreetankStorageModuleTest {

    private TreetankStorageModule storageModule;

    private StorageConfiguration configuration;

    private File file;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeClass
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.createResource(mResource);

        file = CoreTestHelper.PATHS.PATH1.getFile();
        configuration = CoreTestHelper.PATHS.PATH1.getConfig();

        storageModule = new TreetankStorageModule(256, 128, configuration, file);
    }

    @Test(groups = "Initial read write")
    public void testReadAndWrite() throws TTException, IOException {

        final byte[] writeArray = new byte[64 * 1024 * 5];
        for (int i = 0; i < 64 * 1024 * 5; ++i)
            writeArray[i] = (byte)(CoreTestHelper.random.nextDouble() * 256);
        final byte[] readArray = new byte[64 * 1024 * 5];

        System.arraycopy(writeArray, 0, readArray, 0, 64 * 1024 * 5);

        // write
        storageModule.write(writeArray,// bytes (source)
            50,// bytesOffset
            64 * 1024 * 5 - 50,// length
            80);

        // read
        storageModule.read(readArray,// bytes (destination)
            50,// bytesOffset
            64 * 1024 * 5 - 50,// length
            80);

        // check for errors
        assertTrue(Arrays.equals(writeArray, readArray));

        // byte[][] splitBytes = new byte[16][8192];
        //
        // ByteArrayInputStream writeArrayInputStream = new ByteArrayInputStream(writeArray);
        //
        // for (int i = 0; i < splitBytes.length; i++) {
        // splitBytes[i] = new byte[8192];
        // storageModule.read(splitBytes[i],// bytes (destination)
        // 0,// bytesOffset
        // 8192,// length
        // i * 16 * 512);
        // byte[] b = new byte[8192];
        // System.out.println(i);
        // writeArrayInputStream.read(b, 0, 8192);
        //
        // assertTrue(Arrays.equals(splitBytes[i], b));
        // }

    }

    @Test(dependsOnGroups = "Initial read write")
    public void testCheckBounds1() {

        // wrong logical block address
        int result = storageModule.checkBounds(-1,// logicalBlockAddress
            1);// transferLengthInBlocks
        assertEquals(1, result);
        result = storageModule.checkBounds(2,// logicalBlockAddress
            1);// transferLengthInBlocks
        assertEquals(0, result);
    }

    @Test
    public void testOpen() {

        // behavior to test is performed in setUpBeforeClass()
        assertTrue(storageModule != null);
    }
}
