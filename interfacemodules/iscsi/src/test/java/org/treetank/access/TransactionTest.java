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

package org.treetank.access;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.Holder;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.bucket.IConstants;
import org.treetank.exception.TTException;
import org.treetank.jscsi.TreetankStorageModule;

import com.google.inject.Inject;

/**
 * This class tests the transactions {@link IscsiWriteTrx} and {@link IscsiReadTrx} directly.
 * 
 * @author Andreas Rain, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public final class TransactionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionTest.class);

    byte[] testBytes;

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    /**
     * @throws TTException
     */
    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder.generateSession(holder, mResource);
        this.holder = Holder.generateWtx(holder, mResource);

        testBytes = new byte[TreetankStorageModule.BYTES_IN_NODE * 256];
        CoreTestHelper.random.nextBytes(testBytes);

        for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
            LOGGER.info("Bootstrapping node " + i);
            this.holder.getIWtx().bootstrap(new byte[TreetankStorageModule.BYTES_IN_NODE]);
            this.holder.getIWtx().commit();
        }

        LOGGER.info("Bootstrapping node " + IConstants.CONTENT_COUNT + 1);
        this.holder.getIWtx().bootstrap(new byte[TreetankStorageModule.BYTES_IN_NODE]);
        this.holder.getIWtx().commit();
    }

    /**
     * Testing to write on every node.
     * 
     * @throws TTException
     */
    @Test
    public void testReadWrite() throws TTException {
        byte[] subByte = new byte[TreetankStorageModule.BYTES_IN_NODE];
        System.arraycopy(testBytes, 0, subByte, 0, TreetankStorageModule.BYTES_IN_NODE);

        holder.getIWtx().moveTo(0);
        holder.getIWtx().setValue(subByte);

        assertTrue(Arrays.equals(subByte, holder.getIWtx().getValueOfCurrentNode()));

        holder.getIWtx().commit();
        assertTrue(Arrays.equals(subByte, holder.getIWtx().getValueOfCurrentNode()));

    }

    /**
     * Clean up after the test finishes.
     * 
     * @throws TTException
     */
    @AfterMethod
    public void tearDown() throws TTException {
        holder.close();
        CoreTestHelper.deleteEverything();
    }

}
