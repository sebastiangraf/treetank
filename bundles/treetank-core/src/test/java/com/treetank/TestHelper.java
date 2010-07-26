package com.treetank;

import static org.junit.Assert.fail;

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
import com.treetank.api.IDatabase;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory.StorageType;
import com.treetank.node.AttributeNode;
import com.treetank.node.DeletedNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
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
    public static final void setDB(final String storageKind, final String revisionKind, final int revisions,
        final File file) throws TreetankUsageException {
        final StorageType type = StorageType.valueOf(revisionKind);
        final ERevisioning revision = ERevisioning.valueOf(revisionKind);

        final Properties props = new Properties();
        props.put(EDatabaseSetting.STORAGE_TYPE.name(), type);
        props.put(EDatabaseSetting.REVISION_TYPE.name(), revision);
        props.put(EDatabaseSetting.REVISION_TO_RESTORE.name(), revisions);
        final DatabaseConfiguration config = new DatabaseConfiguration(file, props);
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
    public static NodePage getNodePage(final long revision, final int offset, final int length) {
        final NodePage page = new NodePage(0, revision);
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
                    random.nextInt(), random.nextInt()));
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
     * @param file
     *            The file to read.
     * @param whitespaces
     *            Retrieve file and don't remove any whitespaces.
     * @return StringBuilder instance, which has the string representation of
     *         the document.
     * @throws IOException
     *             throws an IOException if any I/O operation fails.
     */
    @Ignore("Not a test, utility method only")
    public static final StringBuilder readFile(final File file, final boolean whitespaces) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        final StringBuilder sBuilder = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (whitespaces) {
                sBuilder.append(line + "\n");
            } else {
                sBuilder.append(line.trim());
            }
        }

        // Remove last newline.
        if (whitespaces) {
            sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
        }
        in.close();

        return sBuilder;
    }

}
