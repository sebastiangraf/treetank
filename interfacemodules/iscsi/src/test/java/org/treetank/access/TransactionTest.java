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

import static org.testng.Assert.assertEquals;

import java.util.Properties;

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
import org.treetank.api.INode;
import org.treetank.exception.TTException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

@Guice(moduleFactory = ModuleFactory.class)
public final class TransactionTest {

    int size = 512;

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        this.holder = Holder.generateWtx(holder, mResource);
    }

    @Test(enabled = false)
    public void testJustEverything() throws TTException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput(512);
        output.write(1);

        holder.getIWtx().bootstrap(output.toByteArray(), true);
        output.write(2);
        holder.getIWtx().bootstrap(output.toByteArray(), true);
        output.write(3);
        holder.getIWtx().bootstrap(output.toByteArray(), true);
        output.write(4);
        holder.getIWtx().bootstrap(output.toByteArray(), false);
        output.write(5);
        holder.getIWtx().insertAfter(output.toByteArray());

        INode node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 0);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 4);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 1);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 2);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 3);

        holder.getIWtx().moveTo(1);
        holder.getIWtx().remove();

        holder.getIWtx().moveTo(0);

        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 0);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 4);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 2);

        holder.getIRtx().nextNode();
        node = holder.getIRtx().getCurrentNode();
        assertEquals(node.getNodeKey(), 3);

    }

    @AfterMethod
    public void tearDown() throws TTException {
        holder.close();
        CoreTestHelper.deleteEverything();
    }

}
