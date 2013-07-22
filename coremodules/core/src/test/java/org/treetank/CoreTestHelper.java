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

import static com.google.common.base.Objects.toStringHelper;
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
import java.util.Set;

import org.treetank.access.Session;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.IMetaEntry;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.DumbMetaEntryFactory.DumbKey;
import org.treetank.bucket.DumbMetaEntryFactory.DumbValue;
import org.treetank.bucket.DumbDataFactory.DumbData;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.DataBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
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
     * Getting a data buckets filled with datas.
     * 
     * @param offset
     *            offset to start within the bucket
     * @param length
     *            length of the bucket
     * @param databucketKey
     *            key of the databucket
     * @param lastbucketKey
     *            key of the former bucket
     * @return a {@link DataBucket} filled
     */
    public static final DataBucket getDataBucket(final int offset, final int length,
        final long databucketKey, final long lastBucketKey) {
        final DataBucket bucket = new DataBucket(databucketKey, lastBucketKey);
        for (int i = offset; i < length; i++) {
            bucket.setData(i, generateOne());
        }
        return bucket;
    }

    /**
     * Generating one single {@link DumbData} with random values.
     * 
     * @return one {@link DumbData} with random values.
     */
    public static final DumbData generateOne() {
        byte[] data = new byte[0];
        CoreTestHelper.random.nextBytes(data);
        return new DumbData(CoreTestHelper.random.nextLong(), data);
    }

    /**
     * Getting a fake structure for testing consisting of different arranged buckets.
     * This structure starts with the key 1 and incrementally sets a new bucketkey for the defined offsets in
     * the indirectbuckets to simulate different versions and data-offsets.
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
                buckets.setReferenceHash(j, generateRandomHash().asBytes());
            }
            // ...tell the mock to react when the key is demanded.
            when(reader.read(oldKey)).thenReturn(buckets);
        }
        // returning the mock
        return reader;
    }

    /**
     * Create datas in different versions in Treetank and check directly afterwards the structure.
     * 
     * @param pHolder
     *            for getting the transaction
     * @return a two-dimensional array of datas.
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
     * Create datas in different versions in Treetank and check directly afterwards the structure.
     * 
     * @param pHolder
     *            for getting the transaction
     * @return a two-dimensional array of datas.
     * @throws TTException
     */
    public static final DumbData[][] createTestData(Holder pHolder) throws TTException {
        IBucketWriteTrx wtx = pHolder.getSession().beginBucketWtx();
        int[] datasPerRevision = new int[10];
        Arrays.fill(datasPerRevision, 128);
        DumbData[][] datas = CoreTestHelper.insertDatasWithTransaction(datasPerRevision, wtx);
        checkStructure(combineDatas(datas), wtx, 0);
        wtx.close();
        return datas;
    }

    /**
     * Utility method to create datas per revision.
     * 
     * @param pDatasPerRevision
     *            to create
     * @param pWtx
     *            to store to.
     * @throws TTException
     */
    public static final DumbData[][] insertDatasWithTransaction(final int[] pDatasPerRevision,
        final IBucketWriteTrx pWtx) throws TTException {
        final DumbData[][] returnVal = createDatas(pDatasPerRevision);
        for (int i = 0; i < returnVal.length; i++) {
            for (int j = 0; j < returnVal[i].length; j++) {
                final long dataKey = pWtx.incrementDataKey();
                returnVal[i][j].setDataKey(dataKey);
                pWtx.setData(returnVal[i][j]);
                assertEquals(returnVal[i][j], pWtx.getData(dataKey));
            }
            checkStructure(Arrays.asList(returnVal[i]), pWtx, i * returnVal[i].length);
            pWtx.commit();
        }
        return returnVal;
    }

    /**
     * Utility method to create datas per revision.
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
            assertNull(pWtx.getMetaBucket().put(entry.getKey(), entry.getValue()));
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

                /**
                 * {@inheritDoc}
                 */
                @Override
                public String toString() {
                    return toStringHelper(this).add("key", key).add("value", value).toString();
                }

            });

        }

        return returnVal;
    }

    /**
     * Generating new atas passed on a given number of datas within a revision
     * 
     * @param pDatasPerRevision
     *            denote the number of datas within all versions
     * @return a two-dimensional array containing the datas.
     */
    public static final DumbData[][] createDatas(final int[] pDatasPerRevision) {
        final DumbData[][] returnVal = new DumbData[pDatasPerRevision.length][];
        for (int i = 0; i < pDatasPerRevision.length; i++) {
            returnVal[i] = new DumbData[pDatasPerRevision[i]];
            for (int j = 0; j < pDatasPerRevision[i]; j++) {
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
        final Map<IMetaEntry, IMetaEntry> map = Maps.newHashMap();

        final MetaBucket formerBucket = pRtx.getMetaBucket();
        if (pWorkOnClone) {
            final Set<Map.Entry<IMetaEntry, IMetaEntry>> keys = formerBucket.entrySet();
            for (Map.Entry<IMetaEntry, IMetaEntry> key : keys) {
                map.put(key.getKey(), key.getValue());
            }
        }
        for (Map.Entry<DumbKey, DumbValue> entry : pEntries) {
            IMetaEntry value =
                pWorkOnClone ? map.remove(entry.getKey()) : formerBucket.remove(entry.getKey());

            // DEBUG CODE!
            if (value == null) {
                System.out.println("ERROR!");
                System.out.println(pWorkOnClone);
                System.out.println("-------");
                System.out.println(pEntries);
                System.out.println("-------");
                System.out.println(entry);
                System.out.println("-------");
                System.out.println(map);
                System.out.println("-------");
                System.out.println(pRtx.getRevision());
            }

            assertNotNull(value);
            assertEquals(entry.getValue(), value);
        }
        assertEquals(0, map.size());
    }

    /**
     * Checking the transaction with datas written sequentially.
     * 
     * @param pDatas
     *            to be compared with
     * @param pRtx
     *            to check
     * @throws TTIOException
     */
    public static final void checkStructure(final List<DumbData> pDatas, final IBucketReadTrx pRtx,
        final long startKey) throws TTIOException {
        long key = startKey;
        for (DumbData data : pDatas) {
            assertEquals(data, pRtx.getData(key));
            key++;
        }
    }

    /**
     * Combining multiple datas to one overall list.
     * 
     * @param pDatas
     *            to be combined
     * @return a list of all data in one list.
     */
    public static final List<DumbData> combineDatas(final DumbData[][] pDatas) {
        List<DumbData> list = Lists.newArrayList();
        for (int i = 0; i < pDatas.length; i++) {
            list.addAll(Arrays.asList(pDatas[i]));
        }
        return list;
    }

    public static final HashCode generateRandomHash() {
        HashCode code =
            StandardSettings.HASHFUNC.newHasher().putLong(CoreTestHelper.random.nextLong()).hash();
        return code;
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
