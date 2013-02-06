package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
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

import com.google.common.io.Files;

public class IBackendTest {

    @Test(dataProvider = "instantiateBackend")
    public void testFirstRef(Class<IBackend> clazz, IBackend[] pBackends) throws TTException {

        for (final IBackend backend : pBackends) {
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
            backend.close();
            backend.truncate();
        }
    }

    @Test(dataProvider = "instantiateBackend")
    public void testOtherReferences(Class<IBackend> clazz, IBackend[] pBackends) throws TTException {
        // initializing structure
        Map<Long, IPage> pages = new HashMap<Long, IPage>();
        for (int i = 0; i < 1000; i++) {
            pages.put(new Long(i), generatePage(i));
        }

        // checking for backends
        for (final IBackend backend : pBackends) {
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
        }
    }

    /**
     * Providing different implementations of the {@link IBackend}s.
     * 
     * @return different classes of the {@link IBackend}s
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateBackend")
    public Object[][] instantiateBackend() throws TTIOException {

        INodeFactory nodeFac = new DumbNodeFactory();
        IMetaEntryFactory metaFac = new DumbMetaEntryFactory();
        IByteHandlerPipeline handler = new ByteHandlerPipeline();

        Object[][] returnVal =
            {
                {
                    IBackend.class,
                    new IBackend[] {
                        createBerkeleyStorage(nodeFac, handler, metaFac),
                        createJCloudsStorage(nodeFac, handler, metaFac), new RAMStorage(handler)
                    }
                }
            };
        return returnVal;
    }

    private static IBackend createBerkeleyStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        File rootFolderToCreate = Files.createTempDir();
        File fileToCreate =
            new File(new File(new File(rootFolderToCreate, StorageConfiguration.Paths.Data.getFile()
                .getName()), CoreTestHelper.RESOURCENAME), ResourceConfiguration.Paths.Data.getFile()
                .getName());
        fileToCreate.mkdirs();
        Properties props =
            StandardSettings.getStandardProperties(rootFolderToCreate.getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        return new BerkeleyStorage(props, pNodeFac, pMetaFac, pHandler);
    }

    private static IBackend createJCloudsStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler,
        IMetaEntryFactory pMetaFac) throws TTIOException {
        File rootFolderToCreate = Files.createTempDir();
        File fileToCreate =
            new File(new File(new File(rootFolderToCreate, StorageConfiguration.Paths.Data.getFile()
                .getName()), CoreTestHelper.RESOURCENAME), ResourceConfiguration.Paths.Data.getFile()
                .getName());
        fileToCreate.mkdirs();
        Properties props =
            StandardSettings.getStandardProperties(rootFolderToCreate.getAbsolutePath(),
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
}
