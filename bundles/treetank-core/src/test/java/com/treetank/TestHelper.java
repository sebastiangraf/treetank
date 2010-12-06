package com.treetank;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.Session;
import com.treetank.access.WriteTransaction;
import com.treetank.access.WriteTransaction.HashKind;
import com.treetank.api.IDatabase;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbsIOFactory.StorageType;
import com.treetank.node.AttributeNode;
import com.treetank.node.DeletedNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
import com.treetank.page.NodePage;
import com.treetank.settings.ECharsForSerializing;
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
            PATH1(new File(new StringBuilder(File.separator).append("tmp").append(File.separator).append(
                "tnk").append(File.separator).append("path1").toString())),

            // PATH2
            PATH2(new File(new StringBuilder(File.separator).append("tmp").append(File.separator).append(
                "tnk").append(File.separator).append("path2").toString()));

        final File file;

        PATHS(final File paramFile) {
            file = paramFile;
        }

        public File getFile() {
            return file;
        }

    }

    private final static Map<File, DatabaseConfiguration> configs =
        new HashMap<File, DatabaseConfiguration>();

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
    public static final void setDB(final File file, final String hashKind) throws TreetankUsageException {

        final Properties props = new Properties();
        props.put(EDatabaseSetting.HASHKIND_TYPE.name(), hashKind);
        final DatabaseConfiguration config = new DatabaseConfiguration(file, props);
        configs.put(file, config);
    }

    @Ignore
    public static final void setDB(final StorageType storageKind, final ERevisioning revisionKind,
        final int revisions, final File file, final HashKind hashKind) throws TreetankUsageException {

        final Properties props = new Properties();
        props.put(EDatabaseSetting.STORAGE_TYPE.name(), storageKind);
        props.put(EDatabaseSetting.REVISION_TYPE.name(), revisionKind);
        props.put(EDatabaseSetting.REVISION_TO_RESTORE.name(), revisions);
        props.put(EDatabaseSetting.HASHKIND_TYPE, hashKind);
        final DatabaseConfiguration config = new DatabaseConfiguration(file, props);
        configs.put(file, config);
    }

    @Ignore
    public static final void deleteEverything() {
        if (PATHS.PATH1.getFile().exists()) {
            assertTrue(Database.truncateDatabase(PATHS.PATH1.getFile()));
        }
        if (PATHS.PATH2.getFile().exists()) {
            assertTrue(Database.truncateDatabase(PATHS.PATH2.getFile()));
        }

        configs.clear();

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
    public static NodePage getNodePage(final long revision, final int offset, final int length,
        final long nodePageKey) {
        final NodePage page = new NodePage(nodePageKey, revision);
        for (int i = offset; i < length; i++) {
            switch (random.nextInt(6)) {
            case 0:
                page.setNode(i, AttributeNode.createData(random.nextLong(), random.nextLong(), random
                    .nextInt(), random.nextInt(), random.nextInt(), new byte[] {
                    0, 1, 2, 3, 4
                }));
                break;
            case 1:
                page.setNode(i, DeletedNode.createData(random.nextLong(), random.nextLong()));
                break;
            case 2:
                page.setNode(i, ElementNode.createData(random.nextLong(), random.nextLong(), random
                    .nextLong(), random.nextLong(), random.nextLong(), random.nextLong(), random.nextInt(),
                    random.nextInt(), random.nextInt(), random.nextLong()));
                break;
            case 3:
                page.setNode(i, NamespaceNode.createData(random.nextLong(), random.nextLong(), random
                    .nextInt(), random.nextInt()));
                break;
            case 4:
                page.setNode(i,

                DocumentRootNode.createData());
                break;
            case 5:
                page.setNode(i, TextNode.createData(random.nextLong(), random.nextLong(), random.nextLong(),
                    random.nextLong(), random.nextInt(), new byte[] {
                        0, 1
                    }));
                break;
            }

        }
        return page;
    }

    /**
     * Read a file into a StringBuilder.
     * 
     * @param paramFile
     *            The file to read.
     * @param paramWhitespaces
     *            Retrieve file and don't remove any whitespaces.
     * @return StringBuilder instance, which has the string representation of
     *         the document.
     * @throws IOException
     *             throws an IOException if any I/O operation fails.
     */
    @Ignore("Not a test, utility method only")
    public static StringBuilder readFile(final File paramFile, final boolean paramWhitespaces)
        throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(paramFile));
        final StringBuilder sBuilder = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (paramWhitespaces) {
                sBuilder.append(line + ECharsForSerializing.NEWLINE);
            } else {
                sBuilder.append(line.trim());
            }
        }

        // Remove last newline.
        if (paramWhitespaces) {
            sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
        }
        in.close();

        return sBuilder;
    }

}
