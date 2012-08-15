/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 */
package org.treetank.service.jaxrx.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.exception.TTException;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;
import org.treetank.service.jaxrx.implementation.DatabaseRepresentation;
import org.treetank.service.jaxrx.implementation.NodeIdRepresentationTest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

/**
 * This class tests {@link RESTResponseHelper}.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */

@Guice(moduleFactory = NodeModuleFactory.class)
public class RESTResponseHelperTest {
    /**
     * name constant.
     */
    private static final String NAME = "name";

    /**
     * shake constant.
     */
    private static final String SHAKE = "shakespeare";
    /**
     * book constant.
     */
    private static final String BOOK = "books";
    /**
     * fact constant.
     */
    private static final String FACT = "factbook";
    /**
     * ebay constant.
     */
    private static final String EBAY = "ebay";
    /**
     * resource path constant.
     */
    private static final String RESPATH = "/factbook.xml";

    @Inject
    public IStorageFactory mStorageFac;

    @Inject
    public IRevisioningFactory mRevisioningFac;

    @BeforeMethod
    public void before() throws TTException {
        TestHelper.deleteEverything();
        TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile());
    }

    @AfterMethod
    public void after() throws TTException {
        TestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.service.jaxrx.util.RESTResponseHelper#buildResponseOfDomLR(java.util.Map)} .
     * 
     * @throws IOException
     * @throws WebApplicationException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws TTException
     * @throws InterruptedException
     */
    @Test
    public final void testBuildResponseOfDomLR() throws WebApplicationException, IOException,
        ParserConfigurationException, SAXException, TTException, InterruptedException {

        final List<String> availResources = new ArrayList<String>();
        availResources.add(FACT);
        availResources.add(EBAY);
        availResources.add(BOOK);
        availResources.add(SHAKE);

        final DatabaseRepresentation treetank =
            new DatabaseRepresentation(TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile()), mStorageFac,
                mRevisioningFac);
        InputStream input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treetank.shred(input, FACT);
        input.close();

        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treetank.shred(input, EBAY);
        input.close();

        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treetank.add(input, BOOK);

        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treetank.shred(input, SHAKE);
        input.close();

        final StreamingOutput result =
            RESTResponseHelper.buildResponseOfDomLR(TestHelper.getDatabase(TestHelper.PATHS.PATH1.getFile()),
                mStorageFac, mRevisioningFac);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        result.write(outputStream);
        final Document doc = DOMHelper.buildDocument(outputStream);
        final NodeList listRes = doc.getElementsByTagName("resource");
        assertEquals("Test for the length of resource", 4, listRes.getLength());

        Set<String> names = new HashSet<String>();

        names.add(listRes.item(0).getAttributes().getNamedItem(NAME).getTextContent());
        names.add(listRes.item(1).getAttributes().getNamedItem(NAME).getTextContent());
        names.add(listRes.item(2).getAttributes().getNamedItem(NAME).getTextContent());
        names.add(listRes.item(3).getAttributes().getNamedItem(NAME).getTextContent());

        assertTrue(names.remove(FACT));
        assertTrue(names.remove(EBAY));
        assertTrue(names.remove(BOOK));
        assertTrue(names.remove(SHAKE));

        assertEquals("Size of test-structure must be 0", 0, names.size());

        outputStream.close();

        treetank.deleteResource(EBAY);
        treetank.deleteResource(FACT);
        treetank.deleteResource(BOOK);
        treetank.deleteResource(SHAKE);

    }
}
