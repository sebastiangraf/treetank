package com.treetank.service.xml.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.exception.AbsTTException;

import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_ID;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT_SPACES;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_REST;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_XMLDECL;

public class XMLSerializerPropertiesTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testXMLSerializerProp() {
        final String path = TestHelper.PATHS.PATH1.getFile().getAbsolutePath();
        if (!new File(path).mkdirs()) {
            fail("Directories couldn't be created!");
        }
        final XMLSerializerProperties props = new XMLSerializerProperties();

        final ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>)props.getmProps();
        final Enumeration<String> keys = map.keys();

        assertNotNull(keys);

        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();

            if (key.equals(S_ID[0])) {
                assertEquals(false, (Boolean)map.get(S_ID[0]));
            } else if (key.equals(S_REST[0])) {
                assertEquals(false, (Boolean)map.get(S_REST[0]));
            } else if (key.equals(S_INDENT[0])) {
                assertEquals(true, (Boolean)map.get(S_INDENT[0]));
            } else if (key.equals(S_INDENT_SPACES[0])) {
                assertSame(2, (Integer)map.get(S_INDENT_SPACES[0]));
            } else if (key.equals(S_XMLDECL[0])) {
                assertEquals(true, (Boolean)map.get(S_XMLDECL[0]));
            }
        }
    }
}
