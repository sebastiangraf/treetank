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

package org.treetank.service.xml.xpath.expr;

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
import org.treetank.axis.AxisTest;
import org.treetank.exception.TTException;
import org.treetank.service.xml.xpath.XPathAxis;

import com.google.inject.Inject;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class UnionAxisTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        mResource = mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10);
        NodeHelper.createTestDocument(mResource);
        holder =
            Holder.generateRtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testUnion() throws TTException {

        holder.getNRtx().moveTo(1L);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "child::node()/parent::node() union child::node()"), new long[] {
            1L, 4L, 5L, 8L, 9L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "child::node()/parent::node() | child::node()"), new long[] {
            1L, 4L, 5L, 8L, 9L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "child::node()/parent::node() | child::node() | self::node()"), new long[] {
            1L, 4L, 5L, 8L, 9L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "child::node()/parent::node() | child::node() | self::node()" + "union parent::node()"),
            new long[] {
                1L, 4L, 5L, 8L, 9L, 13L, 0L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "b/preceding::node() union text() | descendant::node()"), new long[] {
            4L, 8L, 7L, 6L, 5L, 13L, 9L, 11L, 12L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "//c/ancestor::node() | //node()"),
            new long[] {
                5L, 1L, 9L, 4L, 8L, 13L, 6L, 7L, 11L, 12L
            });

    }

}
