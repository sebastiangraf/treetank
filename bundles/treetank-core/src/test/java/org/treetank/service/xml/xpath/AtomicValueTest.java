/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: AtomicValueTest.java 4362 2008-08-24 11:46:16Z kramis $
 */

package org.treetank.service.xml.xpath;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.utils.TypedValue;

public class AtomicValueTest {

    private AtomicValue a, b, c, d, e, f, zero, n_zero;

    private int aVal = 1;

    private String bVal = "test";

    private float cVal = 2345.1441f;

    private double dVal = 245E2;

    private boolean eVal = true;

    @Before
    public void setUp() {

        a = new AtomicValue(aVal, Type.INTEGER);
        b = new AtomicValue(bVal, Type.STRING);
        c = new AtomicValue(cVal, Type.FLOAT);
        d = new AtomicValue(dVal, Type.DOUBLE);
        e = new AtomicValue(eVal);
        f = new AtomicValue(2.0d, Type.DOUBLE);

        zero = new AtomicValue(+0, Type.INTEGER);
        n_zero = new AtomicValue(-0, Type.INTEGER);

    }

    @Test
    public final void testGetInt() {

        assertThat(a.getInt(), is(aVal));
        assertThat(zero.getInt(), is(+0));
        assertThat(n_zero.getInt(), is(-0));
    }

    @Test
    public final void testGetDBL() {
        assertThat(d.getDBL(), is(dVal));
        assertThat(f.getDBL(), is(2.0d));
    }

    @Test
    public final void testGetFLT() {
        assertThat(c.getFLT(), is((float)cVal));

    }

    @Test
    public final void testGetString() {

        assertThat(TypedValue.parseString(b.getRawValue()), is(bVal));
    }

    @Test
    public final void testGetBool() {
        assertEquals(true, e.getBool());
    }

    @Test
    public final void testGetStringValue() {
        testGetString();
    }

}
