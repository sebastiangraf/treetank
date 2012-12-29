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

package org.treetank.service.xml.xpath;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AxisTest;
import org.treetank.exception.TTException;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class XPathAxisWideTest {

    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "factbook.xml";

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
        Properties props =
            StandardSettings.getStandardProperties(TestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                TestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        holder = Holder.generateWtx(mResource);
        new XMLShredder(holder.getNWtx(), XMLShredder.createFileReader(new File(XML)),
            EShredderInsert.ADDASFIRSTCHILD).call();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testIterateFactbook() throws Exception {
        // Verify.
        holder.getNRtx().moveTo(ROOT_NODE);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/mondial/continent[@id]"), new long[] {
            2L, 5L, 8L, 11L, 14L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "mondial/continent[@name]"),
            new long[] {
                2L, 5L, 8L, 11L, 14L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "mondial/continent[@id=\"f0_119\"]"),
            new long[] {
                2L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "/mondial/continent[@name = \"Africa\"]"), new long[] {
            14L
        });

        final AbsAxis axis5 = new XPathAxis(holder.getNRtx(), "mondial/lake/node()");
        for (int i = 0; i < 61; i++) {
            assertEquals(true, axis5.hasNext());
        }
        // assertEquals(29891L, axis5.next());
        assertEquals(false, axis5.hasNext());

        final AbsAxis axis6 = new XPathAxis(holder.getNRtx(), "mondial/country/religions/node()");
        for (int i = 0; i < 446; i++) {
            assertEquals(true, axis6.hasNext());
            axis6.next();
        }
        assertEquals(false, axis6.hasNext());

        final AbsAxis axis7 = new XPathAxis(holder.getNRtx(), "child::mondial/child::lake/child::node()");
        for (int i = 0; i < 60; i++) {
            assertEquals(true, axis7.hasNext());
            axis7.next();
        }
        assertEquals(true, axis7.hasNext());
        // assertEquals(29891L, axis7.next());
        assertEquals(false, axis7.hasNext());

        final AbsAxis axis8 = new XPathAxis(holder.getNRtx(), "//*[@id]");
        for (int i = 0; i < 5562; i++) {
            assertEquals(true, axis8.hasNext());
            axis8.next();
        }
        assertEquals(false, axis8.hasNext());

        final AbsAxis axis9 = new XPathAxis(holder.getNRtx(), "/mondial/country/attribute::car_code");
        for (int i = 0; i < 194; i++) {
            assertEquals(true, axis9.hasNext());
            axis9.next();
        }
        assertEquals(false, axis9.hasNext());

        final AbsAxis axis10 = new XPathAxis(holder.getNRtx(), "//country[@*]");
        for (int i = 0; i < 231; i++) {
            assertEquals(true, axis10.hasNext());
            axis10.next();
        }
        assertEquals(false, axis10.hasNext());

        holder.close();
    }

}
