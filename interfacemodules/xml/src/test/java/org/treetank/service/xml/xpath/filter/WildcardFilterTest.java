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

package org.treetank.service.xml.xpath.filter;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.TestHelper;
import org.treetank.axis.filter.AbsFilterTest;
import org.treetank.axis.filter.WildcardFilter;
import org.treetank.exception.TTException;

public class WildcardFilterTest {

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
        TestHelper.deleteEverything();
    }

    @Test
    public void testIFilterConvetions() throws TTException {
        holder.getNRtx().moveTo(9L);
        AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "b", true), true);
        holder.getNRtx().moveToAttribute(0);
        try {
            AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "p", false), true);
            Assert.fail("Expected an Exception, because attributes are not supported.");
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage(), "Wildcards are not supported in attribute names yet.");

        }
        // IFilterTest.testIFilterConventions(new
        // WildcardFilter(holder.getRtx(), "b",
        // true), true);

        // holder.getRtx().moveTo(3L);
        // IFilterTest.testIFilterConventions(new ItemFilter(holder.getRtx()),
        // true);

        holder.getNRtx().moveTo(1L);
        AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "p", false), true);
        AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "a", true), true);
        AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "c", true), false);
        AbsFilterTest.testIFilterConventions(new WildcardFilter(holder.getNRtx(), "b", false), false);

    }
}
