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
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
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

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class UpdateBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private final int ELEMENTS = 524288;

    private final IStorage mStorage;
    private final ResourceConfiguration mConfig;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        ELEMENTS
    })[0];
    private IBucketWriteTrx mTrx;

    public UpdateBench() throws TTException {
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

    private void toModify(int numbersToModify, int commitAfterNodes) throws TTException {
        for (int i = 0; i < numbersToModify; i++) {
            final long keyToAdapt = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;

            final DumbNode node = BenchUtils.generateOne();
            node.setNodeKey(keyToAdapt);

            mTrx.setNode(node);
            if (i % commitAfterNodes == 0) {
                mTrx.commit();
            }
        }
        mTrx.close();
    }

    @BeforeEachRun
    public void setUp() throws TTException {
        mStorage.createResource(mConfig);
        mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        mTrx = mSession.beginBucketWtx();
        insert(ELEMENTS);
        mTrx.commitBlocked();
    }

    private static final int COMMITAFTER = 512;

    @Bench
    public void bench16384() throws TTException {
        toModify(16384, COMMITAFTER);
        System.out.println("163842");
    }

    @Bench
    public void bench32768() throws TTException {
        toModify(32768, COMMITAFTER);
        System.out.println("32768");
    }

    @Bench
    public void bench65536() throws TTException {
        toModify(65536, COMMITAFTER);
        System.out.println("65536");
    }

    @Bench
    public void bench131072() throws TTException {
        toModify(131072, COMMITAFTER);
        System.out.println("131072");
    }

    public static void main(String[] args) {
        Benchmark bench = new Benchmark(new Config());
        bench.add(UpdateBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
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

            // OUTPUT.add(new TabularSummaryOutput());
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
