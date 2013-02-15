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
import org.treetank.CoreTestHelper;
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
        CoreTestHelper.deleteEverything();
        CoreTestHelper.getStorage(CoreTestHelper.PATHS.PATH1.getFile());
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.access.conf.ResourceConfiguration#serialize(org.treetank.access.conf.ResourceConfiguration)}
     * and {@link org.treetank.access.conf.ResourceConfiguration#deserialize(File, String)}.
     */
    @Test
    public void testDeSerialize() throws Exception {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME);
        CoreTestHelper.getStorage(CoreTestHelper.PATHS.PATH1.getFile());
        ResourceConfiguration resConf = mResourceConfig.create(props);
        CoreTestHelper.createResource(resConf);
        ResourceConfiguration serializedConf =
            ResourceConfiguration.deserialize(new File(props.getProperty(ConstructorProps.STORAGEPATH)),
                props.getProperty(ConstructorProps.RESOURCE));
        assertEquals(resConf.toString(), serializedConf.toString());

    }
}
