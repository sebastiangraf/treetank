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

package org.treetank.xmlprague;

import java.io.File;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class SerializeBench {

    private XMLSerializer serializer;
    public static File TNKFILE = new File("src" + File.separator + "main" + File.separator + "resources"
        + File.separator + "small.xml");

    private long revisionKey = 0;

    public void beforeSerialize() {
        try {
            final IDatabase database = Database.openDatabase(TNKFILE);
            final ISession session = database.getSession();

            final XMLSerializerBuilder builder =
                new XMLSerializerBuilder(session, new NullOutputStream(), revisionKey);

            serializer = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "beforeSerialize", afterEachRun = "tearDown")
    public void benchInsert() {
        try {
            serializer.call();
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
    }

    public void tearDown() {
        try {
            Database.forceCloseDatabase(TNKFILE);
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
        System.out.println(revisionKey++);
    }

    public static void main(final String[] args) {

        if (args.length != 2) {
            System.out
                .println("Please use java -jar JAR \"folder with tnks to serialize\" \"folder to write csv\"");
            System.exit(-1);
        }

        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
        final File filetoserialize = new File(args[0]);
        final File[] files = filetoserialize.listFiles();
        final File filetoexport = new File(args[1]);
        for (final File currentFile : files) {
            TNKFILE = currentFile;
            System.out.println("Starting benchmark for " + TNKFILE.getName());
            final int runs = (int)getRevisions(TNKFILE);
            final int index = currentFile.getName().lastIndexOf(".");
            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
            folder.mkdirs();
            final Benchmark bench = new Benchmark(new AbstractConfig(runs, new AbstractMeter[] {
                new TimeMeter(Time.MilliSeconds)
            }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
            });
            bench.add(SerializeBench.class);
            final BenchmarkResult res = bench.run();
            new TabularSummaryOutput(System.out).visitBenchmark(res);
            new CSVOutput(folder).visitBenchmark(res);
            System.out.println("Finished benchmark for " + TNKFILE.getName());
        }

    }

    private static long getRevisions(final File tnks) {
        try {
            final IDatabase database = Database.openDatabase(tnks);
            final ISession session = database.getSession();
            final IReadTransaction rtx = session.beginReadTransaction();
            final long returnVal = rtx.getRevisionNumber();
            Database.forceCloseDatabase(tnks);
            return returnVal;
        } catch (AbsTTException exc) {
            exc.printStackTrace();
            return 0;
        }
    }
}
