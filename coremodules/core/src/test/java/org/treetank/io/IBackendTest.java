package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyStorage;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.file.FileStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

import com.google.common.io.Files;

public class IBackendTest {

    @Test(dataProvider = "instantiateBackend")
    public void testFirstRef(Class<IBackend> clazz, IBackend[] pBackends) throws TTException {

        for (final IBackend backend : pBackends) {

            final PageReference pageRef1 = new PageReference();
            final UberPage page1 = new UberPage(1);
            pageRef1.setPage(page1);

            // same instance check
            final IBackendWriter backendWriter = backend.getWriter();
            backendWriter.writeFirstReference(pageRef1);
            final PageReference pageRef2 = backendWriter.readFirstReference();
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), pageRef1.getKey(), pageRef2.getKey());
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), ((UberPage)pageRef1.getPage()).getRevisionCount(),
                ((UberPage)pageRef2.getPage()).getRevisionCount());
            backendWriter.close();

            // new instance check
            final IBackendReader backendReader = backend.getReader();
            final PageReference pageRef3 = backendReader.readFirstReference();
            assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                .toString(), pageRef1.getKey(), pageRef3.getKey());
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), ((UberPage)pageRef1.getPage()).getRevisionCount(),
                ((UberPage)pageRef3.getPage()).getRevisionCount());
            backendReader.close();
            backend.close();
            backend.truncate();
        }
    }

    @Test(dataProvider = "instantiateBackend")
    public void testOtherReferences(Class<IBackend> clazz, IBackend[] pBackends) throws TTException {

        for (final IBackend backend : pBackends) {

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
        IByteHandlerPipeline handler = new ByteHandlerPipeline();

        Object[][] returnVal = {
            {
                IBackend.class, new IBackend[] {
                    createFileStorage(nodeFac, handler), createBerkeleyStorage(nodeFac, handler)
                }
            }
        };
        return returnVal;
    }

    private static List<PageReference> getReferences() {
        List<PageReference> returnVal = new ArrayList<PageReference>();

        return returnVal;
    }

    private static IBackend createBerkeleyStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler)
        throws TTIOException {
        File rootFolderToCreate = Files.createTempDir();
        File fileToCreate =
            new File(new File(new File(rootFolderToCreate, StorageConfiguration.Paths.Data.getFile()
                .getName()), TestHelper.RESOURCENAME), ResourceConfiguration.Paths.Data.getFile().getName());
        fileToCreate.mkdirs();
        Properties props =
            StandardSettings.getStandardProperties(rootFolderToCreate.getAbsolutePath(),
                TestHelper.RESOURCENAME);
        return new BerkeleyStorage(props, pNodeFac, pHandler);
    }

    private static IBackend createFileStorage(INodeFactory pNodeFac, IByteHandlerPipeline pHandler) {
        File rootFolderToCreate = Files.createTempDir();
        File fileToCreate =
            new File(new File(new File(rootFolderToCreate, StorageConfiguration.Paths.Data.getFile()
                .getName()), TestHelper.RESOURCENAME), ResourceConfiguration.Paths.Data.getFile().getName());
        fileToCreate.mkdirs();
        Properties props =
            StandardSettings.getStandardProperties(rootFolderToCreate.getAbsolutePath(),
                TestHelper.RESOURCENAME);
        return new FileStorage(props, pNodeFac, pHandler);
    }
}
