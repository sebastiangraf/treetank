/**
 * 
 */
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
import org.treetank.bucket.DumbDataFactory;
import org.treetank.bucket.DumbDataFactory.DumbData;
import org.treetank.exception.TTException;
import org.treetank.io.IOUtils;
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class UpdateBench {

    private final String RESOURCENAME = "benchResourcegrave9283";
    private final int ELEMENTS = 262144;
    // private final int ELEMENTS = 32768;

    private int counter = 0;

    private IStorage mStorage;
    private final Injector mInject;
    private ISession mSession;
    private DumbData[] mNodesToInsert = BenchUtils.createDatas(new int[] {
        ELEMENTS
    })[0];
    private IBucketWriteTrx mTrx;

    private static final int FACTOR = 8;

    public UpdateBench() throws TTException {
        final File storageFile = FileSystems.getDefault().getPath("/Users/sebi/bla").toFile();
        // final File storageFile = FileSystems.getDefault().getPath("/Volumes/ramdisk/tt").toFile();
        IOUtils.recursiveDelete(storageFile);

        mInject =
            Guice.createInjector(new ModuleSetter().setDataFacClass(DumbDataFactory.class).setMetaFacClass(
                DumbMetaEntryFactory.class).setBackendClass(JCloudsStorage.class).createModule());

        final StorageConfiguration config = new StorageConfiguration(storageFile);
        Storage.createStorage(config);
        mStorage = Storage.openStorage(storageFile);

    }

    private void modify(int numbersToModify, boolean blocked, boolean seq) throws TTException {
        final int offset = numbersToModify / FACTOR;
        // long lastTime = 0;
        for (int i = 0; i < FACTOR; i++) {
            // long time1 = System.currentTimeMillis();
            for (int j = 0; j < offset; j++) {
                long keyToAdapt;
                if (seq) {
                    keyToAdapt = (i * offset) + j;
                } else {
                    keyToAdapt = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;
                }

                final DumbData node = BenchUtils.generateOne();
                node.setDataKey(keyToAdapt);
                mTrx.setData(node);
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

        for (int i = 0; i < ELEMENTS; i++) {
            final long nodeKey = mTrx.incrementDataKey();
            mNodesToInsert[i].setDataKey(nodeKey);
            mTrx.setData(mNodesToInsert[i]);
        }
        mTrx.commitBlocked();
    }

    @Bench
    public void blockedRan016384() throws TTException {
        modify(16384, true, false);
        System.out.println("163842");
    }

    @Bench
    public void blockedRan032768() throws TTException {
        modify(32768, true, false);
        System.out.println("32768");
    }

    @Bench
    public void blockedRan065536() throws TTException {
        modify(65536, true, false);
        System.out.println("65536");
    }

    @Bench
    public void blockedRan131072() throws TTException {
        modify(131072, true, false);
        System.out.println("131072");
    }

    @Bench
    public void blockedRan262144() throws TTException {
        modify(262144, true, false);
        System.out.println("262144");
    }

    @Bench
    public void nonblockedRan016384() throws TTException {
        modify(16384, false, false);
        System.out.println("163842");
    }

    @Bench
    public void nonblockedRan032768() throws TTException {
        modify(32768, false, false);
        System.out.println("32768");
    }

    @Bench
    public void nonblockedRan065536() throws TTException {
        modify(65536, false, false);
        System.out.println("65536");
    }

    @Bench
    public void nonblockedRan131072() throws TTException {
        modify(131072, false, false);
        System.out.println("131072");
    }

    @Bench
    public void nonblockedRan262144() throws TTException {
        modify(262144, false, false);
        System.out.println("262144");
    }

    @Bench
    public void blockedSeq016384() throws TTException {
        modify(16384, true, true);
        System.out.println("163842");
    }

    @Bench
    public void blockedSeq032768() throws TTException {
        modify(32768, true, true);
        System.out.println("32768");
    }

    @Bench
    public void blockedSeq065536() throws TTException {
        modify(65536, true, true);
        System.out.println("65536");
    }

    @Bench
    public void blockedSeq131072() throws TTException {
        modify(131072, true, true);
        System.out.println("131072");
    }

    @Bench
    public void blockedSeq262144() throws TTException {
        modify(262144, true, true);
        System.out.println("262144");
    }

    @Bench
    public void nonblockedSeq016384() throws TTException {
        modify(16384, false, true);
        System.out.println("163842");
    }

    @Bench
    public void nonblockedSeq032768() throws TTException {
        modify(32768, false, true);
        System.out.println("32768");
    }

    @Bench
    public void nonblockedSeq065536() throws TTException {
        modify(65536, false, true);
        System.out.println("65536");
    }

    @Bench
    public void nonblockedSeq131072() throws TTException {
        modify(131072, false, true);
        System.out.println("131072");
    }

    @Bench
    public void nonblockedSeq262144() throws TTException {
        modify(262144, false, true);
        System.out.println("262144");
    }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.close();
        // mStorage.truncateResource(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
    }

    final static File outputFold = new File("/Users/sebi/listenerBench");

    public static void main(String[] args) {

        final File resultFold = new File("/Users/sebi/resBench");
        IOUtils.recursiveDelete(outputFold);
        IOUtils.recursiveDelete(resultFold);
        outputFold.mkdirs();
        resultFold.mkdirs();

        Benchmark bench = new Benchmark(new Config());
        bench.add(UpdateBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);

        new CSVOutput(resultFold).visitBenchmark(res);
    }

    static class Config extends AbstractConfig {

        private final static int RUNS = 1;
        private final static Set<AbstractMeter> METERS = new HashSet<AbstractMeter>();
        private final static Set<AbstractOutput> OUTPUT = new HashSet<AbstractOutput>();

        private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
        private final static double GCPROB = 1.0d;

        static {
            METERS.add(new TimeMeter(Time.MilliSeconds));
            // METERS.add(new MemMeter(Memory.Byte));

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
