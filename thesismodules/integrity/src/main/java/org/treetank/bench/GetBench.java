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
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbNodeFactory;
import org.treetank.bucket.DumbNodeFactory.DumbNode;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IOUtils;
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Benchmarking the getting of data. ELEMENTS nodes are inserted within FACTOR revisions. Afterwards within
 * FACTOR revisions, ELEMENTS nodes are modified. The aim are highly distributed and scattered buckets.
 * Then powers of 2 nodes are retrieved sequentially and random-access-like.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class GetBench {
    private final String RESOURCENAME = "benchResourcegrave9283123";

    private static final File benchedFile = FileSystems.getDefault().getPath("tmp", "bench").toFile();
    // private static final File benchedFile =
    // FileSystems.getDefault().getPath("/Volumes/ramdisk/tt").toFile();

    private static final int FACTOR = 8;

    private final int ELEMENTS = 262144;
    // private final int ELEMENTS = 32768;

    private IStorage mStorage;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        ELEMENTS
    })[0];
    private IBucketReadTrx mTrx;

    public GetBench() throws TTException {
        // final Injector inj =
        // Guice.createInjector(new ModuleSetter().setNodeFacClass(DumbNodeFactory.class).setMetaFacClass(
        // DumbMetaEntryFactory.class).setBackendClass(JCloudsStorage.class).createModule());
        //
        // final ResourceConfiguration resConfig =
        // inj.getInstance(IResourceConfigurationFactory.class).create(
        // StandardSettings.getProps(benchedFile.getAbsolutePath(), RESOURCENAME));
        // IOUtils.recursiveDelete(benchedFile);
        //
        // // Creating Storage and inserting ELEMENTS nodes in FACTOR revisions
        // final StorageConfiguration storConfig = new StorageConfiguration(benchedFile);
        // Storage.createStorage(storConfig);
        // final IStorage storage = Storage.openStorage(benchedFile);
        // storage.createResource(resConfig);
        // final ISession session =
        // storage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        // IBucketWriteTrx trx = session.beginBucketWtx();
        // long time = System.currentTimeMillis();
        // // Creating FACTOR versions with ELEMENTS\FACTOR elements
        // for (int j = 0; j < FACTOR; j++) {
        // System.out.println("Inserting revision " + j + " and " + (ELEMENTS / FACTOR) + " elements");
        // for (int i = 0; i < ELEMENTS / FACTOR; i++) {
        // final long nodeKey = trx.incrementNodeKey();
        // mNodesToInsert[i].setNodeKey(nodeKey);
        // trx.setNode(mNodesToInsert[i]);
        // }
        // trx.commit();
        // }
        // trx.close();
        // trx = session.beginBucketWtx();
        // long endtime = System.currentTimeMillis();
        // System.out.println("Generating nodes in " + FACTOR + " versions took " + (endtime - time) + "ms");
        //
        // // Modifying ELEMENT nodes in FACTOR revisions.
        // for (int i = 0; i < FACTOR; i++) {
        // System.out.println("Modifying revision " + i + " and " + (ELEMENTS / FACTOR) + " elements");
        // boolean continueFlag = true;
        // for (int j = 0; j < ELEMENTS / FACTOR && continueFlag; j++) {
        // try {
        // final long keyToAdapt = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;
        // final DumbNode node = BenchUtils.generateOne();
        // node.setNodeKey(keyToAdapt);
        // trx.setNode(node);
        // } catch (Exception e) {
        // System.err.println("Exception " + e + " thrown in factor " + i + "  and Elements " + j);
        // continueFlag = false;
        // }
        // }
        // if (continueFlag) {
        // long commitstart = System.currentTimeMillis();
        // System.out.println("Revision " + i + " before commit");
        // trx.commit();
        // System.out.println("Commit of revision " + i + " finished in "
        // + (System.currentTimeMillis() - commitstart) + "ms");
        // } else {
        // System.out.println("Revision " + i + " skipped");
        // i--;
        // trx.close();
        // trx = session.beginBucketWtx();
        // }
        // }
        // long endtimeMod = System.currentTimeMillis();
        // System.out
        // .println("Modifying nodes in " + FACTOR + " versions took " + (endtimeMod - endtime) + "ms");
        //
        // trx.close();
        // session.close();
        // storage.close();

    }

    private void get(int numbersToGet, boolean random) throws TTIOException {
        for (int i = 0; i < numbersToGet; i++) {
            if (random) {
                long nextKey = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;
                mTrx.getNode(nextKey);
            } else {
                mTrx.getNode(i % ELEMENTS);
            }
            if (i % 1024 == 0) {
                System.out.println(i + " elements read of " + numbersToGet + " with random=" + random);
            }

        }
    }

    @BeforeEachRun
    public void setUp() throws TTException {
        mStorage = Storage.openStorage(benchedFile);
        mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        mTrx = mSession.beginBucketRtx(mSession.getMostRecentVersion());
    }

    @Bench
    public void random016384() throws TTException {
        get(16384, true);
        System.out.println("163842");
    }

    @Bench
    public void random032768() throws TTException {
        get(32768, true);
        System.out.println("32768");
    }

    // @Bench
    // public void random065536() throws TTException {
    // get(65536, true);
    // System.out.println("65536");
    // }
    //
    // @Bench
    // public void random131072() throws TTException {
    // get(131072, true);
    // System.out.println("131072");
    // }
    //
    // @Bench
    // public void random262144() throws TTException {
    // get(262144, true);
    // System.out.println("262144");
    // }

    @Bench
    public void seq016384() throws TTException {
        get(16384, false);
        System.out.println("163842");
    }

    @Bench
    public void seq032768() throws TTException {
        get(32768, false);
        System.out.println("32768");
    }

    // @Bench
    // public void seq065536() throws TTException {
    // get(65536, false);
    // System.out.println("65536");
    // }
    //
    // @Bench
    // public void seq131072() throws TTException {
    // get(131072, false);
    // System.out.println("131072");
    // }
    //
    // @Bench
    // public void seq262144() throws TTException {
    // get(262144, false);
    // System.out.println("262144");
    // }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.close();
    }

    final static File outputFold = new File("/Users/sebi/listenerBench");

    public static void main(String[] args) {

        final File resultFold = new File("/Users/sebi/resBench");
        IOUtils.recursiveDelete(outputFold);
        IOUtils.recursiveDelete(resultFold);
        outputFold.mkdirs();
        resultFold.mkdirs();

        Benchmark bench = new Benchmark(new Config());
        bench.add(GetBench.class);
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
