package com.treetank.saxon.testsuit;

import net.sf.saxon.CollectionURIResolver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.*;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AnyURIValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This CollectionURIResolver forms part of the test driver for the XQTS test suite.
 * It locates collections for a given namespace based on information in the XQTS catalog
 */
public class XQTSCollectionURIResolver implements CollectionURIResolver {

    private DocumentInfo catalog;
    private NodeInfo collectionElement;
    private boolean isDefault;


    public XQTSCollectionURIResolver(DocumentInfo catalog, NodeInfo collectionElement, boolean isDefault) {
        this.catalog = catalog;
        this.collectionElement = collectionElement;
        this.isDefault = isDefault;
    }

    public SequenceIterator resolve(String href, String base, XPathContext context) throws XPathException {

        NamePool pool = catalog.getNamePool();
        int inputDocumentNC = pool.allocate("", "http://www.w3.org/2005/02/query-test-XQTSCatalog", "input-document");
        int IDNC = pool.allocate("", "", "ID");

        if (href == null) {
            href = "";
        }

        if (!(href.equals(collectionElement.getAttributeValue(IDNC)) || (href.equals("") && isDefault))) {
            throw new XPathException("Unknown collection name " + href);
        }

        AxisIterator iter = collectionElement.iterateAxis(
                Axis.CHILD, new NameTest(Type.ELEMENT, inputDocumentNC, pool));
        List documents = new ArrayList(5);

        while (true) {
            NodeInfo m = (NodeInfo)iter.next();
            if (m==null) {
                break;
            }
            String shortName = m.getStringValue();
            String longName = "TestSources/" + shortName + ".xml";
            URI uri;
            try {
                uri = new URI(collectionElement.getBaseURI()).resolve(longName);
            } catch (URISyntaxException e) {
                throw new XPathException(e);
            }
            documents.add(new AnyURIValue(uri.toString()));
        }
        return new ListIterator(documents);
    }
}
