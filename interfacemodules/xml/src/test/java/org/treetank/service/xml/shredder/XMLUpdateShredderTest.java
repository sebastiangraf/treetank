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

package org.treetank.service.xml.shredder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.CoreTestHelper.Holder;
import org.treetank.ModuleFactory;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.service.xml.XMLTestHelper;
import org.treetank.service.xml.serialize.XMLSerializer;
import org.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;

import com.google.inject.Inject;

/**
 * Test XMLUpdateShredder.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */

@Guice(moduleFactory = ModuleFactory.class)
public final class XMLUpdateShredderTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder.generateSession(holder, mResource);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testSame() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsSame");
    }

    @Test
    public void testInsertsFirst() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsInsert");
    }

    @Test
    public void testInsertsSecond() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsInsert1");
    }

    @Test
    public void testInsertsThird() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsInsert2");
    }

    @Test
    public void testDeletesFirst() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsDelete");
    }

    @Test
    public void testDeletesSecond() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsDelete1");
    }

    @Test
    public void testDeletesThird() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsDelete2");
    }

    @Test
    public void testDeletesFourth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "revXMLsDelete3");
    }

    @Test
    public void testAllFirst() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll");
    }

    @Test
    public void testAllSecond() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll1");
    }

    @Test
    public void testAllThird() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll2");
    }

    @Test
    public void testAllFourth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll3");
    }

    @Test
    public void testAllFifth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll4");
    }

    @Test
    public void testAllSixth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll5");
    }

    @Test
    public void testAllSeventh() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll6");
    }

    @Test
    public void testAllEighth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll7");
    }

    @Test
    public void testAllNineth() throws Exception {
        check("src" + File.separator + "test" + File.separator + "resources" + File.separator + "revXMLsAll8");
    }

    // @Test
    // public void testLinguistics() throws Exception {
    // test(XMLLINGUISTICS);
    // }

    private void check(final String folderString) throws Exception {
        final File folder = new File(folderString);
        final File[] filesList = folder.listFiles();
        final List<File> list = new ArrayList<File>();
        for (final File file : filesList) {
            if (file.getName().endsWith(".xml")) {
                list.add(file);
            }
        }

        // Sort files array according to file names.
        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(final Object paramFirst, final Object paramSecond) {
                final String firstName =
                    ((File)paramFirst).getName().toString().substring(0,
                        ((File)paramFirst).getName().toString().indexOf('.'));
                final String secondName =
                    ((File)paramSecond).getName().toString().substring(0,
                        ((File)paramSecond).getName().toString().indexOf('.'));
                if (Integer.parseInt(firstName) < Integer.parseInt(secondName)) {
                    return -1;
                } else if (Integer.parseInt(firstName) > Integer.parseInt(secondName)) {
                    return +1;
                } else {
                    return 0;
                }
            }
        });

        boolean first = true;

        // Shredder files.
        for (final File file : list) {
            if (file.getName().endsWith(".xml")) {

                final INodeWriteTrx wtx =
                    new NodeWriteTrx(holder.getSession(), holder.getSession().beginBucketWtx(),
                        HashKind.Rolling);
                if (first) {
                    final XMLShredder shredder =
                        new XMLShredder(wtx, XMLShredder.createFileReader(file),
                            EShredderInsert.ADDASFIRSTCHILD);
                    shredder.call();
                    first = false;
                } else {
                    final XMLShredder shredder =
                        new XMLUpdateShredder(wtx, XMLShredder.createFileReader(file),
                            EShredderInsert.ADDASFIRSTCHILD, file, EShredderCommit.COMMIT);
                    shredder.call();
                }

                final OutputStream out = new ByteArrayOutputStream();
                final XMLSerializer serializer = new XMLSerializerBuilder(holder.getSession(), out).build();
                serializer.call();
                final StringBuilder sBuilder = XMLTestHelper.readFile(file.getAbsoluteFile(), false);

                // System.out.println(out.toString());
                final Diff diff = new Diff(sBuilder.toString(), out.toString());
                // final DetailedDiff detDiff = new DetailedDiff(diff);
                // @SuppressWarnings("unchecked")
                // final List<Difference> differences =
                // detDiff.getAllDifferences();
                // for (final Difference difference : differences) {
                // // System.out.println("***********************");
                // // System.out.println(difference);
                // // System.out.println("***********************");
                // }

                AssertJUnit.assertTrue("pieces of XML are similar " + diff, diff.similar());
                AssertJUnit.assertTrue("but are they identical? " + diff, diff.identical());
                wtx.close();
            }
        }
    }
}
