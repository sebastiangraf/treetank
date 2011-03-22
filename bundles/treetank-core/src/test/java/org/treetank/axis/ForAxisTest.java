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

package org.treetank.axis;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit-test class to test the functionality of the DubFilter.
 * 
 * @author Tina Scherer
 * 
 */
public class ForAxisTest {

    @Before
    public void setUp() throws AbsTTException {

        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testFor() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());

        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(1L);

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::text() return child::node()"),
            new long[] {
                4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L
            });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/node()"),
            new long[] {
                6L, 7L, 11L, 12L
            });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/text()"),
            new long[] {
                6L, 12L
            });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/c"), new long[] {
            7L, 11L
        });

        // IAxisTest.testIAxisConventions(new XPathAxis(
        // rtx,
        // "for $a in child::node(), $b in /node(), $c in ., $d in /c return $a/c"),
        // new long[] {7L, 11L});

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a[@p:x]"),
            new long[] {
                9L
            });

        AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in . return $a"), new long[] {
            1L
        });

        final AbsAxis axis = new XPathAxis(rtx, "for $i in (10, 20), $j in (1, 2) return ($i + $j)");
        assertEquals(true, axis.hasNext());

        assertEquals("11.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("12.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("21.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("22.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}
