/**
 * 
 */
package com.treetank.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankRestException;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class TreeTankWrapperTest {

    private TreeTankWrapper wrapper;

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
        wrapper = new TreeTankWrapper(ITestConstants.PATH2);
    }

    @After
    public void tearDown() throws TreetankRestException {
        wrapper.close();
        TestHelper.closeEverything();
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#post(long, java.lang.String)}
     * . Inserting just content to the wrapper at the root node.
     */
    @Test
    public void testPost1() throws TreetankRestException {
        wrapper.post(0, DocumentCreater.XML);
        wrapper.close();
        testContent(DocumentCreater.XML);
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#post(long, java.lang.String)}
     * . Inserting just content to the wrapper at the root node.
     */
    @Test
    public void testPost2() throws TreetankRestException {
        wrapper.post(0, DocumentCreater.XML);
        wrapper.post(9, DocumentCreater.XML);
        wrapper.close();
        testContent("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a><c/>bar</b>oops3</p:a>");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#putText(long, java.lang.String)}
     * .
     */
    @Test
    public void testPutText() throws TreetankRestException {
        wrapper.post(0, DocumentCreater.XML);
        wrapper.putText(10, "bla");
        wrapper.close();
        testContent("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"bla\"><c/>bar</b>oops3</p:a>");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#delete(long)}.
     */
    @Test
    public void testDelete1() throws TreetankRestException {
        try {
            wrapper.post(0, DocumentCreater.XML);
            wrapper.delete(10);
            wrapper.close();
            testContent("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b><c/>bar</b>oops3</p:a>");
        } catch (final TreetankRestException e) {
            fail();
        }
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#delete(long)}.
     */
    @Test
    public void testDelete2() throws TreetankRestException {
        wrapper.post(0, DocumentCreater.XML);
        wrapper.delete(9);
        wrapper.close();
        testContent("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2oops3</p:a>");
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#getLastRevision()}.
     */
    @Test
    public void testGetLastRevision() throws TreetankRestException {
        assertEquals(0, wrapper.getLastRevision());
        assertEquals(0, wrapper.post(0, DocumentCreater.XML));
        assertEquals(0, wrapper.getLastRevision());
        assertEquals(1, wrapper.post(0, DocumentCreater.XML));
        assertEquals(1, wrapper.getLastRevision());
    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#get(java.io.OutputStream, long, long)}
     * .
     */
    @Test
    public void testGetOutputStreamLongLong() throws TreetankRestException {
        final String content = "<rest:item><rest:sequence xmlns:rest=\"REST\"><rest:item><p:a xmlns:p=\"ns\" rest:id=\"1\" i=\"j\">oops1<b rest:id=\"5\">foo<c rest:id=\"7\"/></b>oops2<b rest:id=\"9\" p:x=\"y\"><c rest:id=\"11\"/>bar</b>oops3</p:a></rest:item></rest:sequence></rest:item>";
        wrapper.post(0, DocumentCreater.XML);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        wrapper.get(stream, 0, 0);
        wrapper.close();
        assertEquals(content, new String(stream.toByteArray()));

    }

    /**
     * Test method for
     * {@link com.treetank.service.rest.TreeTankWrapper#get(java.io.OutputStream, long, long, java.lang.String)}
     * .
     */
    @Test
    public void testGetOutputStreamLongLongString()
            throws TreetankRestException {
        final String content = "<rest:item><rest:sequence xmlns:rest=\"REST\"><rest:item><c rest:id=\"7\"/></rest:item></rest:sequence></rest:item><rest:item><rest:sequence xmlns:rest=\"REST\"><rest:item><c rest:id=\"11\"/></rest:item></rest:sequence></rest:item>";
        wrapper.post(0, DocumentCreater.XML);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        wrapper.get(stream, 0, 0, "descendant-or-self::b/child::*");
        assertEquals(content, new String(stream.toByteArray()));
        wrapper.close();

    }

    protected final static void testContent(final String contentToCheck) {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH2);
            final IReadTransaction rtx = session.beginReadTransaction();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            new XMLSerializer(rtx, stream).run();
            final String content = new String(stream.toByteArray());
            rtx.close();
            session.close();
            assertEquals(contentToCheck, content);
        } catch (final TreetankException exc) {
            exc.printStackTrace();
            fail();
        }
    }

}
