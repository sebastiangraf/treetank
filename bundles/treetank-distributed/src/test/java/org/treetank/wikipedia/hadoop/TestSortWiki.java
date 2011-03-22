/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.wikipedia.hadoop;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.treetank.wikipedia.hadoop.XMLInputFormat;
import org.treetank.wikipedia.hadoop.XMLMap;
import org.treetank.wikipedia.hadoop.XMLOutputFormat;
import org.treetank.wikipedia.hadoop.XMLReduce;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;

public final class TestSortWiki extends XMLTestCase {

    private static final File INPUT =
        new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "testInput.xml");

    private static final File OUTPUT =
        new File(TestHelper.PATHS.PATH1.getFile().getAbsolutePath() + "testOutput.xml");

    private static final File EXPECTED =
        new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "testExpected.xml");

    @Override
    @Before
    public void setUp() throws Exception {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        OUTPUT.delete();
    }

//    @Ignore
//    public void testWithLocal() throws Exception {
//        MiniMRCluster mr = null;
//        try {
//            mr = new MiniMRCluster(2, "file:///", 3);
//            final Configuration conf = mr.createJobConf();
//            runSortWiki(conf);
//        } finally {
//            if (mr != null) {
//                mr.shutdown();
//            }
//        }
//    }

    /**
     * Run the mapreduce job.
     * 
     * @param paramConf
     *            Job configuration.
     * @throws Exception
     */
    public void runSortWiki(final Configuration paramConf) throws Exception {
        final Job job = new Job(paramConf);
        job.setJarByClass(this.getClass());
        job.setJobName(this.getClass().getName());

        job.setOutputKeyClass(Date.class);
        job.setOutputValueClass(List.class);

        job.setMapperClass(XMLMap.class);
        job.setReducerClass(XMLReduce.class);

        job.setInputFormatClass(XMLInputFormat.class);
        job.setOutputFormatClass(XMLOutputFormat.class);

        final Configuration config = job.getConfiguration();
        config.set("timestamp", "timestamp");
        config.set("revision", "revision");
        config.set("record_element_name", "page");
        config.set("namespace_prefix", "");
        config.set("namespace_URI", "");
        config.set("root", "mediawiki");

        FileInputFormat.setInputPaths(job, new Path(INPUT.getAbsolutePath()));
        FileOutputFormat.setOutputPath(job, new Path(OUTPUT.getAbsolutePath()));

        assertTrue("Job completed without any exceptions", job.waitForCompletion(false));
        final StringBuilder output = TestHelper.readFile(OUTPUT, false);
        final StringBuilder expected = TestHelper.readFile(EXPECTED, false);
        assertXMLEqual("XML files are at least similar", expected.toString(), output.toString());
    }
    
    @Test
    public void test() {
        
    }
}
