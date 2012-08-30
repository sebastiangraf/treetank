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

package org.treetank.service.xml.xpath.xmark;

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
import org.treetank.exception.TTException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathStringChecker;

import com.google.inject.Inject;

/**
 * Performs the XMark benchmark.
 * 
 * @author Patrick Lang
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class XMarkBenchTest {

    final XMarkBenchQueries xmbq = new XMarkBenchQueries();

    private static final String FACTOR = "0.01";
    private static final String XMLFILE = "1mb.xml";

    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + XMLFILE;

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
        mResource = mResourceConfig.create(props, 10);
        holder = Holder.generateWtx(mResource);
        new XMLShredder(holder.getNWtx(), XMLShredder.createFileReader(new File(XML)),
            EShredderInsert.ADDASFIRSTCHILD).call();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }
    
    @Test
    public void xMarkTest_Q1() throws TTXPathException {
        String query = xmbq.getQuery(1, FACTOR);
        String result = xmbq.getResult(1, FACTOR);
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(), query),
            new String[] {
                result
            });
    }

    @Test
    public void xMarkTest_Q5() throws TTXPathException {
        String query = xmbq.getQuery(5, FACTOR);
        String result = xmbq.getResult(5, FACTOR);
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(), query),
            new String[] {
                result
            });
    }

    @Test
    public void xMarkTest_Q6() throws TTXPathException {
        String query = xmbq.getQuery(6, FACTOR);
        String result = xmbq.getResult(6, FACTOR);
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(), query),
            new String[] {
                result
            });
    }

    @Test
    public void xMarkTest_Q7() throws TTXPathException {
        String query = xmbq.getQuery(7, FACTOR);
        String result = xmbq.getResult(7, FACTOR);
        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(), query),
            new String[] {
                result
            });
    }

    /*
     * @Test public void xMarkTest_Q21() { String query = xmbq.getQuery(21,
     * FACTOR); String result = xmbq.getResult(21, FACTOR);
     * XPathStringChecker.testIAxisConventions(holder.getRtx(),new XPathAxis(getRtx(), query),
     * new String[] { result }); }
     * 
     * @Test public void xMarkTest_Q22() { String query = xmbq.getQuery(22,
     * FACTOR); String result = xmbq.getResult(22, FACTOR);
     * XPathStringChecker.testIAxisConventions(holder.getRtx(),new XPathAxis(getRtx(), query),
     * new String[] { result }); }
     * 
     * @Test public void xMarkTest_Q23() { String query = xmbq.getQuery(23,
     * FACTOR); String result = xmbq.getResult(23, FACTOR);
     * XPathStringChecker.testIAxisConventions(holder.getRtx(),new XPathAxis(getRtx(), query),
     * new String[] { result }); }
     */

}
