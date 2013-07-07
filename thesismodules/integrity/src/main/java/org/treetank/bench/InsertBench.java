package org.treetank.bench;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Set;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.MemMeter;
import org.perfidix.meter.Memory;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbNodeFactory;
import org.treetank.bucket.DumbNodeFactory.DumbNode;
import org.treetank.exception.TTException;
import org.treetank.io.IOUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InsertBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private final IStorage mStorage;
    private final ResourceConfiguration mConfig;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        524288
    })[0];
    private IBucketWriteTrx mTrx;

    private static final int FACTOR = 8;

    public InsertBench() throws TTException {
        final File storageFile = FileSystems.getDefault().getPath("tmp", "bench").toFile();
        IOUtils.recursiveDelete(storageFile);
        Injector inj =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(DumbNodeFactory.class).setMetaFacClass(
                DumbMetaEntryFactory.class).createModule());

        mConfig =
            inj.getInstance(IResourceConfigurationFactory.class).create(
                StandardSettings.getProps(storageFile.getAbsolutePath(), RESOURCENAME));

        IOUtils.recursiveDelete(storageFile);
        final StorageConfiguration config = new StorageConfiguration(storageFile);
        Storage.createStorage(config);
        mStorage = Storage.openStorage(storageFile);

    }

    private void insert(int numbersToInsert) throws TTException {
        for (int i = 0; i < numbersToInsert; i++) {
            final long nodeKey = mTrx.incrementNodeKey();
            mNodesToInsert[i].setNodeKey(nodeKey);
            mTrx.setNode(mNodesToInsert[i]);
        }
    }

    @BeforeEachRun
    public void setUp() throws TTException {
        mStorage.createResource(mConfig);
        mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        mTrx = mSession.beginBucketWtx();
    }

    @Bench
    public void bench016384() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(16384 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("16384");
    }

    @Bench
    public void bench032768() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(32768 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("32768");
    }

    @Bench
    public void bench065536() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(65536 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("65536");
    }

    @Bench
    public void bench131072() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(131072 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("131072");
    }

    @Bench
    public void bench262144() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(262144 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("262144");
    }

    @Bench
    public void bench524288() throws TTException {
        for (int i = 0; i < FACTOR; i++) {
            insert(524288 / FACTOR);
            mTrx.commitBlocked();
        }
        System.out.println("524288");
    }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.truncateResource(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
    }

    public static void main(String[] args) {
        Benchmark bench = new Benchmark(new Config());
        bench.add(InsertBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);

        final File outputFold = new File("/Users/sebi/insertBench");
        IOUtils.recursiveDelete(outputFold);
        outputFold.mkdirs();
        new CSVOutput(outputFold).visitBenchmark(res);

    }

    static class Config extends AbstractConfig {

        private final static int RUNS = 100;
        private final static Set<AbstractMeter> METERS = new HashSet<AbstractMeter>();
        private final static Set<AbstractOutput> OUTPUT = new HashSet<AbstractOutput>();

        private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
        private final static double GCPROB = 1.0d;

        static {
            METERS.add(new TimeMeter(Time.MilliSeconds));
            METERS.add(new MemMeter(Memory.Byte));

            // OUTPUT.add(new TabularSummaryOutput());
            // OUTPU
        }

        /**
         * Public constructor.
         */
        public Config() {
            super(RUNS, METERS.toArray(new AbstractMeter[METERS.size()]), OUTPUT
                .toArray(new AbstractOutput[OUTPUT.size()]), ARRAN, GCPROB);
        }

    }

}
