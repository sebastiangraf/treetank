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

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.ERevisioning;
import com.treetank.utils.IConstants;

public class PercentageSizeModifier {

    private final static int mProb = 80;

    private static int MODIFIERNUMBER = 0;

    private final static int FACTOR = 10;

    private IWriteTransaction wtx;
    private ISession session;
    private IDatabase database;

    @BeforeBenchClass
    public void setUp() {
        Database.truncateDatabase(CommonStuff.PATH1);
        Database.truncateDatabase(CommonStuff.PATH2);
        Database.truncateDatabase(CommonStuff.PATH3);
    }

    @AfterEachRun
    public void tearDown() {
        try {
            wtx.close();
            session.close();
            database.close();
            Database.truncateDatabase(CommonStuff.PATH1);
            Database.truncateDatabase(CommonStuff.PATH2);
            Database.truncateDatabase(CommonStuff.PATH3);
        } catch (TreetankException exc) {

        }
    }

    @Bench(beforeEachRun = "beforeRan1")
    public void benchRandom1() {
        try {
            final Properties props = new Properties();
            props.setProperty(EDatabaseSetting.MILESTONE_REVISION.name(), "1");
            props.setProperty(EDatabaseSetting.REVISION_TYPE.name(),
                    ERevisioning.SLIDING_SNAPSHOT.name());
            final DatabaseConfiguration conf = new DatabaseConfiguration(
                    CommonStuff.PATH1, props);
            Database.createDatabase(conf);
            database = Database.openDatabase(CommonStuff.PATH1);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
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
            props.setProperty(EDatabaseSetting.MILESTONE_REVISION.name(), "4");
            props.setProperty(EDatabaseSetting.REVISION_TYPE.name(),
                    ERevisioning.SLIDING_SNAPSHOT.name());
            final DatabaseConfiguration conf = new DatabaseConfiguration(
                    CommonStuff.PATH1, props);
            Database.createDatabase(conf);
            database = Database.openDatabase(CommonStuff.PATH1);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
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
            props.setProperty(EDatabaseSetting.MILESTONE_REVISION.name(), "4");
            props.setProperty(EDatabaseSetting.REVISION_TYPE.name(),
                    ERevisioning.INCREMENTAL.name());
            final DatabaseConfiguration conf = new DatabaseConfiguration(
                    CommonStuff.PATH1, props);
            Database.createDatabase(conf);
            database = Database.openDatabase(CommonStuff.PATH1);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            for (int i = 0; i < MODIFIERNUMBER; i++) {
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

    public void beforeRan1() {
        try {
            XMLShredder.main(CommonStuff.XMLPath.getAbsolutePath(),
                    CommonStuff.PATH1.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beforeRan4() {
        try {
            XMLShredder.main(CommonStuff.XMLPath.getAbsolutePath(),
                    CommonStuff.PATH2.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beforeInc() {
        try {
            XMLShredder.main(CommonStuff.XMLPath.getAbsolutePath(),
                    CommonStuff.PATH3.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        CommonStuff.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 0; i < 30000; i = i + 1000) {
            MODIFIERNUMBER = i;
            final PercentageSizeModifier toBench = new PercentageSizeModifier();
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
