package org.treetank.service.xml;

import static org.junit.Assert.assertNotNull;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.treetank.NodeElementTestHelper;
import org.treetank.api.INodeWriteTrx;
import org.treetank.exception.TTException;

public class XMLTestHelper {

    /**
     * Read a file into a StringBuilder.
     * 
     * @param paramFile
     *            The file to read.
     * @param paramWhitespaces
     *            Retrieve file and don't remove any whitespaces.
     * @return StringBuilder instance, which has the string representation of
     *         the document.
     * @throws IOException
     *             throws an IOException if any I/O operation fails.
     */
    public static StringBuilder readFile(final File paramFile, final boolean paramWhitespaces)
        throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(paramFile));
        final StringBuilder sBuilder = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (paramWhitespaces) {
                sBuilder.append(line + "\n");
            } else {
                sBuilder.append(line.trim());
            }
        }

        // Remove last newline.
        if (paramWhitespaces) {
            sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
        }
        in.close();

        return sBuilder;
    }

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

    public static class DocumentCreater {

        /**
         * Create simple revision test in current database.
         * 
         * @param pWtx
         *            {@link INodeWriteTrx} to write to
         * @throws TTException
         *             if anything went wrong
         */
        public static void createVersioned(final INodeWriteTrx pWtx) throws TTException {
            assertNotNull(pWtx);
            NodeElementTestHelper.DocumentCreater.create(pWtx);
            pWtx.commit();
            for (int i = 0; i <= 1; i++) {
                pWtx.moveTo(ROOT_NODE);
                pWtx.insertElementAsFirstChild(new QName("ns", "a", "p"));
                pWtx.insertTextAsFirstChild("OOPS4!");
                pWtx.commit();
            }

        }

        /**
         * Create simple test document containing all supported node kinds, but
         * ignoring their namespace prefixes.
         * 
         * @param pWtx
         *            {@link INodeWriteTrx} to write to
         * @throws TTException
         *             if anything went wrong
         */
        public static void createWithoutNamespace(final INodeWriteTrx pWtx) throws TTException {
            assertNotNull(pWtx);
            NodeElementTestHelper.createDocumentRootNode(pWtx);
            pWtx.moveTo(ROOT_NODE);

            pWtx.insertElementAsFirstChild(new QName("a"));
            pWtx.insertAttribute(new QName("i"), "j");
            pWtx.moveTo(pWtx.getNode().getParentKey());

            pWtx.insertTextAsFirstChild("oops1");

            pWtx.insertElementAsRightSibling(new QName("b"));

            pWtx.insertTextAsFirstChild("foo");
            pWtx.insertElementAsRightSibling(new QName("c"));
            pWtx.moveTo(pWtx.getNode().getParentKey());

            pWtx.insertTextAsRightSibling("oops2");

            pWtx.insertElementAsRightSibling(new QName("b"));
            pWtx.insertAttribute(new QName("x"), "y");
            pWtx.moveTo(pWtx.getNode().getParentKey());

            pWtx.insertElementAsFirstChild(new QName("c"));
            pWtx.insertTextAsRightSibling("bar");
            pWtx.moveTo(pWtx.getNode().getParentKey());

            pWtx.insertTextAsRightSibling("oops3");

            pWtx.moveTo(ROOT_NODE);
        }
    }

}
