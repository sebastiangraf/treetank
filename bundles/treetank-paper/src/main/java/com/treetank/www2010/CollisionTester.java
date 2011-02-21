package com.treetank.www2010;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.shredder.EShredderInsert;
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

    private final static FilterCounter filterCounter = new FilterCounter(new HashFilter());
    private final static RelativeNodeCounter relativeCounter = new RelativeNodeCounter(new HashFilter());
    private final static NodeCounter nodeCounter = new NodeCounter();

    private static IWriteTransaction wtx;

    public static File XMLFile = new File("src" + File.separator + "main" + File.separator + "resources"
        + File.separator + "small.xml");

    public static File TNKFILE = TestHelper.PATHS.PATH1.getFile();

    public void setUpRolling() {
        Database.truncateDatabase(TNKFILE);
        try {
            TestHelper.setDB(TNKFILE, WriteTransaction.HashKind.Rolling.name());
            final IDatabase database = TestHelper.getDatabase(TNKFILE);
            final ISession session = database.getSession();
            wtx = session.beginWriteTransaction();
            final XMLShredder shredder =
                new XMLShredder(wtx, XMLShredder.createReader(XMLFile), EShredderInsert.ADDASFIRSTCHILD);
            shredder.call();
            wtx.close();
            final IReadTransaction rtx1 = session.beginReadTransaction();
            final IReadTransaction rtx2 = session.beginReadTransaction();
            final IReadTransaction rtx3 = session.beginReadTransaction();
            ((HashFilter)filterCounter.getFilter()).setTrx(rtx1);
            ((HashFilter)relativeCounter.getFilter()).setTrx(rtx2);
            nodeCounter.setRtx(rtx3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpPostorder() {
        Database.truncateDatabase(TNKFILE);
        try {
            TestHelper.setDB(TNKFILE, WriteTransaction.HashKind.Postorder.name());

            final IDatabase database = TestHelper.getDatabase(TNKFILE);
            final ISession session = database.getSession();
            wtx = session.beginWriteTransaction();
            final XMLShredder shredder =
                new XMLShredder(wtx, XMLShredder.createReader(XMLFile), EShredderInsert.ADDASFIRSTCHILD);
            shredder.call();
            wtx.close();
            final IReadTransaction rtx1 = session.beginReadTransaction();
            final IReadTransaction rtx2 = session.beginReadTransaction();
            final IReadTransaction rtx3 = session.beginReadTransaction();
            ((HashFilter)filterCounter.getFilter()).setTrx(rtx1);
            ((HashFilter)relativeCounter.getFilter()).setTrx(rtx2);
            nodeCounter.setRtx(rtx3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpPostorder")
    public void benchPostorder() {
        ExecutorService exec = Executors.newCachedThreadPool();
        final Future<Void> return1 = exec.submit(filterCounter);
        final Future<Void> return2 = exec.submit(relativeCounter);
        final Future<Void> return3 = exec.submit(nodeCounter);
        exec.shutdown();
        try {
            return1.get();
            return2.get();
            return3.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpRolling")
    public void benchRolling() {
        ExecutorService exec = Executors.newCachedThreadPool();
        final Future<Void> return1 = exec.submit(filterCounter);
        final Future<Void> return2 = exec.submit(relativeCounter);
        final Future<Void> return3 = exec.submit(nodeCounter);
        exec.shutdown();
        try {
            return1.get();
            return2.get();
            return3.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEachRun
    public void tearDown() {
        try {
            Database.forceCloseDatabase(TNKFILE);
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
        nodeCounter.reset();
        filterCounter.reset();
        relativeCounter.reset();
    }

    public static void main(final String[] args) {

        if (args.length != 4) {
            System.out
                .println("Please use java -jar JAR \"folder with xmls to parse\" \"tt location\" \"folder to write csv\" true|false");
            System.exit(-1);
        }
        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
        final File filetoshred = new File(args[0]);
        final File[] files = filetoshred.listFiles();
        final File filetoexport = new File(args[2]);
        TNKFILE = new File(args[1]);
        for (final File currentFile : files) {
            System.out.println("Starting collisiontest for " + currentFile.getName());
            final int index = currentFile.getName().lastIndexOf(".");
            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
            folder.mkdirs();
            XMLFile = currentFile;
            Benchmark bench;
            final boolean listener = Boolean.parseBoolean(args[3]);
            if (listener) {
                bench = new Benchmark(new AbstractConfig(1, new AbstractMeter[] {
                    filterCounter, relativeCounter, nodeCounter
                }, new AbstractOutput[] {
                    new TabularSummaryOutput()
                }, KindOfArrangement.SequentialMethodArrangement, 1.0d) {
                });
            } else {
                bench = new Benchmark(new AbstractConfig(1, new AbstractMeter[] {
                    filterCounter, relativeCounter, nodeCounter
                }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
                });
            }

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
        public void setTransaction(IReadTransaction rtx) {
            this.rtx = rtx;
        }

        public IReadTransaction getTransaction() {
            return this.rtx;
        }

    }

}
