package org.treetank.jscsi;

import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTException;
import org.treetank.iscsi.data.BlockDataElementFactory;
import org.treetank.iscsi.data.ISCSIMetaPageFactory;
import org.treetank.iscsi.jscsi.TreetankStorageModule;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.ModuleFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This class writes a lot of data into a big storage module,
 * multiple times to test the performance on the system.
 * 
 * @author Andreas Rain
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class StorageModuleBenchmarkSpecificBackend {

    private final static int NODENUMBER = 2048; // 2GB Storage

    private TreetankStorageModule storageModule;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    /**
     * Setup method for this test.
     * 
     * @throws TTException
     * @throws InterruptedException
     */
    // Activate to run this before class. Bootstrap does not work and leads to the bug.
    @BeforeClass
    public void setUp() throws TTException, InterruptedException {
        CoreTestHelper.deleteEverything();

        final Injector injector =
            com.google.inject.Guice.createInjector(new ModuleSetter().setDataFacClass(
                BlockDataElementFactory.class).setMetaFacClass(ISCSIMetaPageFactory.class).createModule());
        final IResourceConfigurationFactory resFac =
            injector.getInstance(IResourceConfigurationFactory.class);

        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        final ResourceConfiguration resConf = resFac.create(props);

        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        CoreTestHelper.Holder.generateSession(holder, resConf);
        storageModule = new TreetankStorageModule(NODENUMBER, holder.getSession());

    }

    /**
     * Method to clear data that has been written during tests.
     * 
     * @throws TTException
     */
    @AfterClass
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test functionality of read and write transactions.
     * 
     * @throws TTException
     * @throws IOException
     */

    @Test
    public void benchStorage() throws TTException, IOException {
        long startTime;

        // Write simple.
        final byte[] writeArray = new byte[2 * TreetankStorageModule.BYTES_IN_DATA];
        CoreTestHelper.random.nextBytes(writeArray);
        System.out.println("Writing simply.");
        for (int j = 0; j < 10; j++) {
            startTime = System.currentTimeMillis();
            System.out.println("Starting run " + j);
            for (long i = 0; i < NODENUMBER / 2; i++) {
                // write
                storageModule.write(writeArray, i * 2 * TreetankStorageModule.BYTES_IN_DATA);
            }
            printRunTimeTaken(j, System.currentTimeMillis() - startTime);
        }
        System.out.println("Finished.");

        // Write distributed.
        System.out.println("Writing distributed.");
        for (int j = 0; j < 10; j++) {
            startTime = System.currentTimeMillis();
            System.out.println("Starting run " + j);
            for (long i = 0; i < NODENUMBER / 2; i++) {
                // write
                storageModule.write(writeArray, i * 2 * TreetankStorageModule.BYTES_IN_DATA);
            }
            printRunTimeTaken(j, System.currentTimeMillis() - startTime);
        }
        System.out.println("Finished.");

        // Write bigger Junks.
        System.out.println("Writing bigger Junks.");
        final byte[] writeArray2 = new byte[32 * TreetankStorageModule.BYTES_IN_DATA];
        CoreTestHelper.random.nextBytes(writeArray2);
        for (int j = 0; j < 10; j++) {
            startTime = System.currentTimeMillis();
            System.out.println("Starting run " + j);
            for (long i = 0; i < NODENUMBER / 64; i = i + 10) {
                // write
                storageModule.write(writeArray2, i * 2 * TreetankStorageModule.BYTES_IN_DATA);
            }
            printRunTimeTaken(j, System.currentTimeMillis() - startTime);
        }
        System.out.println("Finished.");

        // Reading again.
        System.out.println("Reading.");
        for (int j = 0; j < 10; j++) {
            startTime = System.currentTimeMillis();
            System.out.println("Starting run " + j);
            for (long i = 0; i < NODENUMBER / 2; i++) {
                // write
                storageModule.read(writeArray, i * 2 * TreetankStorageModule.BYTES_IN_DATA);
            }
            printRunTimeTaken(j, System.currentTimeMillis() - startTime);
        }
        System.out.println("Finished.");

        // Reading bigger Junks.
        System.out.println("Reading bigger Junks.");
        for (int j = 0; j < 10; j++) {
            startTime = System.currentTimeMillis();
            System.out.println("Starting run " + j);
            for (long i = 0; i < NODENUMBER / 64; i = i + 10) {
                // write
                storageModule.read(writeArray2, i * 2 * TreetankStorageModule.BYTES_IN_DATA);
            }
            printRunTimeTaken(j, System.currentTimeMillis() - startTime);
        }
        System.out.println("Finished.");

    }

    private void printRunTimeTaken(int pRunNumber, long pTime) {
        System.out.println("Run " + pRunNumber + " took " + pTime);
    }

}
