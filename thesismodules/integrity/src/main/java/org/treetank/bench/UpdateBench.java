/**
 * 
 */
package org.treetank.bench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.treetank.exception.TTIOException;
import org.treetank.io.IOUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class UpdateBench {

    private final String RESOURCENAME = "benchResourcegrave9283";

    private final File bootstrappedFile = FileSystems.getDefault().getPath("tmp", "bootstrapped").toFile();
    private final File benchedFile = FileSystems.getDefault().getPath("tmp", "bench").toFile();

    private static final int FACTOR = 8;

    private final int ELEMENTS = 524288;

    private IStorage mStorage;
    private ISession mSession;
    private DumbNode[] mNodesToInsert = BenchUtils.createNodes(new int[] {
        ELEMENTS
    })[0];
    private IBucketWriteTrx mTrx;

    public UpdateBench() throws TTException {
        final Injector inj =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(DumbNodeFactory.class).setMetaFacClass(
                DumbMetaEntryFactory.class).createModule());

        final ResourceConfiguration resConfig =
            inj.getInstance(IResourceConfigurationFactory.class).create(
                StandardSettings.getProps(benchedFile.getAbsolutePath(), RESOURCENAME));
        IOUtils.recursiveDelete(benchedFile);
        final StorageConfiguration storConfig = new StorageConfiguration(benchedFile);
        Storage.createStorage(storConfig);
        final IStorage storage = Storage.openStorage(benchedFile);
        storage.createResource(resConfig);
        final ISession session =
            storage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
        final IBucketWriteTrx trx = session.beginBucketWtx();
        long time = System.currentTimeMillis();
        for (int i = 0; i < ELEMENTS; i++) {
            final long nodeKey = trx.incrementNodeKey();
            mNodesToInsert[i].setNodeKey(nodeKey);
            trx.setNode(mNodesToInsert[i]);
        }
        trx.commitBlocked();
        long endtime = System.currentTimeMillis();
        System.out.println("Generating nodes took " + (endtime - time) + "ms");
        trx.close();
        session.close();
        storage.close();

        IOUtils.recursiveDelete(bootstrappedFile);
        try {
            copyDirectory(benchedFile, bootstrappedFile);
        } catch (IOException e) {
            throw new TTIOException(e);
        }

    }

    private void toModify(int numbersToModify) throws TTException {
        for (int i = 1; i <= numbersToModify; i++) {
            final long keyToAdapt = Math.abs(BenchUtils.random.nextLong()) % ELEMENTS;

            final DumbNode node = BenchUtils.generateOne();
            node.setNodeKey(keyToAdapt);

            mTrx.setNode(node);
            if (i % (numbersToModify / FACTOR) == 0) {
                mTrx.commit();
            }
        }
        mTrx.close();
    }

    @BeforeEachRun
    public void setUp() throws TTException {
        try {
            IOUtils.recursiveDelete(benchedFile);
            copyDirectory(bootstrappedFile, benchedFile);
            mStorage = Storage.openStorage(benchedFile);
            mSession = mStorage.getSession(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
            mTrx = mSession.beginBucketWtx();
        } catch (IOException e) {
            throw new TTIOException(e);
        }
    }

    @Bench
    public void bench16384() throws TTException {
        toModify(16384);
        System.out.println("163842");
    }

    @Bench
    public void bench32768() throws TTException {
        toModify(32768);
        System.out.println("32768");
    }

    @Bench
    public void bench65536() throws TTException {
        toModify(65536);
        System.out.println("65536");
    }

    @Bench
    public void bench131072() throws TTException {
        toModify(131072);
        System.out.println("131072");
    }

    @Bench
    public void bench262144() throws TTException {
        toModify(262144);
        System.out.println("262144");
    }

    @Bench
    public void bench524288() throws TTException {
        toModify(524288);
        System.out.println("524288");
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

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    static class Config extends AbstractConfig {

        private final static int RUNS = 10;
        private final static Set<AbstractMeter> METERS = new HashSet<AbstractMeter>();
        private final static Set<AbstractOutput> OUTPUT = new HashSet<AbstractOutput>();

        private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
        private final static double GCPROB = 1.0d;

        static {
            METERS.add(new TimeMeter(Time.MilliSeconds));
            METERS.add(new MemMeter(Memory.Byte));

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
