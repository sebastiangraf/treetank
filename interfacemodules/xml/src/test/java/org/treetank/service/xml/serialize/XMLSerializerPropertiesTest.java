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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_ID;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT_SPACES;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_REST;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_XMLDECL;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.exception.TTException;
import org.treetank.testutil.CoreTestHelper;

public class XMLSerializerPropertiesTest {

    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        CoreTestHelper.closeEverything();
    }

    @Test
    public void testXMLSerializerProp() {
        final XMLSerializerProperties props = new XMLSerializerProperties();

        final ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>)props.getmProps();
        final Enumeration<String> keys = map.keys();

        assertNotNull(keys);

        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();

            if (key.equals(S_ID[0])) {
                assertSame(false, (Boolean)map.get(S_ID[0]));
            } else if (key.equals(S_REST[0])) {
                assertSame(false, (Boolean)map.get(S_REST[0]));
            } else if (key.equals(S_INDENT[0])) {
                assertSame(true, (Boolean)map.get(S_INDENT[0]));
            } else if (key.equals(S_INDENT_SPACES[0])) {
                assertSame(2, (Integer)map.get(S_INDENT_SPACES[0]));
            } else if (key.equals(S_XMLDECL[0])) {
                assertSame(true, (Boolean)map.get(S_XMLDECL[0]));
            }
        }
    }
}
