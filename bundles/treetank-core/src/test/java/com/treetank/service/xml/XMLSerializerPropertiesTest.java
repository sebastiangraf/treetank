package com.treetank.service.xml;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.exception.TreetankException;

public class XMLSerializerPropertiesTest {
    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testXMLSerializerProp() {
        final String path = TestHelper.PATHS.PATH1.getFile().getAbsolutePath();
        if (!new File(path).mkdirs()) {
            TestCase.fail("Directories couldn't be created!");
        }
        final XMLSerializerProperties props = new XMLSerializerProperties(
                new File(path, "props").getAbsolutePath());

        final ConcurrentHashMap<String, Object> map = props.getmProps();
        final Enumeration<String> keys = map.keys();

        while (keys.hasMoreElements()) {
            System.out.println(map.get(keys.nextElement()));
        }

        props.write();
    }
}
