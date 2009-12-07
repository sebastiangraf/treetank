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

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.ERevisioning;
import com.treetank.utils.IConstants;
import com.treetank.utils.SettableProperties;
import com.treetank.utils.StorageConstants;

public class PercentageSizeInsert {
    private final static int mProb = 20;

    private static int NODE_SET_SIZE = 0;

    private final static int FACTOR = 10;

    private final static int WINDOW_SIZE = 4;
    private final static int REVISION_MILESTONES = 4;

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
    public void benchRandomInc() {
        try {
            final Properties props = new Properties();
            props.put(SettableProperties.SNAPSHOT_WINDOW.getName(),
                    REVISION_MILESTONES);
            props.put(SettableProperties.REVISION_TYPE,
                    ERevisioning.INCREMENTAL);
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
                    nextKey = nextKey
                            % (FACTOR * IConstants.INP_REFERENCE_COUNT);
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandom1() {
        try {
            final Properties props = new Properties();
            props.put(SettableProperties.SNAPSHOT_WINDOW.getName(), 1);
            props.put(SettableProperties.REVISION_TYPE,
                    ERevisioning.SLIDING_SNAPSHOT);
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
                    nextKey = nextKey
                            % (FACTOR * IConstants.INP_REFERENCE_COUNT);
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandom4() {
        try {
            final Properties props = new Properties();
            props
                    .put(SettableProperties.SNAPSHOT_WINDOW.getName(),
                            WINDOW_SIZE);
            props.put(SettableProperties.REVISION_TYPE,
                    ERevisioning.SLIDING_SNAPSHOT);
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
                    nextKey = nextKey
                            % (FACTOR * IConstants.INP_REFERENCE_COUNT);
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        StorageConstants.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 0; i < 30000; i = i + 1000) {
            NODE_SET_SIZE = i;
            final PercentageSizeInsert toBench = new PercentageSizeInsert();
            final Benchmark benchmark = new Benchmark(new BenchmarkConfig());
            benchmark.add(toBench);
            final BenchmarkResult res = benchmark.run();
            final StringBuilder pathBuilder = new StringBuilder(
                    CommonStuff.RESULTFOLDER.getAbsolutePath())
                    .append(File.separator);
            if (i < 100000) {
                pathBuilder.append("0");
                if (i < 10000) {
                    pathBuilder.append("0");
                    if (i < 1000) {
                        pathBuilder.append("0");
                        if (i < 100) {
                            pathBuilder.append("0");
                            if (i < 10) {
                                pathBuilder.append("0");
                            }
                        }
                    }
                }
            }
            pathBuilder.append(NODE_SET_SIZE);
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
