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
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.combinedCloud.CombinedBackend;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.io.ram.RAMStorage;
import org.treetank.page.DumbMetaEntryFactory;
import org.treetank.page.DumbNodeFactory;
import org.treetank.page.IConstants;
import org.treetank.page.IndirectPage;
import org.treetank.page.MetaPage;
import org.treetank.page.NodePage;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

public class IBackendTest {

    private ByteHandlerPipeline handler = new ByteHandlerPipeline(new Encryptor(StandardSettings.KEY),
        new Zipper());

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
            final UberPage page1 =
                new UberPage(CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong(),
                    CoreTestHelper.random.nextLong());

            // same instance check
            final IBackendWriter backendWriter = backend.getWriter();
            backendWriter.writeUberPage(page1);
            final UberPage page2 = backendWriter.readUber();
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), page1, page2);
            backendWriter.close();

            // new instance check
            final IBackendReader backendReader = backend.getReader();
            final UberPage page3 = backendReader.readUber();
            assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                .toString(), page1, page3);
            backendReader.close();

            backend.truncate();
        }
    }

    @Test(dataProvider = "instantiateBackend")
    public void testOtherReferences(Class<IBackendCreator> clazz, IBackendCreator[] pBackends)
        throws TTException {
        // initializing structure
        Map<Long, IPage> pages = new HashMap<Long, IPage>();
        for (int i = 0; i < 100; i++) {
            pages.put(new Long(i), generatePage(i));
        }

        // checking for backends
        for (final IBackendCreator backendCreator : pBackends) {
            CoreTestHelper.deleteEverything();
            IOUtils.createFolderStructure(CoreTestHelper.PATHS.PATH1.getFile(), ResourceConfiguration.Paths
                .values());
            final IBackend backend = backendCreator.getBackend();
            backend.initialize();
            final IBackendWriter backendWriter = backend.getWriter();
            for (Long i : pages.keySet()) {
                // same instance check
                backendWriter.write(pages.get(i));
                final IPage page2 = backendWriter.read(i);
                assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(
                    " failed on index ").append(i).toString(), pages.get(i), page2);
            }
            backendWriter.close();
            final IBackendReader backendReader = backend.getReader();
            for (Long i : pages.keySet()) {
                // new instance check
                final IPage page3 = backendReader.read(i);
                assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                    .toString(), pages.get(i), page3);
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
                        public IBackend getBackend() {
                            return new RAMStorage(handler);
                        }
                    }, new IBackendCreator() {
                        @Override
                        public IBackend getBackend() throws TTIOException {
                            return createCombinedStorage(nodeFac, handler, metaFac);
                        }
                    }
                    /*, new IBackendCreator() {
                        @Override
                        public IBackend getBackend() throws TTIOException {
                            return createAWSJCloudsStorage(nodeFac, handler, metaFac);
                        }
                    }*/

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
        return new CombinedBackend(props, pNodeFac, pMetaFac, pHandler);
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

    private static IBackend createAWSJCloudsStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        props.setProperty(ConstructorProps.JCLOUDSTYPE, "aws-s3");
        return new JCloudsStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    private static IBackend createLocalJCloudsStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        return new JCloudsStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    private static IPage generatePage(long pKey) {
        final double whichPage = CoreTestHelper.random.nextDouble();
        if (whichPage < 0.2) {
            IndirectPage returnVal = new IndirectPage(pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.setReferenceKey(i, CoreTestHelper.random.nextLong());
            }
            return returnVal;
        } else if (whichPage < 0.4) {
            MetaPage returnVal = new MetaPage(pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.getMetaMap().put(
                    new DumbMetaEntryFactory.DumbKey(CoreTestHelper.random.nextLong()),
                    new DumbMetaEntryFactory.DumbValue(CoreTestHelper.random.nextLong()));
            }
            return returnVal;
        } else if (whichPage < 0.6) {
            NodePage returnVal = new NodePage(pKey);
            for (int i = 0; i < IConstants.CONTENT_COUNT; i++) {
                returnVal.setNode(i, new DumbNodeFactory.DumbNode(CoreTestHelper.random.nextLong(),
                    CoreTestHelper.random.nextLong()));
            }
            return returnVal;
        } else if (whichPage < 0.8) {
            RevisionRootPage returnVal =
                new RevisionRootPage(pKey, CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
            returnVal.setReferenceKey(0, CoreTestHelper.random.nextLong());
            returnVal.setReferenceKey(1, CoreTestHelper.random.nextLong());
            return returnVal;
        } else {
            UberPage returnVal =
                new UberPage(pKey, CoreTestHelper.random.nextLong(), CoreTestHelper.random.nextLong());
            returnVal.setReferenceKey(0, CoreTestHelper.random.nextLong());
            return returnVal;
        }

    }

    interface IBackendCreator {
        IBackend getBackend() throws TTIOException;
    }

}
