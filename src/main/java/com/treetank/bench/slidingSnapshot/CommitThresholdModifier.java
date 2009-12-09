package com.treetank.bench.slidingSnapshot;

import java.io.File;
import java.util.Properties;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
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
import com.treetank.constants.ERevisioning;
import com.treetank.constants.ESettable;
import com.treetank.constants.EStorage;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.IConstants;

public class CommitThresholdModifier {

    private static int mProb = 1;

    private static int MOD_SIZE = 3000;

    private final static int FACTOR = 10;

    private IWriteTransaction wtx;
    private ISession session;

    @BeforeEachRun
    public void setUp() {
        try {
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH1));
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }

    }

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
    public void benchRandomIncFactor() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.REVISION_TYPE.getName(),
                    ERevisioning.INCREMENTAL);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < MOD_SIZE; i++) {
                do {
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
                } while (!wtx.getNode().isElement());
                wtx.setName(CommonStuff.getString());
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandomWindowFactor() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.REVISION_TYPE.getName(),
                    ERevisioning.SLIDING_SNAPSHOT);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < MOD_SIZE; i++) {
                do {
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
                } while (!wtx.getNode().isElement());
                wtx.setName(CommonStuff.getString());
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandomIncFull() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.REVISION_TYPE.getName(),
                    ERevisioning.INCREMENTAL);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < MOD_SIZE; i++) {
                do {
                    long nextKey = 0;
                    do {
                        nextKey = CommonStuff.ran.nextLong();
                        if (nextKey < 0) {
                            nextKey = nextKey * -1;
                        }
                        nextKey = nextKey % wtx.getNodeCount();
                    } while (nextKey == 0);

                    wtx.moveTo(nextKey);
                } while (!wtx.getNode().isElement());
                wtx.setName(CommonStuff.getString());
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandomWindowFull() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.REVISION_TYPE.getName(),
                    ERevisioning.SLIDING_SNAPSHOT);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH1, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(CommonStuff.getString(), "");
            for (int i = 0; i < MOD_SIZE; i++) {
                do {
                    long nextKey = 0;
                    do {
                        nextKey = CommonStuff.ran.nextLong();
                        if (nextKey < 0) {
                            nextKey = nextKey * -1;
                        }
                        nextKey = nextKey % wtx.getNodeCount();
                    } while (nextKey == 0);

                    wtx.moveTo(nextKey);
                } while (!wtx.getNode().isElement());
                wtx.setName(CommonStuff.getString());
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        EStorage.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 1; i <= 100; i++) {
            mProb = i;
            final CommitThresholdModifier toBench = new CommitThresholdModifier();
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
            pathBuilder.append(mProb);
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
