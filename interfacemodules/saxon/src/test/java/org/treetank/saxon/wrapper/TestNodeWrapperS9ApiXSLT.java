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

package org.treetank.saxon.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.NodeTestHelper;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.saxon.evaluator.XSLTEvaluator;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

import com.google.inject.Inject;

/**
 * Test XSLT S9Api.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public final class TestNodeWrapperS9ApiXSLT {

    /** Stylesheet file. */
    private static final File STYLESHEET = new File("src" + File.separator + "test" + File.separator
        + "resources" + File.separator + "styles" + File.separator + "books.xsl");

    /** Books XML file. */
    private static final File BOOKS = new File("src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "data" + File.separator + "books.xml");

    private CoreTestHelper.Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        holder = CoreTestHelper.Holder.generateStorage();
        final StorageConfiguration dbConfig = new StorageConfiguration(CoreTestHelper.PATHS.PATH1.getFile());
        Storage.createStorage(dbConfig);
        final IStorage databaseBooks = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME);
        ResourceConfiguration resConfig = mResourceConfig.create(props);
        databaseBooks.createResource(resConfig);
        final ISession session =
            databaseBooks.getSession(new SessionConfiguration(CoreTestHelper.RESOURCENAME,
                StandardSettings.KEY));
        final INodeWriteTrx wtx =
            new NodeWriteTrx(session, session.beginPageWriteTransaction(), HashKind.Rolling);
        NodeTestHelper.createDocumentRootNode(wtx);
        final XMLEventReader reader = XMLShredder.createFileReader(BOOKS);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();
        session.close();
        databaseBooks.close();
        CoreTestHelper.Holder.generateSession(holder, resConfig);

        saxonTransform(BOOKS, STYLESHEET);

        XMLUnit.setIgnoreWhitespace(true);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.closeEverything();
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testWithoutSerializer() throws Exception {
        final OutputStream out =
            new XSLTEvaluator(holder.getSession(), STYLESHEET, new ByteArrayOutputStream()).call();

        final StringBuilder sBuilder = readFile();

        final Diff diff = new Diff(sBuilder.toString(), out.toString());
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());

        AssertJUnit.assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testWithSerializer() throws Exception {
        final Serializer serializer = new Serializer();
        serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes");

        final OutputStream out =
            new XSLTEvaluator(holder.getSession(), STYLESHEET, new ByteArrayOutputStream(), serializer)
                .call();

        final StringBuilder sBuilder = readFile();

        final Diff diff = new Diff(sBuilder.toString(), out.toString());
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());

        AssertJUnit.assertTrue(diff.toString(), diff.similar());
    }

    /**
     * Transform source document with the given stylesheet.
     * 
     * @param xml
     *            Source xml file.
     * @param stylesheet
     *            Stylesheet to transform source xml file.
     * @throws SaxonApiException
     *             Exception from Saxon in case anything goes wrong.
     */
    private void saxonTransform(final File xml, final File stylesheet) throws SaxonApiException {
        final Processor proc = new Processor(false);
        final XsltCompiler comp = proc.newXsltCompiler();
        final XsltExecutable exp = comp.compile(new StreamSource(stylesheet));
        final XdmNode source = proc.newDocumentBuilder().build(new StreamSource(xml));
        final Serializer out = new Serializer();
        out.setOutputProperty(Serializer.Property.METHOD, "xml");
        out.setOutputProperty(Serializer.Property.INDENT, "yes");
        out.setOutputFile(new File(CoreTestHelper.PATHS.PATH1.getFile(), "books1.html"));
        final XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(out);
        trans.transform();
    }

    /**
     * Read file, which has been generated by "pure" Saxon.
     * 
     * @return StringBuilder instance, which has the string representation of
     *         the document.
     * @throws IOException
     *             throws an IOException if any I/O operation fails.
     */
    private StringBuilder readFile() throws IOException {
        final BufferedReader in =
            new BufferedReader(new FileReader(new File(CoreTestHelper.PATHS.PATH1.getFile(), "books1.html")));
        final StringBuilder sBuilder = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            sBuilder.append(line + "\n");
        }

        // Remove last newline.
        sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
        in.close();

        return sBuilder;
    }

}
