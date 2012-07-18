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

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.TestHelper;
import org.treetank.exception.TTException;
import org.treetank.node.ElementNode;

public class AttributeAndNamespaceTest {

    private Holder holder;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        NodeHelper.createTestDocument();
        holder = Holder.generateRtx();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testAttribute() throws TTException {
        holder.getNRtx().moveTo(1L);
        assertEquals(1, ((ElementNode)holder.getNRtx().getNode()).getAttributeCount());
        holder.getNRtx().moveToAttribute(0);
        assertEquals("i", holder.getNRtx().getQNameOfCurrentNode().getLocalPart());

        holder.getNRtx().moveTo(9L);
        assertEquals(1, ((ElementNode)holder.getNRtx().getNode()).getAttributeCount());
        holder.getNRtx().moveToAttribute(0);
        assertEquals("p:x", new StringBuilder(holder.getNRtx().getQNameOfCurrentNode().getPrefix()).append(
            ":").append(holder.getNRtx().getQNameOfCurrentNode().getLocalPart()).toString());
        assertEquals("ns", holder.getNRtx().getQNameOfCurrentNode().getNamespaceURI());
    }

    @Test
    public void testNamespace() throws TTException {
        holder.getNRtx().moveTo(1L);
        assertEquals(1, ((ElementNode)holder.getNRtx().getNode()).getNamespaceCount());
        holder.getNRtx().moveToNamespace(0);
        assertEquals("p", holder.getNRtx().getQNameOfCurrentNode().getLocalPart());
        assertEquals("ns", holder.getNRtx().getQNameOfCurrentNode().getNamespaceURI());
    }
}
