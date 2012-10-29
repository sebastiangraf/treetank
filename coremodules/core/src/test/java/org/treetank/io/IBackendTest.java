package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.page.DumbNodeFactory;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

public class IBackendTest {



    @Test(dataProvider = "instantiateBackend")
    public void testFirstRef(Class<IBackend> clazz, IBackend[] pBackends) throws TTException {

        for (final IBackend backend : pBackends) {

            final PageReference pageRef1 = new PageReference();
            final UberPage page1 = new UberPage();
            pageRef1.setPage(page1);

            // same instance check
            final IBackendWriter backendWriter = backend.getWriter();
            backendWriter.writeFirstReference(pageRef1);
            final PageReference pageRef2 = backendWriter.readFirstReference();
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), pageRef1.getNodePageKey(), pageRef2.getNodePageKey());
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), ((UberPage)pageRef1.getPage()).getRevisionCount(),
                ((UberPage)pageRef2.getPage()).getRevisionCount());
            backendWriter.close();

            // new instance check
            final IBackendReader backendReader = backend.getReader();
            final PageReference pageRef3 = backendReader.readFirstReference();
            assertEquals(new StringBuilder("Check for ").append(pBackends.getClass()).append(" failed.")
                .toString(), pageRef1.getNodePageKey(), pageRef3.getNodePageKey());
            assertEquals(new StringBuilder("Check for ").append(backend.getClass()).append(" failed.")
                .toString(), ((UberPage)pageRef1.getPage()).getRevisionCount(),
                ((UberPage)pageRef3.getPage()).getRevisionCount());
            backendReader.close();
            backend.close();
        }
    }

    /**
     * Providing different implementations of the {@link IBackend}s.
     * 
     * @return different classes of the {@link IBackend}s
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateBackend")
    public Object[][] instantiateBackend() throws TTByteHandleException {

        
        INodeFactory nodeFac = new DumbNodeFactory();
        IByteHandler handler = new ByteHandlerPipeline();
        
        Object[][] returnVal =
            {
                {
                    IBackend.class,
                    new IBackend[] {
                        
                        
                    }
                }
            };
        return returnVal;
    }
}
