/**
 * 
 */
package org.treetank.access.conf;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

/**
 * Test for {@link ResourceConfiguration}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class ResourceConfigurationTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

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
     * {@link org.treetank.access.conf.ResourceConfiguration#serialize(org.treetank.access.conf.ResourceConfiguration)}
     * and {@link org.treetank.access.conf.ResourceConfiguration#deserialize(java.io.File)}.
     */
    @Test
    public void testDeSerialize() throws Exception {
        TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        ResourceConfiguration resConf =
            mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10);
        TestHelper.createResource(resConf);
        ResourceConfiguration serializedConf =
            ResourceConfiguration.deserialize(new File(new File(TestHelper.PATHS.PATH1.getFile(),
                DatabaseConfiguration.Paths.Data.getFile().getName()), TestHelper.RESOURCENAME));
        assertEquals(resConf.toString(), serializedConf.toString());

    }
}
