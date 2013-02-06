/**
 * 
 */
package org.treetank.log;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.log.LogValue.LogValueBinding;
import org.treetank.page.IConstants;

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

    @Test
    public void test() {

        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        ResourceConfiguration conf = mResourceConfig.create(props);

        LogValueBinding binding = new LogValueBinding(conf.mNodeFac, conf.mMetaFac);
        for (int i = 0; i < NUMBEROFELEMENTS; i++) {
            LogValue value =
                new LogValue(CoreTestHelper.getNodePage(0, IConstants.CONTENT_COUNT, 0), CoreTestHelper
                    .getNodePage(0, IConstants.CONTENT_COUNT, 0));
            TupleOutput output = new TupleOutput();
            binding.objectToEntry(value, output);

            TupleInput input = new TupleInput(output);
            LogValue toCompare = binding.entryToObject(input);
            assertEquals(value, toCompare);
        }

    }
}
