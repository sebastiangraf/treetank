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

package org.treetank.access;

import static org.treetank.node.IConstants.ATTRIBUTE;
import static org.treetank.node.IConstants.ELEMENT;
import static org.treetank.node.IConstants.NAMESPACE;
import static org.treetank.node.IConstants.TEXT;

import java.util.Random;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;

public final class OverallTest {

    private static int NUM_CHARS = 3;
    private static int ELEMENTS = 1000;
    private static int COMMITPERCENTAGE = 20;
    private static int REMOVEPERCENTAGE = 20;
    private static final Random ran = new Random(0l);
    public static String chars = "abcdefghijklm";

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        holder = Holder.generateWtx();
    }

    @Test
    public void testJustEverything() throws AbsTTException {
        holder.getNWtx().insertElementAsFirstChild(new QName(getString()));
        for (int i = 0; i < ELEMENTS; i++) {
            if (ran.nextBoolean()) {
                switch (holder.getNWtx().getNode().getKind()) {
                case ELEMENT:
                    holder.getNWtx().setQName(new QName(getString()));
                    holder.getNWtx().setURI(getString());
                    break;
                case ATTRIBUTE:
                    holder.getNWtx().setQName(new QName(getString()));
                    holder.getNWtx().setURI(getString());
                    holder.getNWtx().setValue(getString());
                    break;
                case NAMESPACE:
                    holder.getNWtx().setQName(new QName(getString()));
                    holder.getNWtx().setURI(getString());
                    break;
                case TEXT:
                    holder.getNWtx().setValue(getString());
                    break;
                default:
                }
            } else {
                if (holder.getNWtx().getNode() instanceof ElementNode) {
                    if (ran.nextBoolean()) {
                        holder.getNWtx().insertElementAsFirstChild(
                                new QName(getString()));
                    } else {
                        holder.getNWtx().insertElementAsRightSibling(
                                new QName(getString()));
                    }
                    while (ran.nextBoolean()) {
                        holder.getNWtx().insertAttribute(
                                new QName(getString()), getString());
                        holder.getNWtx().moveTo(
                                holder.getNWtx().getNode().getParentKey());
                    }
                    while (ran.nextBoolean()) {
                        holder.getNWtx().insertNamespace(
                                new QName(getString(), getString()));
                        holder.getNWtx().moveTo(
                                holder.getNWtx().getNode().getParentKey());
                    }
                }

                if (ran.nextInt(100) < REMOVEPERCENTAGE) {
                    holder.getNWtx().remove();
                }

                if (ran.nextInt(100) < COMMITPERCENTAGE) {
                    holder.getNWtx().commit();
                }
                do {
                    final int newKey = ran.nextInt(i + 1) + 1;
                    holder.getNWtx().moveTo(newKey);
                } while (holder.getNWtx().getNode() == null);
                // TODO Check if reference check can occur on "=="
                if (holder.getNWtx().getNode().getKind() != IConstants.ELEMENT) {
                    holder.getNWtx().moveTo(
                            holder.getNWtx().getNode().getParentKey());
                }
            }
        }
        final long key = holder.getNWtx().getNode().getNodeKey();
        holder.getNWtx().remove();
        holder.getNWtx().insertElementAsFirstChild(new QName(getString()));
        holder.getNWtx().moveTo(key);
        holder.getNWtx().commit();
        holder.getNWtx().close();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
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
