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
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Inserting sequentially (what else) different powers of 2-nodes to the storage and making FACTOR
 * intermediate commits. The commits occur once unblocked and once blocked to determine the impact of
 * blocking.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class InsertBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private int counter = 0;

    private IStorage mStorage;
    private final Injector mInject;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        262144
    })[0];
    private IBucketWriteTrx mTrx;

    private static final int FACTOR = 8;

    public InsertBench() throws TTException {
        final File storageFile = FileSystems.getDefault().getPath("/Users/sebi/neverbackuped/bla").toFile();
        // final File storageFile = FileSystems.getDefault().getPath("/Volumes/ramdisk/tt").toFile();
        IOUtils.recursiveDelete(storageFile);

        mInject =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(DumbNodeFactory.class).setMetaFacClass(
                DumbMetaEntryFactory.class).setBackendClass(JCloudsStorage.class).createModule());

        final StorageConfiguration config = new StorageConfiguration(storageFile);
        Storage.createStorage(config);
        mStorage = Storage.openStorage(storageFile);

    }

    private void insert(int numbersToInsert, boolean blocked) throws TTException {
        final int offset = numbersToInsert / FACTOR;
        // long lastTime = 0;
        for (int i = 0; i < FACTOR; i++) {
            // long time1 = System.currentTimeMillis();
            for (int j = 0; j < offset; j++) {
                final long nodeKey = mTrx.incrementNodeKey();
                mNodesToInsert[i * offset + j].setNodeKey(nodeKey);
                mTrx.setNode(mNodesToInsert[i * offset + j]);
            }
            // long time2 = System.currentTimeMillis();
            if (blocked) {
                mTrx.commitBlocked();
            } else {
                mTrx.commit();
            }
            // long time3 = System.currentTimeMillis();
            // System.out.println("Time to insert: " + (time2 - time1));
            // System.out.println("Time to commit: " + (time3 - time2));
            // lastTime = time3;
        }
        mTrx.close();
        // long time4 = System.currentTimeMillis();
        // System.out.println("Time to end: " + (time4 - lastTime));
    }

    @BeforeEachRun
    public void setUp() throws TTException {

        final ResourceConfiguration config =
            mInject.getInstance(IResourceConfigurationFactory.class).create(
                StandardSettings.getProps(mStorage.getLocation().getAbsolutePath(), new StringBuilder(
                    RESOURCENAME).append(counter).toString()));

        mStorage.truncateResource(new SessionConfiguration(new StringBuilder(RESOURCENAME).append(counter)
            .toString(), StandardSettings.KEY));
        mStorage.createResource(config);
        mSession =
            mStorage.getSession(new SessionConfiguration(new StringBuilder(RESOURCENAME).append(counter)
                .toString(), StandardSettings.KEY));
        mTrx = mSession.beginBucketWtx();
    }

    @Bench
    public void blocked016384() throws TTException {
        insert(16384, true);
        mTrx.close();
        System.out.println("16384");
    }

    @Bench
    public void blocked032768() throws TTException {
        insert(32768, true);
        System.out.println("32768");
    }

    @Bench
    public void blocked065536() throws TTException {
        insert(65536, true);
        mTrx.close();
        System.out.println("65536");
    }

    @Bench
    public void blocked131072() throws TTException {
        insert(131072, true);
        mTrx.close();
        System.out.println("131072");
    }

    @Bench
    public void blocked262144() throws TTException {
        insert(262144, true);
        mTrx.close();
        System.out.println("262144");
    }

    @Bench
    public void nonblocked016384() throws TTException {
        insert(16384, false);
        mTrx.close();
        System.out.println("16384");
    }

    @Bench
    public void nonblocked032768() throws TTException {
        insert(32768, false);
        System.out.println("32768");
    }

    @Bench
    public void nonblocked065536() throws TTException {
        insert(65536, false);
        mTrx.close();
        System.out.println("65536");
    }

    @Bench
    public void nonblocked131072() throws TTException {
        insert(131072, false);
        mTrx.close();
        System.out.println("131072");
    }

    @Bench
    public void nonblocked262144() throws TTException {
        insert(262144, false);
        mTrx.close();
        System.out.println("262144");
    }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.truncateResource(new SessionConfiguration(new StringBuilder(RESOURCENAME).append(counter)
            .toString(), StandardSettings.KEY));
        counter++;
    }

    final static File outputFold = new File("/Users/sebi/listenerBench");

    public static void main(String[] args) {
        final File resultFold = new File("/Users/sebi/resBench");
        // IOUtils.recursiveDelete(outputFold);
        IOUtils.recursiveDelete(resultFold);
        outputFold.mkdirs();
        resultFold.mkdirs();

        Benchmark bench = new Benchmark(new Config());
        bench.add(InsertBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);

        new CSVOutput(resultFold).visitBenchmark(res);

    }

    static class Config extends AbstractConfig {

        private final static int RUNS = 10;
        private final static Set<AbstractMeter> METERS = new HashSet<AbstractMeter>();
        private final static Set<AbstractOutput> OUTPUT = new HashSet<AbstractOutput>();

        private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
        private final static double GCPROB = 1.0d;

        static {
            METERS.add(new TimeMeter(Time.MilliSeconds));

            OUTPUT.add(new CSVOutput(outputFold));
            OUTPUT.add(new TabularSummaryOutput());
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
