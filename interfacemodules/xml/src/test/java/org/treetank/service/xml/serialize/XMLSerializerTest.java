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

package org.treetank.service.xml.serialize;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.INodeWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.service.xml.DocumentCreater;
import org.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class XMLSerializerTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        Properties props = StandardSettings.getStandardProperties(TestHelper.PATHS.PATH1.getFile().getAbsolutePath(), TestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        holder = Holder.generateSession(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testXMLSerializer() throws Exception {
        final INodeWriteTrx wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        org.treetank.DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializer serializer = new XMLSerializerBuilder(holder.getSession(), out).build();
        serializer.call();
        String compareTo =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/></b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a>";

        assertEquals(compareTo, out.toString());
    }

    @Test
    public void testRestSerializer() throws Exception {

        final INodeWriteTrx wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        org.treetank.DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(holder.getSession(), out);
        builder.setREST(true);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.REST, out.toString());

    }

    @Test
    public void testIDSerializer() throws Exception {
        final INodeWriteTrx wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        org.treetank.DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        // Generate from this session.
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLSerializerBuilder builder = new XMLSerializerBuilder(holder.getSession(), out);
        builder.setID(true);
        builder.setDeclaration(true);
        final XMLSerializer serializer = builder.build();
        serializer.call();
        assertEquals(DocumentCreater.ID, out.toString());
    }

    @Test
    public void testSampleCompleteSerializer() throws Exception {
        final INodeWriteTrx wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // generate serialize all from this session
        DocumentCreater.createVersioned(wtx);
        wtx.commit();
        wtx.close();

        XMLSerializer serializerall = new XMLSerializerBuilder(holder.getSession(), out, -1).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
        out.reset();

        serializerall = new XMLSerializerBuilder(holder.getSession(), out, 0, 1, 2).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());
    }

    /**
     * This test check the XPath //books expression and expects 6 books as
     * result. But the failure is, that only the children of the books will be
     * serialized and NOT the book node itself.
     */
    @Test
    public void testKeyStart() throws Exception {

        final INodeWriteTrx wtx =
            new NodeWriteTrx(holder.getSession(), holder.getSession().beginPageWriteTransaction(),
                HashKind.Rolling);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // generate serialize all from this session
        DocumentCreater.createVersioned(wtx);
        wtx.commit();
        wtx.close();

        XMLSerializer serializerall =
            new XMLSerializerBuilder(holder.getSession(), 5l, out, new XMLSerializerProperties()).build();
        serializerall.call();
        final String result =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><b>\n  foo\n  <c/>\n</b>\n";

        assertEquals(result, out.toString());
        out.reset();

        serializerall = new XMLSerializerBuilder(holder.getSession(), out, 0, 1, 2).build();
        serializerall.call();
        assertEquals(DocumentCreater.VERSIONEDXML, out.toString());

    }
}
