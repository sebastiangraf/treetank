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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.treetank.access.Session;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.DumbNodeFactory.DumbNode;
import org.treetank.page.IndirectPage;
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
public final class CoreTestHelper {

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

        final StorageConfiguration config;

        PATHS(final File paramFile) {
            file = paramFile;
            config = new StorageConfiguration(paramFile);
        }

        public File getFile() {
            return file;
        }

        public StorageConfiguration getConfig() {
            return config;
        }

    }

    /** Common random instance for generating common tag names. */
    public final static Random random = new Random(123l);

    private final static Map<File, IStorage> INSTANCES = new Hashtable<File, IStorage>();

    /**
     * Getting a database and create one of not existing. This includes the
     * creation of a resource with the settings in the builder as standard.
     * 
     * @param file
     *            to be created
     * @return a database-obj
     * @throws TTException
     */
    public static final IStorage getDatabase(final File file) throws TTException {
        if (INSTANCES.containsKey(file)) {
            return INSTANCES.get(file);
        } else {
            final StorageConfiguration config = new StorageConfiguration(file);
            if (!file.exists()) {
                Storage.createStorage(config);
            }
            final IStorage storage = Storage.openStorage(file);
            INSTANCES.put(file, storage);
            return storage;
        }
    }

    public static final boolean createResource(final ResourceConfiguration resConf) throws TTException {
        final IStorage storage = CoreTestHelper.getDatabase(CoreTestHelper.PATHS.PATH1.getFile());
        return storage.createResource(resConf);
    }

    /**
     * Deleting all resources as defined in the enum {@link PATHS}.
     * 
     * @throws TTException
     */
    public static final void deleteEverything() throws TTException {
        closeEverything();
        Storage.truncateStorage(PATHS.PATH1.config);
        Storage.truncateStorage(PATHS.PATH2.config);
    }

    /**
     * Closing all resources as defined in the enum {@link PATHS}.
     * 
     * @throws TTException
     */
    public static final void closeEverything() throws TTException {
        if (INSTANCES.containsKey(PATHS.PATH1.getFile())) {
            final IStorage storage = INSTANCES.remove(PATHS.PATH1.getFile());
            storage.close();
        }
        if (INSTANCES.containsKey(PATHS.PATH2.getFile())) {
            final IStorage storage = INSTANCES.remove(PATHS.PATH2.getFile());
            storage.close();
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
     * @param offset
     *            offset to start within the page
     * @param length
     *            length of the page
     * @param nodePageKey
     *            key of the nodepage
     * @return a {@link NodePage} filled
     */
    public static NodePage getNodePage(final int offset, final int length, final long nodePageKey) {
        final NodePage page = new NodePage(nodePageKey);
        for (int i = offset; i < length; i++) {
            page.setNode(i, generateOne());
        }
        return page;
    }

    /**
     * Generating one single {@link DumbNode} with random values.
     * 
     * @return one {@link DumbNode} with random values.
     */
    public static final INode generateOne() {
        return new DumbNode(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
    }

    /**
     * Getting a fake structure for testing consiting of different arranged pages.
     * This structure starts with the key 1 and incrementally sets a new pagekey for the defined offsets in
     * the indirectpages to simulate different versions and node-offsets.
     * The key retrieved thereby has always the value 6 (1 (starting) + 5 (number of indirect layers)
     * 
     * @param offsets
     *            an array with offsets internally of the tree.
     * @return a {@link IBackendReader}-mock
     * @throws TTIOException
     */
    public static IBackendReader getFakedStructure(int[] offsets) throws TTIOException {
        assertEquals(5, offsets.length);
        // mocking the reader
        IBackendReader reader = mock(IBackendReader.class);
        // variable storing the related keys to the pages created in the mock
        long pKey = 1;
        // iterating through the tree..
        for (int i = 0; i < offsets.length; i++) {
            // ...and create a new page.
            final IndirectPage page = new IndirectPage(pKey);
            long oldKey = pKey;
            // set the offsets until the defined parameter...
            for (int j = 0; j <= offsets[i]; j++) {
                // ...by setting the related key to the defined offset and...
                page.setReferenceKey(j, ++pKey);
            }
            // ...tell the mock to react when the key is demanded.
            when(reader.read(oldKey)).thenReturn(page);
        }
        // returning the mock
        return reader;
    }

    /**
     * Utility method to create nodes per revision.
     * 
     * @param nodesPerRevision
     *            to create
     * @param pWtx
     *            to store to.
     * @throws TTException
     */
    public static DumbNode[][] createRevisions(final int[] nodesPerRevision, final IPageWriteTrx pWtx)
        throws TTException {
        final DumbNode[][] returnVal = new DumbNode[nodesPerRevision.length][];
        for (int i = 0; i < nodesPerRevision.length; i++) {
            returnVal[i] = new DumbNode[nodesPerRevision[i]];
            // inserting nodes on this transaction
            for (int j = 0; j < nodesPerRevision[i]; j++) {
                returnVal[i][j] = new DumbNode(pWtx.incrementNodeKey(), CoreTestHelper.random.nextLong());
                pWtx.setNode(returnVal[i][j]);
                // pWtx.setNode(new DumbNode(pWtx.incrementNodeKey(), CoreTestHelper.random.nextLong()));
            }
            // comitting data
            pWtx.commit();
        }
        return returnVal;
        // return null;
    }

    /**
     * Checking the transaction with nodes written sequentially.
     * 
     * @param pNodes
     *            to be compared with
     * @param pRtx
     *            to check
     * @param pStartKey
     *            to start with
     * @re
     * @throws TTIOException
     */
    public static void checkStructure(final List<DumbNode> pNodes, final IPageReadTrx pRtx)
        throws TTIOException {
        long key = 0;
        for (DumbNode node : pNodes) {
            assertEquals(node, pRtx.getNode(key));
            key++;
        }
    }

    public static class Holder {

        IStorage mStorage;

        ISession mSession;

        IPageReadTrx mPageRTrx;
        IPageWriteTrx mPageWTrx;

        public static Holder generateStorage(ResourceConfiguration pConf) throws TTException {
            Holder holder = new Holder();
            holder.mStorage = CoreTestHelper.getDatabase(PATHS.PATH1.getFile());
            holder.mStorage.createResource(pConf);
            return holder;
        }

        public static Holder generateSession(ResourceConfiguration pConf) throws TTException {
            Holder holder = generateStorage(pConf);
            holder.mSession =
                holder.mStorage.getSession(new SessionConfiguration(CoreTestHelper.RESOURCENAME,
                    StandardSettings.KEY));
            return holder;
        }

        public static Holder generateWtx(ResourceConfiguration pConf) throws TTException {
            final Holder holder = generateSession(pConf);
            holder.mPageWTrx = holder.mSession.beginPageWriteTransaction();
            holder.mPageRTrx = holder.mPageWTrx;
            return holder;
        }

        public static Holder generateRtx(ResourceConfiguration pConf) throws TTException {
            final Holder holder = generateSession(pConf);
            holder.mPageRTrx =
                holder.mSession.beginPageReadTransaction(holder.mSession.getMostRecentVersion());
            return holder;
        }

        public IStorage getStorage() {
            return mStorage;
        }

        public ISession getSession() {
            return mSession;
        }

        public void close() throws TTException {
            if (mPageRTrx != null && !mPageRTrx.isClosed()) {
                mPageRTrx.close();
            }
            if (mPageWTrx != null && !mPageWTrx.isClosed()) {
                mPageWTrx.close();
            }
            mSession.close();
            mStorage.close();
        }
    }

}
