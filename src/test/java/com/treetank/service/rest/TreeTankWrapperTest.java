/**
 * 
 */
package com.treetank.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankRestException;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class TreeTankWrapperTest {

    private ISession testContent;

    private TreeTankWrapper wrapper;

    @Before
    public void setUp() {
        TestHelper.removeAllFiles();
        try {
            testContent = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = testContent.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            wtx.close();

            wrapper = new TreeTankWrapper(ITestConstants.PATH2);
        } catch (final TreetankIOException exc) {
            fail();
        }
    }

    @After
    public void tearDown() {
        wrapper.close();
        testContent.close();
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#post(long, java.lang.String)}
     * . Inserting just content to the wrapper at the root node.
     */
    @Test
    @Ignore
    public void testPost1() {
        try {
            wrapper.post(0, DocumentCreater.XML);
            wrapper.close();
            testContent(DocumentCreater.XML);
        } catch (final TreetankRestException e) {
            fail();
        }

    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#putText(long, java.lang.String)}
     * .
     */
    @Test
    @Ignore
    public void testPutText() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#delete(long)}.
     */
    @Test
    @Ignore
    public void testDelete() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#getLastRevision()}.
     */
    @Test
    @Ignore
    public void testGetLastRevision() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#checkRevision(long)}.
     */
    @Test
    @Ignore
    public void testCheckRevision() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#get(java.io.OutputStream, long, long)}
     * .
     */
    @Test
    @Ignore
    public void testGetOutputStreamLongLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#get(java.io.OutputStream, long, long, java.lang.String)}
     * .
     */
    @Test
    @Ignore
    public void testGetOutputStreamLongLongString() {
        fail("Not yet implemented");
    }

    protected final static void testContent(final String contentToCheck) {
        final ISession session = Session.beginSession(ITestConstants.PATH2);
        final IReadTransaction rtx = session.beginReadTransaction();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        new XMLSerializer(rtx, stream).run();
        final String content = new String(stream.toByteArray());
        rtx.close();
        session.close();
        assertEquals(contentToCheck, content);
    }

}
