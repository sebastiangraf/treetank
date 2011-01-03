package com.treetank.xmlprague;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TTException;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class SerializeBench {

    private XMLSerializer serializer;
    private static int RUNS;

    private long revisionKey = 0;

    static {
        try {
            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IReadTransaction rtx = session.beginReadTransaction();
            RUNS = (int)rtx.getRevisionNumber();
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beforeSerialize() {
        try {
            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();

//            serializer = new XMLSerializer(session, 0, revisionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "beforeShred", beforeFirstRun = "beforeFirstShred", afterEachRun = "tearDown")
    public void benchInsert() {
        try {
            serializer.call();
        } catch (TTException e) {
            e.printStackTrace();
        }
    }

    public void tearDown() {
        TestHelper.closeEverything();
    }

    public static void main(final String[] args) {

//        if (args.length != 2) {
//            System.out
//                .println("Please use java -jar JAR \"folder with xmls to parse\" \"folder to write csv\"");
//            System.exit(-1);
//        }
//
//        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
//        final File filetoshred = new File(args[0]);
//        final File[] files = filetoshred.listFiles();
//        final File filetoexport = new File(args[1]);
//        for (final File currentFile : files) {
//            XMLFile = currentFile;
//            System.out.println("Starting benchmark for " + XMLFile.getName());
//            final int index = currentFile.getName().lastIndexOf(".");
//            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
//            folder.mkdirs();
//
//            final Benchmark bench = new Benchmark();
//            bench.add(ShredBench.class);
//            final BenchmarkResult res = bench.run();
//            new TabularSummaryOutput(System.out).visitBenchmark(res);
//            new CSVOutput(folder).visitBenchmark(res);
//            System.out.println("Finished benchmark for " + XMLFile.getName());
//        }

    }

}
