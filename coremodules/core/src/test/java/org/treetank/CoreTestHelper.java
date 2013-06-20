/**
 * c * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.treetank.access.Session;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IMetaEntry;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.DumbNodeFactory.DumbNode;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    public static final String RESOURCENAME = "grave928134589762";

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

    private final static Map<File, IStorage> INSTANCES = Maps.newHashMap();

    /**
     * Getting a database and create one of not existing. This includes the
     * creation of a resource with the settings in the builder as standard.
     * 
     * @param file
     *            to be created
     * @return a database-obj
     * @throws TTException
     */
    public static final IStorage getStorage(final File file) throws TTException {
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
        final IStorage storage = CoreTestHelper.getStorage(CoreTestHelper.PATHS.PATH1.getFile());
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
     * Getting a node buckets filled with nodes.
     * 
     * @param offset
     *            offset to start within the bucket
     * @param length
     *            length of the bucket
     * @param nodebucketKey
     *            key of the nodebucket
     * @param lastbucketKey
     *            key of the former bucket
     * @return a {@link NodeBucket} filled
     */
    public static final NodeBucket getNodeBucket(final int offset, final int length,
        final long nodeBucketKey, final long lastBucketKey) {
        final NodeBucket bucket = new NodeBucket(nodeBucketKey, lastBucketKey);
        for (int i = offset; i < length; i++) {
            bucket.setNode(i, generateOne());
        }
        return bucket;
    }

    /**
     * Generating one single {@link DumbNode} with random values.
     * 
     * @return one {@link DumbNode} with random values.
     */
    public static final DumbNode generateOne() {
        byte[] data = new byte[0];
        CoreTestHelper.random.nextBytes(data);
        return new DumbNode(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(), data);
    }

    /**
     * Getting a fake structure for testing consisting of different arranged buckets.
     * This structure starts with the key 1 and incrementally sets a new bucketkey for the defined offsets in
     * the indirectbuckets to simulate different versions and node-offsets.
     * The key retrieved thereby has always the value 6 (1 (starting) + 5 (number of indirect layers)
     * 
     * @param offsets
     *            an array with offsets internally of the tree.
     * @return a {@link IBackendReader}-mock
     * @throws TTIOException
     */
    public static final IBackendReader getFakedStructure(int[] offsets) throws TTIOException {
        assertEquals(5, offsets.length);
        // mocking the reader
        IBackendReader reader = mock(IBackendReader.class);
        // variable storing the related keys to the buckets created in the mock
        long pKey = 1;
        // iterating through the tree..
        for (int i = 0; i < offsets.length; i++) {
            // ...and create a new buckets.
            final IndirectBucket buckets = new IndirectBucket(pKey);
            long oldKey = pKey;
            // set the offsets until the defined parameter...
            for (int j = 0; j <= offsets[i]; j++) {
                // ...by setting the related key to the defined offset and...
                buckets.setReferenceKey(j, ++pKey);
            }
            // ...tell the mock to react when the key is demanded.
            when(reader.read(oldKey)).thenReturn(buckets);
        }
        // returning the mock
        return reader;
    }

    /**
     * Create nodes in different versions in Treetank and check directly afterwards the structure.
     * 
     * @param pHolder
     *            for getting the transaction
     * @return a two-dimensional array of nodes.
     * @throws TTException
     */
    public static final List<List<Map.Entry<DumbKey, DumbValue>>> createTestMeta(Holder pHolder)
        throws TTException {
        final int[] pNumbers = new int[10];
        Arrays.fill(pNumbers, 200);
        final List<List<Map.Entry<DumbKey, DumbValue>>> returnVal = Lists.newArrayList();
        // adding null for revision 0
        final List<Map.Entry<DumbKey, DumbValue>> firstRevList = Lists.newArrayList();
        returnVal.add(firstRevList);
        // adding all data for upcoming revisions
        for (int i = 0; i < pNumbers.length; i++) {
            IBucketWriteTrx wtx = pHolder.getSession().beginBucketWtx();
            List<Map.Entry<DumbKey, DumbValue>> dataPerVersion =
                insertMetaWithTransaction(pNumbers[i], wtx, returnVal.get(i));
            returnVal.add(dataPerVersion);
            wtx.close();
        }
        return returnVal;
    }

    /**
     * Create nodes in different versions in Treetank and check directly afterwards the structure.
     * 
     * @param pHolder
     *            for getting the transaction
     * @return a two-dimensional array of nodes.
     * @throws TTException
     */
    public static final DumbNode[][] createTestData(Holder pHolder) throws TTException {
        IBucketWriteTrx wtx = pHolder.getSession().beginBucketWtx();
        int[] nodesPerRevision = new int[2];
        Arrays.fill(nodesPerRevision, 128);
        DumbNode[][] nodes = CoreTestHelper.insertNodesWithTransaction(nodesPerRevision, wtx);
        checkStructure(combineNodes(nodes), wtx, 0);
        wtx.close();
        return nodes;
    }

    /**
     * Utility method to create nodes per revision.
     * 
     * @param pNodesPerRevision
     *            to create
     * @param pWtx
     *            to store to.
     * @throws TTException
     */
    public static final DumbNode[][] insertNodesWithTransaction(final int[] pNodesPerRevision,
        final IBucketWriteTrx pWtx) throws TTException {
        final DumbNode[][] returnVal = createNodes(pNodesPerRevision);
        for (int i = 0; i < returnVal.length; i++) {
            for (int j = 0; j < returnVal[i].length; j++) {
                final long nodeKey = pWtx.incrementNodeKey();
                returnVal[i][j].setNodeKey(nodeKey);
                pWtx.setNode(returnVal[i][j]);
                assertEquals(returnVal[i][j], pWtx.getNode(nodeKey));
            }
            checkStructure(Arrays.asList(returnVal[i]), pWtx, i * returnVal[i].length);
            pWtx.commit();
        }
        return returnVal;
    }

    /**
     * Utility method to create nodes per revision.
     * 
     * @param pNumbers
     *            number to create
     * @param pWtx
     *            to store to.
     * @param pAlreadyExistingEntries
     *            already existing entries
     * @return List<Map.Entry<DumbKey, DumbValue>> returning the inserted data
     * @throws TTException
     */
    public static final List<Map.Entry<DumbKey, DumbValue>> insertMetaWithTransaction(final int pNumbers,
        final IBucketWriteTrx pWtx, List<Map.Entry<DumbKey, DumbValue>> pAlreadyExistingEntries)
        throws TTException {
        List<Map.Entry<DumbKey, DumbValue>> returnVal = createMetaEntries(pNumbers);
        List<Map.Entry<DumbKey, DumbValue>> toCheck = Lists.newArrayList();
        assertTrue(pAlreadyExistingEntries.isEmpty() != toCheck.addAll(pAlreadyExistingEntries));
        for (Map.Entry<DumbKey, DumbValue> entry : returnVal) {
            assertTrue(toCheck.add(entry));
            assertNull(pWtx.getMetaBucket().getMetaMap().put(entry.getKey(), entry.getValue()));
            checkStructure(toCheck, pWtx, true);
        }
        assertTrue(pAlreadyExistingEntries.isEmpty() != returnVal.addAll(pAlreadyExistingEntries));
        pWtx.commit();
        return returnVal;
    }

    /**
     * Creating a list of meta entries for testing the meta-bucket stuff
     * 
     * @param pNumbers
     *            of entries
     * @return a list containing map-entries of dumbvalues and dumbkeys.
     */
    public static final List<Map.Entry<DumbKey, DumbValue>> createMetaEntries(final int pNumbers) {
        final List<Map.Entry<DumbKey, DumbValue>> returnVal = Lists.newArrayList();
        for (int i = 0; i < pNumbers; i++) {
            final DumbKey key = new DumbKey(CoreTestHelper.random.nextLong());
            final DumbValue value = new DumbValue(CoreTestHelper.random.nextLong());
            returnVal.add(new Map.Entry<DumbKey, DumbValue>() {
                @Override
                public DumbKey getKey() {
                    return key;
                }

                @Override
                public DumbValue getValue() {
                    return value;
                }

                @Override
                public DumbValue setValue(DumbValue value) {
                    throw new UnsupportedOperationException();
                }

            });

        }

        return returnVal;
    }

    /**
     * Generating new nodes passed on a given number of nodes within a revision
     * 
     * @param pNodesPerRevision
     *            denote the number of nodes within all versions
     * @return a two-dimensional array containing the nodes.
     */
    public static final DumbNode[][] createNodes(final int[] pNodesPerRevision) {
        final DumbNode[][] returnVal = new DumbNode[pNodesPerRevision.length][];
        for (int i = 0; i < pNodesPerRevision.length; i++) {
            returnVal[i] = new DumbNode[pNodesPerRevision[i]];
            for (int j = 0; j < pNodesPerRevision[i]; j++) {
                returnVal[i][j] = generateOne();
            }
        }
        return returnVal;
    }

    /**
     * Checking the transaction with meta entries written.
     * 
     * @param pEntries
     *            to be compared with
     * @param pRtx
     *            to check
     * @param pWorkOnClone
     *            parameter if the check should occur on cloned structure
     * @throws TTIOException
     */
    public static final void checkStructure(final List<Map.Entry<DumbKey, DumbValue>> pEntries,
        final IBucketReadTrx pRtx, final boolean pWorkOnClone) throws TTIOException {
        Map<IMetaEntry, IMetaEntry> map;
        if (pWorkOnClone) {
            map = Maps.newHashMap();
            map.putAll(pRtx.getMetaBucket().getMetaMap());
        } else {
            map = pRtx.getMetaBucket().getMetaMap();
        }
        for (Map.Entry<DumbKey, DumbValue> entry : pEntries) {
            IMetaEntry value = map.remove(entry.getKey());
            assertNotNull(value);
            assertEquals(entry.getValue(), value);
        }
        assertEquals(0, map.size());
    }

    /**
     * Checking the transaction with nodes written sequentially.
     * 
     * @param pNodes
     *            to be compared with
     * @param pRtx
     *            to check
     * @throws TTIOException
     */
    public static final void checkStructure(final List<DumbNode> pNodes, final IBucketReadTrx pRtx,
        final long startKey) throws TTIOException {
        long key = startKey;
        for (DumbNode node : pNodes) {
            assertEquals(node, pRtx.getNode(key));
            key++;
        }
    }

    /**
     * Combining multiple nodes to one overall list.
     * 
     * @param pNodes
     *            to be combined
     * @return a list of all data in one list.
     */
    public static final List<DumbNode> combineNodes(final DumbNode[][] pNodes) {
        List<DumbNode> list = Lists.newArrayList();
        for (int i = 0; i < pNodes.length; i++) {
            list.addAll(Arrays.asList(pNodes[i]));
        }
        return list;
    }

    public static class Holder {

        private IStorage mStorage;

        private ISession mSession;

        public static Holder generateStorage() throws TTException {
            Holder holder = new Holder();
            holder.mStorage = CoreTestHelper.getStorage(PATHS.PATH1.getFile());
            return holder;
        }

        public static void generateSession(Holder pHolder, ResourceConfiguration pConf) throws TTException {
            pHolder.mStorage.createResource(pConf);
            pHolder.mSession =
                pHolder.mStorage.getSession(new SessionConfiguration(CoreTestHelper.RESOURCENAME,
                    StandardSettings.KEY));
        }

        public IStorage getStorage() {
            return mStorage;
        }

        public ISession getSession() {
            return mSession;
        }

        public void close() throws TTException {
            mSession.close();
            mStorage.close();
        }
    }

}
