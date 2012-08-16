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

package org.treetank.saxon.wrapper;

import static org.testng.AssertJUnit.assertEquals;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.custommonkey.xmlunit.XMLUnit;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.exception.TTException;
import org.treetank.saxon.evaluator.XPathEvaluator;

import com.google.inject.Inject;

/**
 * Test XPath S9Api.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public final class TestNodeWrapperS9ApiXPath {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    @BeforeMethod
    public void beforeMethod() throws TTException {
        TestHelper.deleteEverything();
        ResourceConfiguration mResource =
            mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10);
        NodeHelper.createTestDocument(mResource);
        holder = Holder.generateRtx(mResource);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @AfterMethod
    public void afterMethod() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testB1() throws Exception {
        final XPathSelector selector = new XPathEvaluator("//b[1]", holder.getSession()).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : selector) {
            strBuilder.append(item.toString());
        }

        assertEquals("expected pieces to be similar", "<b xmlns:p=\"ns\">foo<c xmlns:p=\"ns\"/>\n</b>",
            strBuilder.toString());
    }

    @Test
    public void testB1String() throws Exception {
        final XPathSelector selector = new XPathEvaluator("//b[1]/text()", holder.getSession()).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : selector) {
            strBuilder.append(item.toString());
        }

        AssertJUnit.assertEquals("foo", strBuilder.toString());
    }

    @Test
    public void testB2() throws Exception {
        final XPathSelector selector = new XPathEvaluator("//b[2]", holder.getSession()).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : selector) {
            strBuilder.append(item.toString());
        }

        assertEquals("expected pieces to be similar",
            "<b xmlns:p=\"ns\" p:x=\"y\">\n   <c xmlns:p=\"ns\"/>bar</b>", strBuilder.toString());
    }

    @Test
    public void testB2Text() throws Exception {
        final XPathSelector selector = new XPathEvaluator("//b[2]/text()", holder.getSession()).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : selector) {
            strBuilder.append(item.toString());
        }

        AssertJUnit.assertEquals("bar", strBuilder.toString());
    }

    @Test
    public void testB() throws Exception {
        final XPathSelector selector = new XPathEvaluator("//b", holder.getSession()).call();

        final StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("<result>");
        for (final XdmItem item : selector) {
            strBuilder.append(item.toString());
        }
        strBuilder.append("</result>");

        assertEquals("expected pieces to be similar",
            "<result><b xmlns:p=\"ns\">foo<c xmlns:p=\"ns\"/>\n"+
        "</b><b xmlns:p=\"ns\" p:x=\"y\">\n   "
                + "<c xmlns:p=\"ns\"/>bar</b></result>", strBuilder.toString());
    }

    @Test
    public void testCountB() throws Exception {
        final XPathSelector selector = new XPathEvaluator("count(//b)", holder.getSession()).call();

        final StringBuilder sb = new StringBuilder();

        for (final XdmItem item : selector) {
            sb.append(item.getStringValue());
        }

        AssertJUnit.assertEquals("2", sb.toString());
    }

}
