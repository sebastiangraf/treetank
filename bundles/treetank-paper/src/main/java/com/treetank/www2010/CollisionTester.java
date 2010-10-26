package com.treetank.www2010;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class CollisionTester {

    private final static NodeCounter nodeCounter = new NodeCounter(new HashFilter());
    private final static RelativeNodeCounter relativeCounter = new RelativeNodeCounter(new HashFilter());

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
            ((HashFilter)nodeCounter.getFilter()).setTrx(wtx);
            ((HashFilter)relativeCounter.getFilter()).setTrx(wtx);
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
            ((HashFilter)nodeCounter.getFilter()).setTrx(wtx);
            ((HashFilter)relativeCounter.getFilter()).setTrx(wtx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpPostorder")
    public void benchPostorder() {
        nodeCounter.call();
    }

    @Bench(beforeEachRun = "setUpRolling")
    public void benchRolling() {
        nodeCounter.call();
    }

    @AfterEachRun
    public void tearDown() {
        TestHelper.closeEverything();
        nodeCounter.reset();
        ((HashFilter)nodeCounter.getFilter()).reset();
        ((HashFilter)relativeCounter.getFilter()).reset();
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
         System.out.println("Starting collisiontest for " + currentFile.getName());
         final int index = currentFile.getName().lastIndexOf(".");
         final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
         folder.mkdirs();
         XMLFile = currentFile;
        final Benchmark bench = new Benchmark(new AbstractConfig(1, new AbstractMeter[] {
            nodeCounter, relativeCounter
        }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
        });
        bench.add(CollisionTester.class);
        final BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
            new CSVOutput(folder).visitBenchmark(res);
            System.out.println("Finished collisiontest for " + currentFile.getName());
        }
    }

    static class HashFilter implements IFilter {

        private final Set<Long> hashSet;

        private IReadTransaction rtx;

        public HashFilter() {
            hashSet = new HashSet<Long>();
        }

        @Override
        public boolean filter() {
            final long hash = rtx.getNode().getHash();
            if (hashSet.contains(hash)) {
                return true;
            } else {
                hashSet.add(hash);
                return false;
            }
        }

        public void reset() {
            hashSet.clear();
        }

        public void setTrx(final IReadTransaction rtx) {
            this.rtx = rtx;
        }

        @Override
        public IReadTransaction getTransaction() {
            return rtx;
        }

    }

}
