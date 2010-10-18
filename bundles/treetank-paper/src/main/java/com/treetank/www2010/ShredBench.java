package com.treetank.www2010;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class ShredBench {

    private XMLShredder shredderNone;

    private static final int RUNS = 100;

    public static File XMLFile = new File("src" + File.separator + "main" + File.separator + "resources"
        + File.separator + "small.xml");

    public void setUpNone() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.None.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone = new XMLShredder(wtx, XMLShredder.createReader(XMLFile), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpRolling() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone = new XMLShredder(wtx, XMLShredder.createReader(XMLFile), true);
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
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone = new XMLShredder(wtx, XMLShredder.createReader(XMLFile), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpPostorder", runs = RUNS)
    public void benchPostorder() {
        try {
            shredderNone.call();
        } catch (TreetankException e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpRolling", runs = RUNS)
    public void benchRolling() {
        try {
            shredderNone.call();
        } catch (TreetankException e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpNone", runs = RUNS)
    public void benchNone() {
        try {
            shredderNone.call();
        } catch (TreetankException e) {
            e.printStackTrace();
        }
    }

    @AfterEachRun
    public void tearDown() {
        TestHelper.closeEverything();

        System.out.println("Run finished!");
    }

    public static void main(final String[] args) {

        if (args.length > 0) {
            final File filetoshred = new File(args[0]);
            ShredBench.XMLFile = filetoshred;
        }

        final Benchmark bench = new Benchmark();
        bench.add(ShredBench.class);

        final BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
    }

}
