package com.treetank.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Map;
import java.util.Properties;

import com.treetank.TestHelper;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory.StorageType;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.settings.EDatabaseSetting;

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
	 * Static method to get {@link DatabaseConfiguration}
	 * 
	 * @param type
	 *            for the the {@link DatabaseConfiguration} should be generated
	 * @return a suitable {@link DatabaseConfiguration}
	 * @throws TreetankUsageException
	 */
	public static DatabaseConfiguration createDBConf(final StorageType type)
			throws TreetankUsageException {
		final Properties props = new Properties();
		props.setProperty(EDatabaseSetting.REVISION_TYPE.name(), type.name());
		return new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile(),
				props);
	}

	/**
	 * Static method to get {@link SessionConfiguration}
	 * 
	 * @return a suitable {@link SessionConfiguration}
	 * @throws TreetankUsageException
	 */
	public static SessionConfiguration createSessionConf()
			throws TreetankUsageException {
		return new SessionConfiguration();
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
	 * @param dbConf
	 *            to be tested
	 * @param sessionConf
	 *            to be tested
	 */
	public static void testFactory(final DatabaseConfiguration dbConf,
			final SessionConfiguration sessionConf) throws TreetankException {
		final AbstractIOFactory fac1 = AbstractIOFactory.getInstance(dbConf,
				sessionConf);
		final AbstractIOFactory fac2 = AbstractIOFactory.getInstance(dbConf,
				sessionConf);
		assertSame(fac1, fac2);
		fac1.closeStorage();
		final AbstractIOFactory fac3 = AbstractIOFactory.getInstance(dbConf,
				sessionConf);
		assertNotSame(fac1, fac3);
		fac3.closeStorage();
	}

	public static void testReadWriteFirstRef(
			final DatabaseConfiguration dbConf,
			final SessionConfiguration sessionConf) throws TreetankException {

		final AbstractIOFactory fac = AbstractIOFactory.getInstance(dbConf,
				sessionConf);
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
