/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: IFilterTest.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.axis;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;

public class IFilterTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    public static void testIFilterConventions(final IFilter filter,
            final boolean expected) {

        final IReadTransaction rtx = filter.getTransaction();

        // IFilter Convention 1.
        final long startKey = rtx.getNode().getNodeKey();

        assertEquals(expected, filter.filter());

        // IAxis Convention 2.
        assertEquals(startKey, rtx.getNode().getNodeKey());

    }

    @Test
    public void testIFilterExample() {
        // Do nothing. This class is only used with other test cases.
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }
}
