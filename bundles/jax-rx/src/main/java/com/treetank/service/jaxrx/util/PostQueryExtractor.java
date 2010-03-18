/**
 * 
 */
package com.treetank.service.jaxrx.util;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible to offer Query extraction of an XML request.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class PostQueryExtractor {

    /**
     * I'm a lazy constructor.
     */
    private PostQueryExtractor() {
        // i do nothing
    }

    /**
     * This method extracts the text and property elements out of the query XML
     * in the HTTP POST request.
     * 
     * @param pvDoc
     *            The DOM {@link Document} containing the query XML.
     * @return A map containing the extracted information.
     */
    public static Map<String, String> getQueryOutOfXML(final Document pvDoc) {
        final Map<String, String> values = new HashMap<String, String>();
        final NodeList queryNodes = pvDoc.getElementsByTagName("text");
        final String query = queryNodes.item(0).getTextContent();
        values.put("query", query);
        final NodeList propertyNodes = pvDoc.getElementsByTagName("property");
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            final Node property = propertyNodes.item(i);
            values.put(property.getAttributes().getNamedItem("name")
                    .getTextContent(), property.getAttributes().getNamedItem(
                    "value").getTextContent());
        }
        return values;
    }

}
