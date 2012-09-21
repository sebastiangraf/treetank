/**
 * 
 */
package org.treetank.access.conf;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.access.Storage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * Test case for de-/serialization of {@link StorageConfiguration}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DatabaseConfigurationTest {

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.access.conf.StorageConfiguration#serialize(org.treetank.access.conf.StorageConfiguration)}
     * and {@link org.treetank.access.conf.StorageConfiguration#deserialize(java.io.File)}.
     * 
     * @throws TTIOException
     */
    @Test
    public void testDeSerialize() throws TTIOException {
        StorageConfiguration conf = new StorageConfiguration(TestHelper.PATHS.PATH1.getFile());
        assertTrue(Storage.createDatabase(conf));
        StorageConfiguration serializedConf =
            StorageConfiguration.deserialize(TestHelper.PATHS.PATH1.getFile());
        assertEquals(conf.toString(), serializedConf.toString());

    }
}
