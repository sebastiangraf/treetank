/**
 * 
 */
package com.treetank.service.jaxrx.util;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * This class tests {@link RESTResponseHelper}.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public class RESTResponseHelperTest {

    /**
     * Test method for
     * {@link org.treetank.rest.util.RESTResponseHelper#buildResponseOfDomLR(java.util.Map)}
     * .
     */
    @Test
    public final void testBuildResponseOfDomLR() {
        final Map<String, String> availResources = new HashMap<String, String>();
        final File resourcesDir = new File(RESTProps.STOREDBPATH);
        final File[] files = resourcesDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    final String dirName = file.getAbsoluteFile().getName();
                    if (dirName.endsWith(RESTProps.TNKEND)) {
                        availResources.put(dirName, "resource");
                    } else if (dirName.endsWith(RESTProps.COLEND)) { // NOPMD no
                                                                     // idea how
                                                                     // to solve
                                                                     // this
                        availResources.put(dirName, "collection");
                    }
                }
            }
        }
        assertNotNull("checks if not null", RESTResponseHelper
                .buildResponseOfDomLR(availResources));

    }

}
