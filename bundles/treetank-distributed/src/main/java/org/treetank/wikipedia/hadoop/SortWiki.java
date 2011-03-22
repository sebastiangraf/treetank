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
 *     * Neither the name of the <organization> nor the
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

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
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
    public int run(final String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        final Job job = new Job(getConf());
        job.setJarByClass(this.getClass());
        job.setJobName(this.getClass().getName());

        // Map output.
        job.setMapOutputKeyClass(DateWritable.class);
        job.setMapOutputValueClass(Text.class);
        
        // Reduce output.
        job.setOutputKeyClass(DateWritable.class);
        job.setOutputValueClass(Text.class);

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

        // First delete target directory.
        deleteDir(new File(args[1]));
        
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        final boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    /**
     * Delete a directory.
     * 
     * @param paramDir
     *                The directory to remove/delete.
     * @return true if it can be deleted, false otherwise.
     */
    public static boolean deleteDir(final File paramDir) {
        if (paramDir.isDirectory()) {
            final String[] children = paramDir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(paramDir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return paramDir.delete();
    }
}
