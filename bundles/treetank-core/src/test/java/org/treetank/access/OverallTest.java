/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.access;

import java.io.File;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.settings.EDatabaseSetting;

public final class OverallTest {

    private static int NUM_CHARS = 3;
    private static int ELEMENTS = 1000;
    private static int COMMITPERCENTAGE = 20;
    private static int REMOVEPERCENTAGE = 20;
    private static final Random ran = new Random(0l);
    public static String chars = "abcdefghijklm";

    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "auction.xml";

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testXML() throws Exception {

        for (int i = 0; i < Integer.parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty()) * 2; i++) {
            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession(new SessionConfiguration());
            final IWriteTransaction wtx = session.beginWriteTransaction();
            if (wtx.moveToFirstChild()) {
                wtx.remove();
                wtx.commit();
            } else {
                wtx.abort();
            }

            final XMLEventReader reader = XMLShredder.createReader(new File(XML));
            final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
            shredder.call();

            wtx.close();
            session.close();

        }
    }

    @Test
    public void testJustEverything() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.insertElementAsFirstChild(new QName(getString()));
        for (int i = 0; i < ELEMENTS; i++) {
            if (ran.nextBoolean()) {
                switch (wtx.getNode().getKind()) {
                case ELEMENT_KIND:
                    wtx.setName(getString());
                    wtx.setURI(getString());
                    break;
                case ATTRIBUTE_KIND:
                    wtx.setName(getString());
                    wtx.setURI(getString());
                    wtx.setValue(getString());
                    break;
                case NAMESPACE_KIND:
                    wtx.setName(getString());
                    wtx.setURI(getString());
                    break;
                case TEXT_KIND:
                    wtx.setValue(getString());
                    break;
                default:
                }
            } else {
                if (wtx.getNode() instanceof ElementNode) {
                    if (ran.nextBoolean()) {
                        wtx.insertElementAsFirstChild(new QName(getString()));
                    } else {
                        wtx.insertElementAsRightSibling(new QName(getString()));
                    }
                    while (ran.nextBoolean()) {
                        wtx.insertAttribute(new QName(getString()), getString());
                        wtx.moveToParent();
                    }
                    while (ran.nextBoolean()) {
                        wtx.insertNamespace(getString(), getString());
                        wtx.moveToParent();
                    }
                }

                if (ran.nextInt(100) < REMOVEPERCENTAGE) {
                    wtx.remove();
                }

                if (ran.nextInt(100) < COMMITPERCENTAGE) {
                    wtx.commit();
                }
                do {
                    final int newKey = ran.nextInt(i + 1) + 1;
                    wtx.moveTo(newKey);
                } while (wtx.getNode() == null);
                // TODO Check if reference check can occur on "=="
                if (wtx.getNode().getKind() != ENodes.ELEMENT_KIND) {
                    wtx.moveToParent();
                }
            }
        }
        final long key = wtx.getNode().getNodeKey();
        wtx.remove();
        wtx.insertElementAsFirstChild(new QName(getString()));
        wtx.moveTo(key);
        wtx.commit();
        wtx.close();
        session.close();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    private static String getString() {
        char[] buf = new char[NUM_CHARS];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(ran.nextInt(chars.length()));
        }

        return new String(buf);
    }

}
