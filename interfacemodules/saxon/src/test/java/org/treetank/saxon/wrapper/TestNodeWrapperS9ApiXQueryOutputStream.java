/**
 * 
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

package org.treetank.saxon.wrapper;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTException;
import org.treetank.saxon.evaluator.XQueryEvaluatorOutputStream;

import com.google.inject.Inject;

/**
 * Test XQuery S9Api.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public final class TestNodeWrapperS9ApiXQueryOutputStream {

    private CoreTestHelper.Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        CoreTestHelper.deleteEverything();
        holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME);
        SaxonHelper.createBookDB(mResourceConfig);
        ResourceConfiguration mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder.generateSession(holder, mResource);
    }

    @AfterMethod
    public void afterMethod() throws TTException {
        holder.getSession().close();
        holder.getStorage().close();
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testWhereBooks() throws Exception {
        final OutputStream out = new ByteArrayOutputStream();
        new XQueryEvaluatorOutputStream("for $x in /bookstore/book where $x/price>30 return $x/title", holder
            .getSession(), out).call();
        final String result = out.toString();
        assertEquals("<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
            result);
    }

    @Test
    public void testOrderByBooks() throws Exception {
        final OutputStream out = new ByteArrayOutputStream();
        new XQueryEvaluatorOutputStream(
            "for $x in /bookstore/book where $x/price>30 order by $x/title return $x/title", holder
                .getSession(), out).call();
        final String result = out.toString();
        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            result);
    }

    @Test
    public void testFLOWR() throws Exception {
        final OutputStream out = new ByteArrayOutputStream();
        new XQueryEvaluatorOutputStream(
            "for $x in /bookstore/book let $y := $x/price where $y>30 order by $x/title return $x/title",
            holder.getSession(), out).call();
        final String result = out.toString();
        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            result);
    }

}
