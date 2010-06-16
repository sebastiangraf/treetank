package com.treetank;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.Session;
import com.treetank.api.IDatabase;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory.StorageType;
import com.treetank.node.ElementNode;
import com.treetank.page.NodePage;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.ERevisioning;

/**
 * 
 * Helper class for offering convenient usage of {@link Session}s for test
 * cases.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TestHelper {

	public enum PATHS {

		// PATH1
		PATH1(new File(new StringBuilder(File.separator).append("tmp").append(
				File.separator).append("tnk").append(File.separator).append(
				"path1").toString())),

		// PATH2
		PATH2(new File(new StringBuilder(File.separator).append("tmp").append(
				File.separator).append("tnk").append(File.separator).append(
				"path2").toString()));

		final File file;

		PATHS(final File paramFile) {
			file = paramFile;
		}

		public File getFile() {
			return file;
		}

	}

	private final static Map<File, DatabaseConfiguration> configs = new HashMap<File, DatabaseConfiguration>();
	public final static Random random = new Random();

	@Test
	public void testDummy() {
		// Just empty to ensure maven running
	}

	@Ignore
	public static final IDatabase getDatabase(final File file) {
		final DatabaseConfiguration config = configs.get(file);
		try {
			if (config != null) {
				Database.createDatabase(config);
			}
			return Database.openDatabase(file);
		} catch (final TreetankException exc) {
			fail(exc.toString());
			return null;
		}
	}

	@Ignore
	public static final void setDB(final String storageKind,
			final String revisionKind, final int revisions, final File file)
			throws TreetankUsageException {
		final StorageType type = StorageType.valueOf(revisionKind);
		final ERevisioning revision = ERevisioning.valueOf(revisionKind);

		final Properties props = new Properties();
		props.put(EDatabaseSetting.STORAGE_TYPE.name(), type);
		props.put(EDatabaseSetting.REVISION_TYPE.name(), revision);
		props.put(EDatabaseSetting.REVISION_TO_RESTORE.name(), revisions);
		final DatabaseConfiguration config = new DatabaseConfiguration(file,
				props);
		configs.put(file, config);
	}

	@Ignore
	public static final void deleteEverything() {
		Database.truncateDatabase(PATHS.PATH1.getFile());
		Database.truncateDatabase(PATHS.PATH2.getFile());

	}

	@Ignore
	public static final void closeEverything() {
		try {
			Database.forceCloseDatabase(PATHS.PATH1.getFile());
			Database.forceCloseDatabase(PATHS.PATH2.getFile());
		} catch (final TreetankException exc) {
			fail(exc.toString());
		}
	}

	@Ignore
	public static NodePage getNodePage(final long revision, final int offset,
			final int length) {
		final NodePage page = new NodePage(0, revision);
		for (int i = offset; i < length; i++) {
			page.setNode(i, new ElementNode(random.nextLong(), random
					.nextLong(), random.nextLong(), random.nextLong(), random
					.nextLong(), random.nextInt(), random.nextInt(), random
					.nextInt()));
		}
		return page;
	}

}
