/**
 * 
 */
package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.bucket.IConstants;
import org.treetank.exception.TTException;
import org.treetank.io.LogValue.LogValueBinding;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.ModuleFactory;

import com.google.inject.Inject;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class LogValueTest {

    private final static int NUMBEROFELEMENTS = 100;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mConf;

    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.getStorage(CoreTestHelper.PATHS.PATH1.getFile());
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mConf = mResourceConfig.create(props);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void test() throws TTException {

        LogValueBinding binding = new LogValueBinding(mConf.mDataFac, mConf.mMetaFac);
        for (int i = 0; i < NUMBEROFELEMENTS; i++) {
            LogValue value =
                new LogValue(CoreTestHelper.getDataBucket(0, IConstants.CONTENT_COUNT, 0, 0), CoreTestHelper
                    .getDataBucket(0, IConstants.CONTENT_COUNT, 0, 0));
            TupleOutput output = new TupleOutput();
            binding.objectToEntry(value, output);

            TupleInput input = new TupleInput(output);
            LogValue toCompare = binding.entryToObject(input);
            assertEquals(value, toCompare);
        }

    }
}
