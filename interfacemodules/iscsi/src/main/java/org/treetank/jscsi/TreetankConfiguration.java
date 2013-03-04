/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.jscsi;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jscsi.target.Configuration;
import org.jscsi.target.Target;
import org.jscsi.target.settings.TextKeyword;
import org.jscsi.target.storage.IStorageModule;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.exception.TTException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This configuration class extends {@link Configuration}
 * so that it can be used within the self defined target
 * server-
 * 
 * @author Andreas Rain
 *
 */
public class TreetankConfiguration extends Configuration {

    private final StorageConfiguration mConf;

    /**
     * Create a new {@link TreetankConfiguration}
     * @param pConf
     *          you have to pass a valid {@link StorageConfiguration}
     * @throws IOException
     */
    public TreetankConfiguration(final StorageConfiguration pConf) throws IOException {
        super();
        this.mConf = pConf;
    }

    /**
     * Static method to create a new {@link TreetankConfiguration}
     * 
     * @param schemaLocation
     * @param configFile
     * @param conf
     * @return {@link TreetankConfiguration}
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TTException
     */
    public static TreetankConfiguration create(final File schemaLocation, final File configFile,
        StorageConfiguration conf) throws SAXException, ParserConfigurationException, IOException,
        TTException {

        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(schemaLocation);

        // create a validator for the document
        final Validator validator = schema.newValidator();

        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(configFile);

        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();

        validator.validate(source, result);
        Document root = (Document)result.getNode();

        // TargetName
        TreetankConfiguration returnConfiguration = new TreetankConfiguration(conf);

        Element targetListNode = (Element)root.getElementsByTagName(ELEMENT_TARGET_LIST).item(0);
        NodeList targetList = targetListNode.getElementsByTagName(ELEMENT_TARGET);
        for (int curTargetNum = 0; curTargetNum < targetList.getLength(); curTargetNum++) {
            Target curTargetInfo =
                parseTargetElement((Element)targetList.item(curTargetNum), returnConfiguration);
            synchronized (returnConfiguration.getTargets()) {
                returnConfiguration.getTargets().add(curTargetInfo);
            }

        }

        // port
        if (root.getElementsByTagName(ELEMENT_PORT).getLength() > 0)
            returnConfiguration.port =
                Integer.parseInt(root.getElementsByTagName(ELEMENT_PORT).item(0).getTextContent());
        else
            returnConfiguration.port = 3260;

        // support sloppy text parameter negotiation (i.e. the jSCSI Initiator)?
        final Node allowSloppyNegotiationNode =
            root.getElementsByTagName(ELEMENT_ALLOWSLOPPYNEGOTIATION).item(0);
        if (allowSloppyNegotiationNode == null)
            returnConfiguration.allowSloppyNegotiation = false;
        else
            returnConfiguration.allowSloppyNegotiation =
                Boolean.parseBoolean(allowSloppyNegotiationNode.getTextContent());

        return returnConfiguration;
    }

    private static final Target parseTargetElement(Element targetElement, TreetankConfiguration conf)
        throws IOException, TTException {

        // TargetName
        // TargetName
        Node nextNode = chopWhiteSpaces(targetElement.getFirstChild());
        // assert
        // nextNode.getLocalName().equals(OperationalTextKey.TARGET_NAME);
        String targetName = nextNode.getTextContent();

        // TargetAlias (optional)
        nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        String targetAlias = "";
        if (nextNode.getLocalName().equals(TextKeyword.TARGET_ALIAS)) {
            targetAlias = nextNode.getTextContent();
            nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        }

        // // Finding out the concrete storage
        // IStorageModule.STORAGEKIND kind = null;
        // if (nextNode.getLocalName().equals(ELEMENT_SYNCFILESTORAGE)) {
        // kind = STORAGEKIND.SyncFile;
        // } else {
        // // assert nextNode.getLocalName().equals(ELEMENT_ASYNCFILESTORAGE);
        // kind = STORAGEKIND.AsyncFile;
        // }

        // Getting storagepath
        nextNode = nextNode.getFirstChild();
        nextNode = chopWhiteSpaces(nextNode);
        // assert nextNode.getLocalName().equals(ELEMENT_PATH);
        // String storageFilePath = nextNode.getTextContent();

        // CreateNode with size
        nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        long storageLength = -1;

        if (nextNode.getLocalName().equals(ELEMENT_CREATE)) {
            Node sizeAttribute = nextNode.getAttributes().getNamedItem(ATTRIBUTE_SIZE);
            storageLength =
                Math.round(((Double.valueOf(sizeAttribute.getTextContent())) * Math.pow(1024, 3)));
        }

        final IStorageModule module =
            new TreetankStorageModule(storageLength
                / (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE), conf.mConf);

        return new Target(targetName, targetAlias, module);

    }

}
