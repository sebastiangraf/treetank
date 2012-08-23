/**
 * 
 */
package org.treetank.access.conf;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.exception.TTException;
import org.treetank.io.IConstants;

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
        Properties props = TestHelper.createProperties();
        TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        ResourceConfiguration resConf = mResourceConfig.create(props, 10);
        TestHelper.createResource(resConf);
        ResourceConfiguration serializedConf =
            ResourceConfiguration.deserialize(new File(props.getProperty(IConstants.DBFILE)));
        assertEquals(resConf.toString(), serializedConf.toString());

    }
}
