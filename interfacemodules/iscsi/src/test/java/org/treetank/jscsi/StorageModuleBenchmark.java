package org.treetank.jscsi;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

/**
 * This class writes a lot of data into a big storage module,
 * multiple times to test the performance on the system.
 * @author Andreas Rain
 *
 */
@Guice(moduleFactory = ModuleFactory.class)
public class StorageModuleBenchmark {

    private final static int NODENUMBER = 524288; // 2GB Storage

    private TreetankStorageModule storageModule;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    /**
     * Setup method for this test.
     * 
     * @throws TTException
     * @throws InterruptedException 
     */
    @BeforeClass
    public void setUp() throws TTException, InterruptedException {
        CoreTestHelper.deleteEverything();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        CoreTestHelper.Holder.generateSession(holder, mResource);
        storageModule = new TreetankStorageModule(NODENUMBER, holder.getSession());
        
        while(!storageModule.isReady()){
            Thread.sleep(500);
        }
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
     * Check the logic of the checkBounds method.
     */
    @Test
    public void testBoundaries() {
        // invalid logical block address
        assertEquals(1, storageModule.checkBounds(-1, 1));
        // block addess out of range
        assertEquals(1, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE, 1));
        // length invalid
        assertEquals(2, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, - 1));
        // length out of range
        assertEquals(2, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, 2));
        // correct check
        assertEquals(0, storageModule.checkBounds(NODENUMBER * TreetankStorageModule.BLOCKS_IN_NODE - 1, 1));
    }

    /**
     * Test functionality of read and write transactions.
     * 
     * @throws TTException
     * @throws IOException
     */
    @Test
    public void benchStorage() throws TTException, IOException {
        final byte[] writeArray = new byte[2 * TreetankStorageModule.BYTES_IN_NODE];
        CoreTestHelper.random.nextBytes(writeArray);
        for (int j = 0; j < 10; j++) {
            System.out.println("Starting run " + j);
            for(long i = 0; i < NODENUMBER/2; i++){
                // write
                System.out.println("StorageIndex: " + (i * 2 * TreetankStorageModule.BYTES_IN_NODE));
                storageModule.write(writeArray, i * 2 * TreetankStorageModule.BYTES_IN_NODE);
            }
        }
    }
}