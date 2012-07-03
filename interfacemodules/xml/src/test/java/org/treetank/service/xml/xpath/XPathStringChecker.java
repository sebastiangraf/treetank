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

package org.treetank.service.xml.xpath;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;
import static org.treetank.node.IConstants.ROOT_NODE;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.treetank.TestHelper;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.node.interfaces.IValNode;

public class XPathStringChecker {

    @BeforeMethod
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @AfterMethod
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    public static void testIAxisConventions(final INodeReadTrx rtx, final AbsAxis axis,
        final String[] expectedValues) {

        // IAxis Convention 1.
        final long startKey = axis.getNode().getNodeKey();

        final String[] strValues = new String[expectedValues.length];
        int offset = 0;
        while (axis.hasNext()) {
            axis.next();
            // IAxis results.
            if (offset >= expectedValues.length) {
                Assert.fail("More nodes found than expected.");
            }
            if (axis.getNode() instanceof IValNode
                && !("".equals(new String(((IValNode)axis.getNode()).getRawValue())))) {
                strValues[offset++] = new String(((IValNode)axis.getNode()).getRawValue());
            } else {
                strValues[offset++] = rtx.getQNameOfCurrentNode().toString();
            }

            // IAxis Convention 2.
            try {
                axis.next();
                Assert.fail("Should only allow to call next() once.");
            } catch (Exception e) {
                // Must throw exception.
            }

            // IAxis Convention 3.
            axis.moveTo(ROOT_NODE);

        }

        // IAxis Convention 5.
        assertEquals(startKey, axis.getNode().getNodeKey());

        // IAxis results.
        assertArrayEquals(expectedValues, strValues);

    }
}
