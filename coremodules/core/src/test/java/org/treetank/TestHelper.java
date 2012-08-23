/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.treetank.access.Database;
import org.treetank.access.Session;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.INode;
import org.treetank.exception.TTException;
import org.treetank.io.IConstants;
import org.treetank.page.DumbNodeFactory.DumbNode;
import org.treetank.page.NodePage;

import com.google.common.io.Files;

/**
 * 
 * Helper class for offering convenient usage of {@link Session}s for test
 * cases.
 * 
 * This includes instantiation of databases plus resources.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TestHelper {

    public static final String RESOURCENAME = "tmp";

    /** Paths where the data is stored to. */
    public enum PATHS {

        // PATH1
            PATH1(new File(new StringBuilder(Files.createTempDir().getAbsolutePath()).append(File.separator)
                .append("tnk").append(File.separator).append("path1").toString())),

            // PATH2
            PATH2(new File(new StringBuilder(Files.createTempDir().getAbsolutePath()).append(File.separator)
                .append("tnk").append(File.separator).append("path2").toString()));

        final File file;

        final DatabaseConfiguration config;

        PATHS(final File paramFile) {
            file = paramFile;
            config = new DatabaseConfiguration(paramFile);
        }

        public File getFile() {
            return file;
        }

        public DatabaseConfiguration getConfig() {
            return config;
        }

    }

    /** Common random instance for generating common tag names. */
    public final static Random random = new Random();

    private final static Map<File, IDatabase> INSTANCES = new Hashtable<File, IDatabase>();

    /**
     * Getting a database and create one of not existing. This includes the
     * creation of a resource with the settings in the builder as standard.
     * 
     * @param file
     *            to be created
     * @return a database-obj
     * @throws TTException
     */
    public static final IDatabase getDatabase(final File file) throws TTException {
        if (INSTANCES.containsKey(file)) {
            return INSTANCES.get(file);
        } else {
            final DatabaseConfiguration config = new DatabaseConfiguration(file);
            if (!file.exists()) {
                Database.createDatabase(config);
            }
            final IDatabase database = Database.openDatabase(file);
            INSTANCES.put(file, database);
            return database;
        }
    }

    public static final Properties createProperties() {
        Properties returnVal = new Properties();
        returnVal.setProperty(IConstants.DBFILE, TestHelper.PATHS.PATH1.getFile().getAbsolutePath());
        returnVal.setProperty(IConstants.RESOURCE, RESOURCENAME);
        return returnVal;
    }

    public static final boolean createResource(final ResourceConfiguration resConf) throws TTException {
        final IDatabase database = TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
        return database.createResource(resConf);
    }

    /**
     * Deleting all resources as defined in the enum {@link PATHS}.
     * 
     * @throws TTException
     */
    public static final void deleteEverything() throws TTException {
        closeEverything();
        Database.truncateDatabase(PATHS.PATH1.config);
        Database.truncateDatabase(PATHS.PATH2.config);
    }

    /**
     * Closing all resources as defined in the enum {@link PATHS}.
     * 
     * @throws TTException
     */
    public static final void closeEverything() throws TTException {
        if (INSTANCES.containsKey(PATHS.PATH1.getFile())) {
            final IDatabase database = INSTANCES.remove(PATHS.PATH1.getFile());
            database.close();
        }
        if (INSTANCES.containsKey(PATHS.PATH2.getFile())) {
            final IDatabase database = INSTANCES.remove(PATHS.PATH2.getFile());
            database.close();
        }
    }

    /**
     * Generating random bytes.
     * 
     * @return the random bytes
     */
    public static final byte[] generateRandomBytes(final int pSize) {
        final byte[] returnVal = new byte[pSize];
        random.nextBytes(returnVal);
        return returnVal;
    }

    /**
     * Getting a node pages filled with nodes.
     * 
     * @param revision
     *            of the page
     * @param offset
     *            offset to start within the page
     * @param length
     *            length of the page
     * @param nodePageKey
     *            key of the nodepage
     * @return a {@link NodePage} filled
     */
    public static NodePage getNodePage(final long revision, final int offset, final int length,
        final long nodePageKey) {
        final NodePage page = new NodePage(nodePageKey, revision);
        for (int i = offset; i < length; i++) {
            page.setNode(i, new DumbNode(random.nextLong(), random.nextLong()));
        }
        return page;
    }

    /**
     * Generating one single {@link DumbNode} with random values.
     * 
     * @return one {@link DumbNode} with random values.
     */
    public static final INode generateOne() {
        return new DumbNode(TestHelper.random.nextLong(), TestHelper.random.nextLong());
    }

}
