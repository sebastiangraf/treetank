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
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.ERevisioning;
import com.treetank.utils.SettableProperties;
import com.treetank.utils.StorageConstants;

public class WindowInsertPercentage {

    private final static int mProb = 20;
    private static Properties props;

    private static int NODE_SET_SIZE = 3000;

    private IWriteTransaction wtx;
    private ISession session;

    private static long window1Seq;
    private static long window1Ran;
    private static long inc1Seq;
    private static long inc1Ran;

    enum Kind {
        WindowSeq, WindowRan, IncSeq, IncRan
    }

    private static Kind kind;

    @BeforeEachRun
    public void setUp() {
        try {
            Session.removeSession(CommonStuff.PATH0);
        } catch (TreetankException exc) {

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
    public void benchIncRan() {
        try {
            kind = Kind.IncRan;
            props.put(SettableProperties.REVISION_TYPE.getName(),
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
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchIncSeq() {
        try {
            kind = Kind.IncSeq;
            props.put(SettableProperties.REVISION_TYPE.getName(),
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
            }
            wtx.commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchWindowSeq() {
        try {
            kind = Kind.WindowSeq;
            props.put(SettableProperties.REVISION_TYPE.getName(),
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
            }
            wtx.commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchWindowRan() {
        try {
            kind = Kind.WindowRan;
            props.put(SettableProperties.REVISION_TYPE.getName(),
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
        StorageConstants.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();

        prepare100Percentage();

        for (int i = 1; i <= 100; i++) {
            props = new Properties();
            props.put(SettableProperties.SNAPSHOT_WINDOW.getName(), i);

            final WindowInsertPercentage toBench = new WindowInsertPercentage();
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
            output.visitBenchmark(res);
        }

    }

    private final static void prepare100Percentage() {
        props = new Properties();
        props.put(SettableProperties.SNAPSHOT_WINDOW.getName(), 1);
        long seqWindowSize = 0;
        long ranWindowSize = 0;
        long seqIncSize = 0;
        long ranIncSize = 0;
        final WindowInsertPercentage insert = new WindowInsertPercentage();
        for (int i = 0; i < RUNS; i++) {
            insert.benchWindowSeq();
            final long lengthSeq = CommonStuff.computeLength(new File(
                    CommonStuff.PATH1, "tt"));
            seqWindowSize = seqWindowSize + lengthSeq;
            insert.tearDown();

            insert.benchWindowRan();
            final long lengthRan = CommonStuff.computeLength(new File(
                    CommonStuff.PATH1, "tt"));
            ranWindowSize = ranWindowSize + lengthRan;
            insert.tearDown();

            insert.benchIncSeq();
            final long lengthIncSeq = CommonStuff.computeLength(new File(
                    CommonStuff.PATH1, "tt"));
            seqIncSize = seqIncSize + lengthIncSeq;
            insert.tearDown();

            insert.benchIncRan();
            final long lengthIncRan = CommonStuff.computeLength(new File(
                    CommonStuff.PATH1, "tt"));
            ranIncSize = ranIncSize + lengthIncRan;
            insert.tearDown();

        }

        window1Seq = seqWindowSize / RUNS;
        window1Ran = ranWindowSize / RUNS;
        inc1Ran = ranIncSize / RUNS;
        inc1Seq = seqIncSize / RUNS;

    }

    private final static int RUNS = 5;
    private final static AbstractMeter[] METERS = {
            new TimeMeter(Time.MilliSeconds),
            new PercentageFileSizeMeter(new File(CommonStuff.PATH1, "tt")) };
    private final static AbstractOutput[] OUTPUT = {};
    private final static KindOfArrangement ARRAN = KindOfArrangement.NoArrangement;
    private final static double gcProb = 1.0d;

    static class BenchmarkConfig extends AbstractConfig {

        protected BenchmarkConfig() {
            super(RUNS, METERS, OUTPUT, ARRAN, gcProb);

        }
    }

    static class PercentageFileSizeMeter extends AbstractMeter {
        private final File mFile;

        public PercentageFileSizeMeter(final File file) {
            mFile = file;
        }

        @Override
        public boolean equals(Object obj) {
            return true;
        }

        @Override
        public String getName() {

            return "FileSize";
        }

        @Override
        public String getUnit() {

            return "bytes";
        }

        @Override
        public String getUnitDescription() {

            return "bytes";
        }

        @Override
        public double getValue() {
            double length = computeLength(mFile);
            switch (kind) {
            case WindowSeq:
                return length / window1Seq;
            case WindowRan:
                return length / window1Ran;
            case IncSeq:
                return length / inc1Seq;
            case IncRan:
                return length / inc1Ran;
            default:
                return 0;
            }

        }

        private final long computeLength(final File file) {
            try {
                long length = 0;
                if (file.isDirectory()) {
                    for (final File child : file.listFiles()) {
                        length = length + computeLength(child);
                    }
                } else {
                    length = length + file.length();
                }
                return length;
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

}