package com.treetank.xmlprague;

import java.io.File;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TTException;
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
        } catch (TTException e) {
            e.printStackTrace();
        }
    }

    public void tearDown() {
        try {
            Database.forceCloseDatabase(TNKFILE);
        } catch (TTException e) {
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
        } catch (TTException exc) {
            exc.printStackTrace();
            return 0;
        }
    }
}
