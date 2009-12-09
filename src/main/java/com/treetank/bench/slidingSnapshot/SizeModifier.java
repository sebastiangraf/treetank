package com.treetank.bench.slidingSnapshot;

import java.io.File;
import java.util.Properties;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeBenchClass;
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
import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.settings.ERevisioning;
import com.treetank.settings.ESettable;
import com.treetank.settings.EStoragePaths;

public class SizeModifier {
    private final static int mProb = 20;

    private static int MODIFIERNUMBER = 0;

    private IWriteTransaction wtx;
    private ISession session;

    @BeforeBenchClass
    public void setUp() {
        try {
            Session.removeSession(CommonStuff.PATH2);
            Session.removeSession(CommonStuff.PATH3);
            Session.removeSession(CommonStuff.PATH4);
        } catch (final TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @AfterEachRun
    public void tearDown() {
        try {
            wtx.close();
            session.close();
            Session.removeSession(CommonStuff.PATH2);
            Session.removeSession(CommonStuff.PATH3);
            Session.removeSession(CommonStuff.PATH4);
        } catch (TreetankException exc) {

        }
    }

    @Bench(beforeEachRun = "beforeRan1")
    public void benchRandom1() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.MILESTONE_REVISION.getName(), 1);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH3, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
                if (wtx.getNode().isElement()) {
                    wtx.setName(CommonStuff.getString());
                } else {
                    wtx.setValue(CommonStuff.getString());
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "beforeRan4")
    public void benchRandom4() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.MILESTONE_REVISION.getName(), 4);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH4, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
                if (wtx.getNode().isElement()) {
                    wtx.setName(CommonStuff.getString());
                } else {
                    wtx.setValue(CommonStuff.getString());
                }

                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "beforeInc")
    public void benchRandomInc() {
        try {
            final Properties props = new Properties();
            props.put(ESettable.MILESTONE_REVISION.getName(), 4);
            props.put(ESettable.REVISION_TYPE,
                    ERevisioning.INCREMENTAL);
            final SessionConfiguration conf = new SessionConfiguration(
                    CommonStuff.PATH2, props);
            session = Session.beginSession(conf);
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
                if (wtx.getNode().isElement()) {
                    wtx.setName(CommonStuff.getString());
                } else {
                    wtx.setValue(CommonStuff.getString());
                }

                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public void beforeInc() {
        try {
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH2));
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public void beforeRan1() {
        try {
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH3));
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public void beforeRan4() {
        try {
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH4));
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        EStoragePaths.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 0; i < 30000; i = i + 1000) {
            MODIFIERNUMBER = i;
            final SizeModifier toBench = new SizeModifier();
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
            pathBuilder.append(MODIFIERNUMBER);
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
            new FileSizeMeter(CommonStuff.PATH0) };
    private final static AbstractOutput[] OUTPUT = {};
    private final static KindOfArrangement ARRAN = KindOfArrangement.NoArrangement;
    private final static double gcProb = 1.0d;

    static class BenchmarkConfig extends AbstractConfig {

        protected BenchmarkConfig() {
            super(RUNS, METERS, OUTPUT, ARRAN, gcProb);

        }
    }
}
