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

import java.io.File;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTException;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.Holder;
import org.treetank.testutil.ModuleFactory;

import com.google.inject.Inject;

/**
 * Performes the XMark benchmark.
 * 
 * @author Tina Scherer
 */
@Guice(moduleFactory = ModuleFactory.class)
public class XMarkTest {

    // XMark 1 GB
    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "auction.xml";

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        this.holder = Holder.generateWtx(holder, mResource);
        new XMLShredder(this.holder.getNWtx(), XMLShredder.createFileReader(new File(XML)),
            EShredderInsert.ADDASFIRSTCHILD).call();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testQ1_10() throws TTException {
        // Verify.

        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "/site/people/person[@id=\"person0\"]/name/text()"), new String[] {
            "Sinisa Farrel"
        });
    }

    @Test
    public void testQ1() throws TTException {

        // Q1 The name of the person with ID 'person0' {projecting}
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "for $b in /site/people/person[@id=\"person0\"] " + "return $b/name/text()"), new String[] {
            "Sinisa Farrel"
        });
    }

    @Test
    public void testQ5() throws TTException {

        // Q5 How many sold items cost more than 40?
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] "
                + "return $i/price)"), new String[] {
            "75"
        });
    }

    @Test
    public void testQ6() throws TTException {

        // Q6 How many items are listed on all continents?
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "for $b in //site/regions return fn:count($b//item)"), new String[] {
            "217"
        });
    }

    @Test
    public void testQ7() throws TTException {
        // Q7 How many pieces of prose are in our database?
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "for $p in /site return fn:count($p//description) + "
                + "fn:count($p//annotation) + fn:count($p//emailaddress)"), new String[] {
            "916.0"
        }); // TODO: why double?
    }

}
