package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jclouds.filesystem.reference.FilesystemConstants;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.bucket.DumbMetaEntryFactory;
import org.treetank.bucket.DumbNodeFactory;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.combined.CombinedStorage;
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class IBackendTest {

    private ByteHandlerPipeline handler = new ByteHandlerPipeline(new Encryptor(StandardSettings.KEY),
        new Zipper());

    private final static HashFunction hashFunc = Hashing.sha512();

    @AfterMethod
    public void tearDown() {
        IOUtils.recursiveDelete(CoreTestHelper.PATHS.PATH1.getFile());
    }

    @Test(dataProvider = "instantiateBackend")
    public void testFirstRef(Class<IBackendCreator> clazz, IBackendCreator[] pBackends) throws TTException {

        for (final IBackendCreator backendCreator : pBackends) {
            CoreTestHelper.deleteEverything();
            IOUtils.createFolderStructure(CoreTestHelper.PATHS.PATH1.getFile(), ResourceConfiguration.Paths
                .values());
            final IBackend backend = backendCreator.getBackend();
            backend.initialize();
            final UberBucket bucket1 =
                new UberBucket(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                    CoreTestHelper.random.nextLong());

            // same instance check
            final IBackendWriter backendWriter = backend.getWriter();
            backendWriter.writeUberBucket(bucket1);
            final UberBucket bucket2 = backendWriter.readUber();
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), bucket1.hashCode(), bucket2.hashCode());
            backendWriter.close();

            // new instance check
            final IBackendReader backendReader = backend.getReader();
            final UberBucket bucket3 = backendReader.readUber();
            assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                .toString(), bucket1, bucket3);
            backendReader.close();

            backend.truncate();
        }
    }

    @Test(dataProvider = "instantiateBackend")
    public void testOtherReferences(Class<IBackendCreator> clazz, IBackendCreator[] pBackends)
        throws TTException {
        // initializing structure
        Map<Long, IBucket> buckets = new HashMap<Long, IBucket>();
        for (int i = 0; i < 100; i++) {
            buckets.put(new Long(i), generateBucket(i));
        }

        // checking for backends
        for (final IBackendCreator backendCreator : pBackends) {
            CoreTestHelper.deleteEverything();
            IOUtils.createFolderStructure(CoreTestHelper.PATHS.PATH1.getFile(), ResourceConfiguration.Paths
                .values());
            final IBackend backend = backendCreator.getBackend();
            backend.initialize();
            final IBackendWriter backendWriter = backend.getWriter();
            for (Long i : buckets.keySet()) {
                // same instance check
                final IBucket bucket2 = backendWriter.read(i);
                assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(
                    " failed on index ").append(i).toString(), buckets.get(i), bucket2);
            }
            backendWriter.close();
            final IBackendReader backendReader = backend.getReader();
            for (Long i : buckets.keySet()) {
                // new instance check
                final IBucket bucket3 = backendReader.read(i);
                assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                    .toString(), buckets.get(i), bucket3);
            }
            backendReader.close();

            backend.truncate();
        }
    }

    @Test(dataProvider = "instantiateBackend")
    public void testHandler(Class<IBackendCreator> clazz, IBackendCreator[] pBackends) throws TTException {
        // initializing structure

        // checking for backends
        for (final IBackendCreator backendCreator : pBackends) {
            CoreTestHelper.deleteEverything();
            IOUtils.createFolderStructure(CoreTestHelper.PATHS.PATH1.getFile(), ResourceConfiguration.Paths
                .values());
            final IBackend backend = backendCreator.getBackend();
            backend.initialize();
            assertEquals(handler.toString(), backend.getByteHandler().toString());
            backend.truncate();
        }
    }

    /**
     * Providing different implementations of the {@link IBackend}s.
     * 
     * @return different classes of the {@link IBackend}s
     * @throws TTException
     */
    @DataProvider(name = "instantiateBackend")
    public Object[][] instantiateBackend() throws TTException {

        final INodeFactory nodeFac = new DumbNodeFactory();
        final IMetaEntryFactory metaFac = new DumbMetaEntryFactory();

        Object[][] returnVal = {
            {
                IBackendCreator.class, new IBackendCreator[] {
                    new IBackendCreator() {
                        @Override
                        public IBackend getBackend() throws TTIOException {
                            return createBerkeleyStorage(nodeFac, handler, metaFac);
                        }
                    }, new IBackendCreator() {
                        @Override
                        public IBackend getBackend() throws TTIOException {
                            return createLocalJCloudsStorage(nodeFac, handler, metaFac);
                        }
                    }, new IBackendCreator() {
                        @Override
                        public IBackend getBackend() throws TTIOException {
                            return createCombinedStorage(nodeFac, handler, metaFac);
                        }
                    }
                /*
                 * , new IBackendCreator() {
                 * 
                 * @Override
                 * public IBackend getBackend() throws TTIOException {
                 * return createAWSJCloudsStorage(nodeFac, handler, metaFac);
                 * }
                 * }
                 */

                }
            }
        };
        return returnVal;
    }

    private static IBackend createCombinedStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        props.setProperty(ConstructorProps.RESOURCEPATH, CoreTestHelper.PATHS.PATH1.getFile()
            .getAbsolutePath());
        props.setProperty(FilesystemConstants.PROPERTY_BASEDIR, CoreTestHelper.PATHS.PATH2.getFile()
            .getAbsolutePath());
        return new CombinedStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    private static IBackend createBerkeleyStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        props.setProperty(ConstructorProps.RESOURCEPATH, CoreTestHelper.PATHS.PATH1.getFile()
            .getAbsolutePath());
        return new BerkeleyStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    //
    // private static IBackend createAWSJCloudsStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
    // IMetaEntryFactory pMetaFac) throws TTIOException {
    // Properties props =
    // StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
    // CoreTestHelper.RESOURCENAME);
    // props.setProperty(ConstructorProps.JCLOUDSTYPE, "aws-s3");
    // return new JCloudsStorage(props, pNodeFac, pMetaFac, pHandler);
    // }

    private static IBackend createLocalJCloudsStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        return new JCloudsStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    private static IBucket generateBucket(long pKey) {
        final double whichBucketPage = CoreTestHelper.random.nextDouble();
        if (whichBucketPage < 0.2) {
            IndirectBucket returnVal = new IndirectBucket(pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.setReferenceKey(i, CoreTestHelper.random.nextLong());
                returnVal.setReferenceHash(i, generateRandomHash().asBytes());
            }
            return returnVal;
        } else if (whichBucketPage < 0.4) {
            MetaBucket returnVal = new MetaBucket(pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.put(new DumbMetaEntryFactory.DumbKey(CoreTestHelper.random.nextLong()),
                    new DumbMetaEntryFactory.DumbValue(CoreTestHelper.random.nextLong()));
            }
            return returnVal;
        } else if (whichBucketPage < 0.6) {
            NodeBucket returnVal = new NodeBucket(pKey, pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.setNode(i, CoreTestHelper.generateOne());
            }
            return returnVal;
        } else if (whichBucketPage < 0.8) {
            RevisionRootBucket returnVal =
                new RevisionRootBucket(pKey, CoreTestHelper.random.nextLong(), CoreTestHelper.random
                    .nextLong());
            returnVal.setReferenceKey(0, CoreTestHelper.random.nextLong());
            returnVal.setReferenceHash(0, generateRandomHash().asBytes());
            returnVal.setReferenceKey(1, CoreTestHelper.random.nextLong());
            returnVal.setReferenceHash(1, generateRandomHash().asBytes());
            return returnVal;
        } else {
            UberBucket returnVal =
                new UberBucket(pKey, CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
            returnVal.setReferenceKey(0, CoreTestHelper.random.nextLong());
            returnVal.setReferenceHash(0, generateRandomHash().asBytes());
            return returnVal;
        }

    }

    private static HashCode generateRandomHash() {
        HashCode code = hashFunc.newHasher().putLong(CoreTestHelper.random.nextLong()).hash();
        return code;
    }

    interface IBackendCreator {
        IBackend getBackend() throws TTIOException;
    }

}
