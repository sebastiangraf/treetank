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

import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * <h1>SortWiki</h1>
 * 
 * <p>
 * Sort Wikipedia pages meta history dump according to the timestamps of the revisions.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SortWiki extends Configured implements Tool {

    static {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }

    /**
     * Default constructor.
     */
    public SortWiki() {
        // To make Checkstyle happy.
    }

    /**
     * Main method.
     * 
     * @param args
     *            Program arguments.
     * @throws Exception
     *             Any exception which might have been occured while running Hadoop.
     */
    public static void main(final String[] args) throws Exception {
        final long start = System.currentTimeMillis();
        System.out.print("Running...");
        final int res = ToolRunner.run(new Configuration(), new SortWiki(), args);
        System.out.println("Done in " + (System.currentTimeMillis() - start) / 1000 + "s");
        System.exit(res);
    }

    @Override
    public int run(final String[] args) throws Exception {
        final Job job = new Job(getConf());
        job.setJarByClass(this.getClass());
        job.setJobName(this.getClass().getName());

        job.setOutputKeyClass(Date.class);
        job.setOutputValueClass(List.class);

        job.setMapperClass(XMLMap.class);
        job.setReducerClass(XMLReduce.class);

        job.setInputFormatClass(XMLInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        final Configuration config = job.getConfiguration();
        config.set("timestamp", "timestamp");
        config.set("revision", "revision");
        config.set("record_element_name", "page");
        config.set("namespace_prefix", "");
        config.set("namespace_URI", "");
        config.set("root", "mediawiki");
        
        // Debug settings.
        config.set("mapred.job.tracker", "local");
        config.set("fs.default.name", "local");

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        final boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }
}
