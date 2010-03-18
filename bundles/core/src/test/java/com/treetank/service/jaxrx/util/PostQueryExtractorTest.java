package com.treetank.service.jaxrx.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Lukas Lewandowski, University of Konstanz.
 * 
 */
public class PostQueryExtractorTest {

    /**
     * Instances the Literal true static variable
     */
    private static final transient String LITERALSTRUE = "true";
    /**
     * Instances the Literal false static variable
     */
    private static final transient String LITERALSFALSE = "false";
    /**
     * Instances the Literal property static variable
     */
    private static final transient String LITERALSPROPERTY = "property";
    /**
     * Instances text param query static variable.
     */
    public static final transient String PARAMQUERY = "query";
    /**
     * Instances text param wrap static variable.
     */
    public static final transient String PARAMWRAP = "wrap";
    /**
     * Instances text param output static variable.
     */
    public static final transient String PARAMOUTPUT = "output";
    /**
     * Instances text param revision static variable.
     */
    public static final transient String PARAMREVISION = "revision";
    /**
     * Instances text param command static variable.
     */
    public static final transient String PARAMCOMMAND = "command";

    /**
     * Test method for
     * {@link org.treetank.rest.util.PostQueryExtractor#getQueryOutOfXML(org.w3c.dom.Document)}
     * .
     * 
     * @throws ParserConfigurationException
     */
    @Test
    public final void testGetQueryOutOfXML()
            throws ParserConfigurationException {
        final Document newQuery = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        final Element queryElement = newQuery.createElement(PARAMQUERY);
        final Element textElement = newQuery.createElement("text");
        textElement.setTextContent("//continent");
        queryElement.appendChild(textElement);
        final Element propertiesElement = newQuery.createElement("properties");
        final Element wrapProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(wrapProperty, PARAMWRAP, LITERALSFALSE);
        final Element outputProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(outputProperty, PARAMOUTPUT, LITERALSTRUE);
        final Element revertProperty = newQuery.createElement(LITERALSPROPERTY);
        createNeededAtts(revertProperty, PARAMCOMMAND, "revertto:0");
        final Element revisionProperty = newQuery
                .createElement(LITERALSPROPERTY);
        createNeededAtts(revisionProperty, PARAMREVISION, "0");

        propertiesElement.appendChild(wrapProperty);
        propertiesElement.appendChild(revisionProperty);
        propertiesElement.appendChild(revertProperty);
        propertiesElement.appendChild(outputProperty);
        queryElement.appendChild(propertiesElement);
        newQuery.appendChild(queryElement);

        final Map<String, String> queryContent = PostQueryExtractor
                .getQueryOutOfXML(newQuery);
        final List<String> expected = new ArrayList<String>();
        expected.add(PARAMQUERY);
        expected.add("//continent");
        expected.add(PARAMWRAP);
        expected.add(LITERALSTRUE);
        expected.add(PARAMOUTPUT);
        expected.add(PARAMCOMMAND);
        expected.add("revertto:0");
        expected.add(PARAMREVISION);
        expected.add("0");
        expected.add(LITERALSFALSE);
        for (final Entry<String, String> entry : queryContent.entrySet()) {
            assertTrue("Compare expected and retrieved properties", expected
                    .contains(entry.getKey()));
            assertTrue("Compare expected and retrieved properties values",
                    expected.contains(entry.getValue()));

        }
    }

    /**
     * This method creates the both attributes within the property element.
     * 
     * @param element
     *            The property element.
     * @param name
     *            The name of the property.
     * @param value
     *            The value of the property.
     */
    private void createNeededAtts(final Element element, final String name,
            final String value) {
        element.setAttribute("name", name);
        element.setAttribute("value", value);
    }

}
