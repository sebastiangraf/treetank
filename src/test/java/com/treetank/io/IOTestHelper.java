package com.treetank.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Map;
import java.util.Properties;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory.StorageType;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.FixedProperties;
import com.treetank.utils.SettableProperties;

/**
 * Helper class for testing the io interfaces
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class IOTestHelper {

    private IOTestHelper() {
    }

    /**
     * Static method to get {@link SessionConfiguration}
     * 
     * @param type
     *            for the the {@link SessionConfiguration} should be generated
     * @return a suitable {@link SessionConfiguration}
     * @throws TreetankUsageException
     */
    public static SessionConfiguration createConf(final StorageType type)
            throws TreetankUsageException {
        final Properties props = new Properties();
        props.put(SettableProperties.STORAGE_TYPE, type);
        return new SessionConfiguration(ITestConstants.PATH1, props);
    }

    /**
     * Tear down for all tests related to the io layer.
     */
    public static void clean() throws TreetankException {

        final Map<SessionConfiguration, AbstractIOFactory> mapping = AbstractIOFactory
                .getActiveFactories();
        for (final SessionConfiguration conf : mapping.keySet()) {
            final AbstractIOFactory fac = mapping.get(conf);

            // Closing all storages
            fac.closeStorage();
            TestHelper.deleteEverything();
        }

    }

    /**
     * Testing the get and remove in the Factory methods.
     * 
     * @param conf
     *            to be tested
     */
    public static void testFactory(final SessionConfiguration conf)
            throws TreetankException {
        final AbstractIOFactory fac1 = AbstractIOFactory.getInstance(conf);
        final AbstractIOFactory fac2 = AbstractIOFactory.getInstance(conf);
        assertSame(fac1, fac2);
        fac1.closeStorage();
        final AbstractIOFactory fac3 = AbstractIOFactory.getInstance(conf);
        assertNotSame(fac1, fac3);
        fac3.closeStorage();
    }

    public static void testPropsReadWrite(final SessionConfiguration conf)
            throws TreetankException {
        final StorageProperties props = new StorageProperties(
                (Integer) FixedProperties.LAST_VERSION_MAJOR
                        .getStandardProperty(),
                (Integer) FixedProperties.LAST_VERSION_MINOR
                        .getStandardProperty());

        final AbstractIOFactory fac = AbstractIOFactory.getInstance(conf);
        // same instance check
        final IWriter writer = fac.getWriter();
        writer.setProps(props);
        final StorageProperties props2 = writer.getProps();
        assertEquals(props, props2);
        writer.close();

        // new instance check
        final IReader reader = fac.getReader();
        assertEquals(props, reader.getProps());
        reader.close();

    }

    public static void testReadWriteFirstRef(final SessionConfiguration conf)
            throws TreetankException {

        final AbstractIOFactory fac = AbstractIOFactory.getInstance(conf);
        final PageReference pageRef1 = new PageReference();
        final UberPage page1 = new UberPage();
        pageRef1.setPage(page1);

        // same instance check
        final IWriter writer = fac.getWriter();
        writer.writeFirstReference(pageRef1);
        final PageReference pageRef2 = writer.readFirstReference();
        assertEquals(pageRef1.getNodePageKey(), pageRef2.getNodePageKey());
        assertEquals(((UberPage) pageRef1.getPage()).getRevisionCount(),
                ((UberPage) pageRef2.getPage()).getRevisionCount());
        writer.close();

        // new instance check
        final IReader reader = fac.getReader();
        final PageReference pageRef3 = reader.readFirstReference();
        assertEquals(pageRef1.getNodePageKey(), pageRef3.getNodePageKey());
        assertEquals(((UberPage) pageRef1.getPage()).getRevisionCount(),
                ((UberPage) pageRef3.getPage()).getRevisionCount());
        reader.close();

    }

    // public static void testReadWriteNodePageStructure(
    // final SessionConfiguration conf) throws TreetankException {
    // final AbstractIOFactory fac = AbstractIOFactory.getInstance(conf);
    // final PageReference pageRef1 = new PageReference();
    // final UberPage page1 = new UberPage();
    // pageRef1.setPage(page1);
    //
    // }
}
