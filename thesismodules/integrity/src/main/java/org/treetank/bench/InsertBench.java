package org.treetank.bench;

import java.io.File;
import java.nio.file.FileSystems;

import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.Bench;
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
        1048576
    })[0];
    private IBucketWriteTrx mTrx;

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
    public void bench16384() throws TTException {
        insert(16384);
        mTrx.commit();
        System.out.println("16384");
    }

    @Bench
    public void bench32768() throws TTException {
        insert(32768);
        mTrx.commit();
        System.out.println("32768");
    }

    @Bench
    public void bench65536() throws TTException {
        insert(65536);
        mTrx.commit();
        System.out.println("65536");
    }

    @Bench
    public void bench131072() throws TTException {
        insert(131072);
        mTrx.commit();
        System.out.println("131072");
    }

    @Bench
    public void bench262144() throws TTException {
        insert(262144);
        mTrx.commit();
        System.out.println("262144");
    }

    @Bench
    public void bench524288() throws TTException {
        insert(524288);
        mTrx.commit();
        System.out.println("524288");
    }

    @Bench
    public void bench1048576() throws TTException {
        insert(1048576);
        mTrx.commit();
        System.out.println("1048576");
    }

    @Bench
    public void bench2097152() throws TTException {
        insert(2097152);
        mTrx.commit();
        System.out.println("2097152");
    }

    // @Bench
    // public void bench4194304() throws TTException {
    // insert(4194304);
    // mTrx.commit();
    // System.out.println("4194304");
    // }
    //
    // @Bench
    // public void bench8388608() throws TTException {
    // insert(8388608);
    // mTrx.commit();
    // System.out.println("8388608");
    // }
    //
    // @Bench
    // public void bench16777216() throws TTException {
    // insert(16777216);
    // mTrx.commit();
    // System.out.println("16777216");
    // }

    @AfterEachRun
    public void tearDown() throws TTException {
        mTrx.close();
        mSession.close();
        mStorage.truncateResource(new SessionConfiguration(RESOURCENAME, StandardSettings.KEY));
    }

    public static void main(String[] args) {
        Benchmark bench = new Benchmark();
        bench.add(InsertBench.class);
        BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
    }

}
