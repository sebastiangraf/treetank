/**
 * 
 */
package org.treetank.service.xml.util;

import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.treetank.api.INodeWriteTransaction;
import org.treetank.exception.AbsTTException;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DocumentCreater {

    /** String representation of test document. */
    public static final String VERSIONEDXML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<tt revision=\"0\"><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>"
            + "<tt revision=\"1\"><p:a>OOPS4!</p:a><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>"
            + "<tt revision=\"2\"><p:a>OOPS4!</p:a><p:a>OOPS4!</p:a><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></tt>";

    /** String representation of rest. */
    public static final String REST =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<rest:sequence xmlns:rest=\"REST\"><rest:item>"
            + "<p:a xmlns:p=\"ns\" rest:ttid=\"1\" i=\"j\">oops1<b rest:ttid=\"5\">foo<c rest:ttid=\"7\"/></b>oops2<b rest:ttid=\"9\" p:x=\"y\">"
            + "<c rest:ttid=\"11\"/>bar</b>oops3</p:a>" + "</rest:item></rest:sequence>";

    /** String representation of ID. */
    public static final String ID =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><p:a xmlns:p=\"ns\" "
            + "ttid=\"1\" i=\"j\">oops1<b ttid=\"5\">foo<c ttid=\"7\"/></b>oops2<b ttid=\"9\" p:x=\"y\">"
            + "<c ttid=\"11\"/>bar</b>oops3</p:a>";

    /**
     * Create simple revision test in current database.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} to write to
     * @throws AbsTTException
     *             if anything went wrong
     */
    public static void createVersioned(final INodeWriteTransaction paramWtx) throws AbsTTException {
        assertNotNull(paramWtx);
        org.treetank.utils.DocumentCreater.create(paramWtx);
        paramWtx.commit();
        for (int i = 0; i <= 1; i++) {
            paramWtx.moveToDocumentRoot();
            paramWtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
            paramWtx.insertTextAsFirstChild("OOPS4!");
            paramWtx.commit();
        }

    }

    /**
     * Create simple test document containing all supported node kinds, but
     * ignoring their namespace prefixes.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} to write to
     * @throws AbsTTException
     *             if anything went wrong
     */
    public static void createWithoutNamespace(final INodeWriteTransaction paramWtx) throws AbsTTException {
        assertNotNull(paramWtx);
        paramWtx.moveToDocumentRoot();

        paramWtx.insertElementAsFirstChild(new QName("a"));
        paramWtx.insertAttribute(new QName("i"), "j");
        paramWtx.moveToParent();

        paramWtx.insertTextAsFirstChild("oops1");

        paramWtx.insertElementAsRightSibling(new QName("b"));

        paramWtx.insertTextAsFirstChild("foo");
        paramWtx.insertElementAsRightSibling(new QName("c"));
        paramWtx.moveToParent();

        paramWtx.insertTextAsRightSibling("oops2");

        paramWtx.insertElementAsRightSibling(new QName("b"));
        paramWtx.insertAttribute(new QName("x"), "y");
        paramWtx.moveToParent();

        paramWtx.insertElementAsFirstChild(new QName("c"));
        paramWtx.insertTextAsRightSibling("bar");
        paramWtx.moveToParent();

        paramWtx.insertTextAsRightSibling("oops3");

        paramWtx.moveToDocumentRoot();
    }
}
