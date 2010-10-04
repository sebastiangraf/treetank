/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.wikipedia.hadoop;

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
