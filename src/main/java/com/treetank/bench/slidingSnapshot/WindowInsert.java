package com.treetank.bench.slidingSnapshot;

import java.io.File;
import java.util.Properties;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;

import com.treetank.access.Session;
import com.treetank.access.SessionConfiguration;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EStoragePaths;

public class WindowInsert {

    private final static int mProb = 20;
    private static Properties props;

    private static int NODE_SET_SIZE = 3000;

    private IWriteTransaction wtx;
    private ISession session;

    // @BeforeBenchClass
    // public void setUp() {
    // 
    // }

    @AfterEachRun
    public void tearDown() {
        try {
            wtx.close();
            session.close();
            Session.removeSession(CommonStuff.PATH1);
        } catch (TreetankException exc) {

        }
    }

    @Bench
    public void benchSeq() {
        try {
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < NODE_SET_SIZE; i++) {
                if (CommonStuff.ran.nextBoolean()) {
                    wtx.insertElementAsFirstChild(CommonStuff.getString(), "");

                } else {
                    wtx
                            .insertElementAsRightSibling(CommonStuff
                                    .getString(), "");
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandom() {
        try {
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < NODE_SET_SIZE; i++) {
                if (CommonStuff.ran.nextBoolean()) {
                    wtx.insertElementAsFirstChild(CommonStuff.getString(), "");

                } else {
                    wtx
                            .insertElementAsRightSibling(CommonStuff
                                    .getString(), "");
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }

                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static void main(final String[] args) throws TreetankUsageException {
        EStoragePaths.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 1; i <= 100; i++) {
            props = new Properties();
            props.put(EDatabaseSetting.MILESTONE_REVISION.getName(), i);

            final WindowInsert toBench = new WindowInsert();
            final Benchmark benchmark = new Benchmark(new BenchmarkConfig());
            benchmark.add(toBench);
            final BenchmarkResult res = benchmark.run();
            final StringBuilder pathBuilder = new StringBuilder(
                    CommonStuff.RESULTFOLDER.getAbsolutePath())
                    .append(File.separator);
            if (i < 100) {
                pathBuilder.append("0");
                if (i < 10) {
                    pathBuilder.append("0");
                }
            }
            pathBuilder.append(i);
            final File file = new File(pathBuilder.toString());
            file.mkdirs();
            final CSVOutput output = new CSVOutput(file);
            // final TabularSummaryOutput tab = new TabularSummaryOutput();
            output.visitBenchmark(res);
            // tab.visitBenchmark(res);
        }

    }

    private final static int RUNS = 5;
    private final static AbstractMeter[] METERS = {
            new TimeMeter(Time.MilliSeconds),
            new FileSizeMeter(CommonStuff.PATH1) };
    private final static AbstractOutput[] OUTPUT = {};
    private final static KindOfArrangement ARRAN = KindOfArrangement.NoArrangement;
    private final static double gcProb = 1.0d;

    static class BenchmarkConfig extends AbstractConfig {

        protected BenchmarkConfig() {
            super(RUNS, METERS, OUTPUT, ARRAN, gcProb);

        }
    }

}
