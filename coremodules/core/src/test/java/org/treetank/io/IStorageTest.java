package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.exception.TTException;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

import com.google.inject.Inject;

@Guice(moduleFactory = ModuleFactory.class)
public class IStorageTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    @Inject
    private ISessionConfigurationFactory mSessionConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        mResource = mResourceConfig.create(TestHelper.createProperties(), 10);
        TestHelper.createResource(mResource);
        mSessionConfig.create(TestHelper.RESOURCENAME);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }

    /**
     * Test method for {@link org.treetank.io.bytepipe.IByteHandler#deserialize(byte[])} and for
     * {@link org.treetank.io.bytepipe.IByteHandler#serialize(byte[])}.
     * 
     * @throws TTException
     */
    @Test
    public void testFirstRef() throws TTException {

        IStorage handler = mResource.mStorage;

        final PageReference pageRef1 = new PageReference();
        final UberPage page1 = new UberPage();
        pageRef1.setPage(page1);

        // same instance check
        final IWriter writer = handler.getWriter();
        writer.writeFirstReference(pageRef1);
        final PageReference pageRef2 = writer.readFirstReference();
        assertEquals(
            new StringBuilder("Check for ").append(handler.getClass()).append(" failed.").toString(),
            pageRef1.getNodePageKey(), pageRef2.getNodePageKey());
        assertEquals(
            new StringBuilder("Check for ").append(handler.getClass()).append(" failed.").toString(),
            ((UberPage)pageRef1.getPage()).getRevisionCount(), ((UberPage)pageRef2.getPage())
                .getRevisionCount());
        writer.close();

        // new instance check
        final IReader reader = handler.getReader();
        final PageReference pageRef3 = reader.readFirstReference();
        assertEquals(
            new StringBuilder("Check for ").append(handler.getClass()).append(" failed.").toString(),
            pageRef1.getNodePageKey(), pageRef3.getNodePageKey());
        assertEquals(
            new StringBuilder("Check for ").append(handler.getClass()).append(" failed.").toString(),
            ((UberPage)pageRef1.getPage()).getRevisionCount(), ((UberPage)pageRef3.getPage())
                .getRevisionCount());
        reader.close();
        handler.close();
    }
}
