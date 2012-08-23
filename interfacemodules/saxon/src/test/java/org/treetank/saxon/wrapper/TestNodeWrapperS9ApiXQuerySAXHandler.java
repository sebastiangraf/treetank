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

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.exception.TTException;
import org.treetank.saxon.evaluator.XQueryEvaluatorSAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.google.inject.Inject;

/**
 * <h1>TestNodeWrapperS9ApiXQueryHandler</h1>
 * 
 * <p>
 * Test the NodeWrapper with Saxon's S9Api for XQuery.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class TestNodeWrapperS9ApiXQuerySAXHandler {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        TestHelper.deleteEverything();
        SaxonHelper.createBookDB(mResourceConfig);
        Properties props = TestHelper.createProperties();
        ResourceConfiguration mResource = mResourceConfig.create(props, 10);
        holder = Holder.generateSession(mResource);
    }

    @AfterMethod
    public void afterMethod() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testWhereBooks() throws Exception {
        final StringBuilder strBuilder = new StringBuilder();
        final ContentHandler contHandler = new XMLFilterImpl() {

            @Override
            public void startElement(final String uri, final String localName, final String qName,
                final Attributes atts) throws SAXException {
                strBuilder.append("<" + localName);

                for (int i = 0; i < atts.getLength(); i++) {
                    strBuilder.append(" " + atts.getQName(i));
                    strBuilder.append("=\"" + atts.getValue(i) + "\"");
                }

                strBuilder.append(">");
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                strBuilder.append("</" + localName + ">");
            }

            @Override
            public void characters(final char[] ch, final int start, final int length) throws SAXException {
                for (int i = start; i < start + length; i++) {
                    strBuilder.append(ch[i]);
                }
            }
        };

        new XQueryEvaluatorSAXHandler("for $x in /bookstore/book where $x/price>30 return $x/title", holder
            .getSession(), contHandler).call();

        assertEquals(strBuilder.toString(),
            "<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>");
    }
}
