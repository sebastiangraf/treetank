package com.treetank.www2010;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.AbsFilter;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.CountingMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;

public class CollisionTester {

    private static IWriteTransaction wtx;

    public static File XMLFile = new File("src" + File.separator + "main" + File.separator + "resources"
        + File.separator + "small.xml");

    public void setUpRolling() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            wtx = session.beginWriteTransaction();
            final XMLShredder shredder = new XMLShredder(wtx, XMLShredder.createReader(XMLFile), true);
            shredder.call();
            wtx.moveToDocumentRoot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpPostorder() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            wtx = session.beginWriteTransaction();
            final XMLShredder shredder = new XMLShredder(wtx, XMLShredder.createReader(XMLFile), true);
            shredder.call();
            wtx.moveToDocumentRoot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpPostorder")
    public void benchPostorder() {
        // try {
        //
        // } catch (TreetankException e) {
        // e.printStackTrace();
        // }
    }

    @Bench(beforeEachRun = "setUpRolling")
    public void benchRolling() {
//        try {
//        } catch (TreetankException e) {
//            e.printStackTrace();
//        }
    }

    @AfterEachRun
    public void tearDown() {
        TestHelper.closeEverything();
    }

    public static void main(final String[] args) {

        if (args.length != 2) {
            System.out
                .println("Please use java -jar JAR \"folder with xmls to parse\" \"folder to write csv\"");
            System.exit(-1);
        }

        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
        final File filetoshred = new File(args[0]);
        final File[] files = filetoshred.listFiles();
        final File filetoexport = new File(args[1]);
        for (final File currentFile : files) {
            System.out.println("Starting benchmark for " + currentFile.getName());
            final int index = currentFile.getName().lastIndexOf(".");
            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
            folder.mkdirs();
            XMLFile = currentFile;

            final Benchmark bench = new Benchmark(new AbstractConfig(1, new AbstractMeter[] {
                new CounterFilter(new HashFilter(wtx), new CountingMeter())
            }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
            });
            bench.add(CollisionTester.class);
            final BenchmarkResult res = bench.run();
            new CSVOutput(folder).visitBenchmark(res);
            System.out.println("Finished benchmark for " + currentFile.getName());
        }
    }

    static class HashFilter extends AbsFilter {

        private final Set<Long> hashSet;

        public HashFilter(IReadTransaction rtx) {
            super(rtx);
            hashSet = new HashSet<Long>();
        }

        @Override
        public boolean filter() {
            final long hash = getTransaction().getNode().getHash();
            if (hashSet.contains(hash)) {
                return true;
            } else {
                hashSet.add(hash);
                return false;
            }

        }

    }

}
