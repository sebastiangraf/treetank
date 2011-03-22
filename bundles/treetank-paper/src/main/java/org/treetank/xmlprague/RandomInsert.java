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
import java.util.Random;

import javax.xml.namespace.QName;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterBenchClass;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class RandomInsert {

    private static int RUNS = 10000;
    private static int NODESPERCOMMIT = 1000;

    private static final Random RAN = new Random(234234234l);
    private final static QName name = new QName("a");

    private static File TNKFile = new File("");

    private IWriteTransaction wtx;
    private long counter = 0;

    @BeforeBenchClass
    public void before() {
        try {
            final IDatabase database = Database.openDatabase(TNKFile);
            final ISession session = database.getSession();
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(name);
        } catch (AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Bench
    public void benchInsert() {
        try {
            for (int i = 0; i < NODESPERCOMMIT; i++) {
                if (RAN.nextBoolean()) {
                    wtx.insertElementAsRightSibling(name);
                } else {
                    wtx.insertElementAsFirstChild(name);
                }

                do {
                    wtx.moveTo(RAN.nextLong() % wtx.getMaxNodeKey());
                } while (wtx.getNode().getNodeKey() == 0);

            }
            wtx.commit();
            System.out.println(counter++);
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
    }

    @AfterBenchClass
    public void last() {
        try {
            Database.forceCloseDatabase(TNKFile);
        } catch (AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {

        if (args.length != 4) {
            System.out
                .println("Please use java -jar JAR \"folder to store\" \"folder to write csv\" \"RUNS\" \"NODESPERCOMMIT\"");
            System.exit(-1);
        }

        RUNS = Integer.parseInt(args[2]);
        NODESPERCOMMIT = Integer.parseInt(args[3]);

        TNKFile = new File(args[0]);
        final File filetoexport = new File(args[1]);

        System.out.println("Starting benchmark for " + TNKFile.getName());
        final FilesizeMeter meter = new FilesizeMeter(new File(new File(TNKFile, "tt"), "tt.tnk"));
        filetoexport.mkdirs();

        final Benchmark bench = new Benchmark(new AbstractConfig(RUNS, new AbstractMeter[] {
            meter, new TimeMeter(Time.MilliSeconds)
        }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
        });

        bench.add(RandomInsert.class);
        final BenchmarkResult res = bench.run();
        new TabularSummaryOutput(System.out).visitBenchmark(res);
        new CSVOutput(filetoexport).visitBenchmark(res);
        System.out.println("Finished benchmark for " + TNKFile.getName());

    }

}
