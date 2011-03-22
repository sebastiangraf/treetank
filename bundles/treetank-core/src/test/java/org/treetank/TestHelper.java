/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank;

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

import org.treetank.access.Database;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.Session;
import org.treetank.access.WriteTransaction;
import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.api.IDatabase;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.AbsIOFactory.StorageType;
import org.treetank.node.AttributeNode;
import org.treetank.node.DeletedNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.page.NodePage;
import org.treetank.settings.ECharsForSerializing;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.settings.ERevisioning;

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
        } catch (final AbsTTException exc) {
            fail(exc.toString());
            return null;
        }
    }

    @Ignore
    public static final void setDB(final File file, final String hashKind) throws TTUsageException {

        final Properties props = new Properties();
        props.put(EDatabaseSetting.HASHKIND_TYPE.name(), hashKind);
        final DatabaseConfiguration config = new DatabaseConfiguration(file, props);
        configs.put(file, config);
    }

    @Ignore
    public static final void setDB(final StorageType storageKind, final ERevisioning revisionKind,
        final int revisions, final File file, final HashKind hashKind) throws TTUsageException {

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
        } catch (final AbsTTException exc) {
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
