/**
 * 
 */
package org.treetank.access.conf;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * Test case for de-/serialization of {@link DatabaseConfiguration}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DatabaseConfigurationTest {

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
        TestHelper.PATHS.PATH1.getFile().mkdirs();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.access.conf.DatabaseConfiguration#serialize(org.treetank.access.conf.DatabaseConfiguration)}
     * and {@link org.treetank.access.conf.DatabaseConfiguration#deserialize(java.io.File)}.
     * 
     * @throws TTIOException
     */
    @Test
    public void testDeSerialize() throws TTIOException {
        DatabaseConfiguration conf = new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile());
        DatabaseConfiguration.serialize(conf);
        DatabaseConfiguration serializedConf =
            DatabaseConfiguration.deserialize(TestHelper.PATHS.PATH1.getFile());
        assertEquals(conf, serializedConf);

    }
}
